package com.nurflugel.ivybrowser.handlers;

import ca.odell.glazedlists.EventList;
import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivybrowser.ui.IvyBrowserMainFrame;
import org.apache.commons.lang.StringUtils;
import static org.apache.commons.lang.StringUtils.*;
import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;
import static javax.swing.JFileChooser.*;

/** The parent class of all the handlers that parse the repository. */
@SuppressWarnings({ "ProtectedField", "UseOfSystemOutOrSystemErr" })
public abstract class BaseWebHandler extends SwingWorker<Object, Object>
{
  public static final int NUMBER_OF_THREADS = 5;

  // public static final int NUMBER_OF_THREADS = 1;
  private static final int                                  BLOCK_SIZE        = 1024;
  protected IvyBrowserMainFrame                             mainFrame;
  protected boolean                                         isTest;
  protected boolean                                         shouldRun         = true;
  protected String                                          ivyRepositoryPath;
  protected EventList<IvyPackage>                           ivyPackages;
  private Map<String, Map<String, Map<String, IvyPackage>>> packageMap;
  private Map<String, IvyPackage>                           allPackages       = Collections.synchronizedMap(new HashMap<String, IvyPackage>());
  public static final String                                JAVADOC           = "javadoc";
  public static final String                                SOURCE            = "source";
  public static final String                                DEFAULT           = "default";
  public static final String                                INFO              = "info";
  public static final String                                PUBLICATIONS      = "publications";
  public static final String                                ARTIFACT          = "artifact";
  public static final String                                DEPENDENCIES      = "dependencies";
  public static final String                                NAME              = "name";
  public static final String                                EXT               = "ext";
  public static final String                                CONF              = "conf";
  public static final String                                ORG               = "org";
  public static final String                                REV               = "rev";
  ExecutorService                                           threadPool        = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

  @SuppressWarnings({ "AssignmentToCollectionOrArrayFieldFromParameter" })
  protected BaseWebHandler(IvyBrowserMainFrame mainFrame, EventList<IvyPackage> ivyPackages, String ivyRepositoryPath,
                           Map<String, Map<String, Map<String, IvyPackage>>> packageMap)
  {
    this.mainFrame         = mainFrame;
    this.ivyPackages       = ivyPackages;
    this.ivyRepositoryPath = ivyRepositoryPath;
    this.packageMap        = packageMap;
  }

  // -------------------------- OTHER METHODS --------------------------

  @Override
  public Object doInBackground()
  {
    findIvyPackages();
    mainFrame.showNormal();

    return null;
  }

  public abstract void findIvyPackages();

  /** Download the actual jar file to wherever the user wants it. */
  public static void downloadFile(JCheckBox fileLabel, String orgName, String moduleName, String version, IvyBrowserMainFrame theFrame,
                                  String thePath) throws IOException
  {
    String       text            = fileLabel.getText().split(" ")[0];
    String       newText         = substringBeforeLast(text, ".") + "-" + version + "." + substringAfterLast(text, ".");
    URL          fileUrl         = new URL(thePath + "/" + orgName + "/" + moduleName + "/" + version + "/" + newText);
    JFileChooser fileChooser     = new JFileChooser("Save file as...");
    String       previousSaveDir = theFrame.getPreferredSaveDir();  // todo get dir preference from Preferences, save if changed
    File         suggestedFile   = null;

    if (previousSaveDir == null)
    {
      suggestedFile = new File(text);
    }
    else
    {
      suggestedFile = new File(previousSaveDir, text);
    }

    fileChooser.setSelectedFile(suggestedFile);

    int returnVal = fileChooser.showSaveDialog(theFrame);

    if (returnVal == APPROVE_OPTION)
    {
      File selectedFile = fileChooser.getSelectedFile();

      downloadFile(fileUrl, selectedFile, theFrame);
    }
  }

  private static void downloadFile(URL fileUrl, File selectedFile, IvyBrowserMainFrame theFrame) throws IOException
  {
    theFrame.setPreferredSaveDir(selectedFile.getParent());

    FileOutputStream     fos       = new FileOutputStream(selectedFile);
    BufferedOutputStream bout      = new BufferedOutputStream(fos, BLOCK_SIZE);
    InputStream          in        = fileUrl.openStream();
    byte[]               data      = new byte[4 * BLOCK_SIZE];
    int                  bytesRead;

    while ((bytesRead = in.read(data)) != -1)
    {
      bout.write(data, 0, bytesRead);
    }

    bout.close();
    in.close();
  }

  public void findVersions(URL repositoryUrl, String orgName, String moduleName) throws IOException
  {
    URL           versionUrl    = new URL(repositoryUrl + "/" + orgName + "/" + moduleName);
    URLConnection urlConnection = versionUrl.openConnection();

    urlConnection.setAllowUserInteraction(true);
    urlConnection.connect();

    try
    {
      InputStream    in          = urlConnection.getInputStream();
      BufferedReader reader      = new BufferedReader(new InputStreamReader(in));
      String         versionLine = reader.readLine();

      while (versionLine != null)
      {
        boolean hasVersion = hasVersion(versionLine);

        if (hasVersion && !versionLine.contains("Parent Directory"))
        {
          String version = getContents(versionLine);

          mainFrame.setStatusLabel("Parsing " + repositoryUrl + " for " + moduleName + " version " + version);

          try
          {
            findVersionedLibrary(repositoryUrl, stripSlash(orgName), stripSlash(moduleName), stripSlash(version));
          }
          catch (IOException e)
          {
            System.out.println("You have a version with a missing XML file: " + stripSlash(orgName) + " " + stripSlash(moduleName) + " "
                               + stripSlash(version) + " " + e.getMessage());
          }
          catch (JDOMException e)
          {
            System.out.println("JDom couldn't parse the file for this library (probably a 0-byte file): " + stripSlash(orgName) + " "
                               + stripSlash(moduleName) + " " + stripSlash(version) + " " + e.getMessage());
          }
        }

        versionLine = reader.readLine();
      }

      reader.close();
    }
    catch (IOException e)
    {
      System.out.println("You have a missing versionUrl " + versionUrl + " " + e.getMessage());
    }
  }

  protected abstract boolean hasVersion(String versionLine);

  /** Read the .ivy file for the version and see what's there. */
  protected void findVersionedLibrary(URL repositoryUrl, String orgName, String moduleName, String version) throws IOException, JDOMException
  {
    URL           versionUrl    = new URL(repositoryUrl + "/" + orgName + "/" + moduleName + "/" + version);
    URLConnection urlConnection = versionUrl.openConnection();

    urlConnection.setAllowUserInteraction(true);
    urlConnection.connect();

    List<IvyPackage> localPackages = new ArrayList<IvyPackage>();
    SAXBuilder       builder       = new SAXBuilder();
    String           path          = repositoryUrl + "/" + orgName + "/" + moduleName + "/" + version + "/" + moduleName + "-" + version + ".ivy.xml";
    URL              url           = new URL(path);
    Document         doc           = builder.build(url);
    Element          root          = doc.getRootElement();
    Element          info          = root.getChild(INFO);
    Element          publications  = root.getChild(PUBLICATIONS);

    if (publications != null)
    {
      List       artifacts    = publications.getChildren(ARTIFACT);
      Element    dependencies = root.getChild(DEPENDENCIES);
      IvyPackage ivyPackage   = getIvyPackage(orgName, moduleName, version);

      localPackages.add(ivyPackage);

      for (Object item : artifacts)
      {
        Element artifact = (Element) item;
        String  name     = artifact.getAttributeValue(NAME);
        String  ext      = artifact.getAttributeValue(EXT);
        String  conf     = artifact.getAttributeValue(CONF);

        if (conf.equals(JAVADOC))
        {
          ivyPackage.setHasJavaDocs(true);
        }
        else if (conf.equals(SOURCE))
        {
          ivyPackage.setHasSourceCode(true);
        }

        ivyPackage.addPublication(name + "." + ext);
      }

      if (dependencies != null)
      {
        List children = dependencies.getChildren("dependency");

        for (Object item : children)
        {
          Element    dependency        = (Element) item;
          String     org               = dependency.getAttributeValue(ORG);
          String     module            = dependency.getAttributeValue(NAME);
          String     rev               = dependency.getAttributeValue(REV);
          String     conf              = dependency.getAttributeValue(CONF);
          IvyPackage dependencyPackage = getIvyPackage(org, module, rev);

          ivyPackage.addDependency(dependencyPackage);
        }
      }
    }

    addPackages(localPackages);
  }

  private IvyPackage getIvyPackage(String orgName, String moduleName, String version)
  {
    String key = IvyPackage.getKey(orgName, moduleName, version);

    if (allPackages.containsKey(key))
    {
      return allPackages.get(key);
    }
    else
    {
      IvyPackage ivyPackage = new IvyPackage(orgName, moduleName, version);

      allPackages.put(key, ivyPackage);

      return ivyPackage;
    }
  }

  protected abstract String getContents(String packageLine);

  private void addPackages(List<IvyPackage> localPackages)
  {
    ivyPackages.getReadWriteLock().writeLock().lock();
    ivyPackages.addAll(localPackages);
    ivyPackages.getReadWriteLock().writeLock().unlock();

    for (IvyPackage localPackage : localPackages)
    {
      addPackage(localPackage);
    }
  }

  /** puts the package into the map of packages. */
  private void addPackage(IvyPackage ivyPackage)
  {
    String                               orgName    = stripSlash(ivyPackage.getOrgName());
    String                               moduleName = stripSlash(ivyPackage.getModuleName());
    String                               version    = stripSlash(ivyPackage.getVersion());
    Map<String, Map<String, IvyPackage>> orgMap     = packageMap.get(orgName);

    if (orgMap == null)
    {
      orgMap = Collections.synchronizedMap(new HashMap<String, Map<String, IvyPackage>>());
      packageMap.put(orgName, orgMap);
    }

    Map<String, IvyPackage> moduleMap = orgMap.get(moduleName);

    if (moduleMap == null)
    {
      moduleMap = Collections.synchronizedMap(new HashMap<String, IvyPackage>());
      orgMap.put(moduleName, moduleMap);
    }

    IvyPackage thePackage = moduleMap.get(version);

    if (thePackage == null)
    {
      moduleMap.put(version, ivyPackage);
    }
    else
    {
      thePackage.setHasJavaDocs(ivyPackage.hasJavaDocs());
      thePackage.setHasSourceCode(ivyPackage.hasSourceCode());
      thePackage.setPublications(ivyPackage.getPublications());
      thePackage.setDependencies(ivyPackage.getDependencies());
    }
  }

  public static String stripSlash(String text)
  {
    return text.replaceAll("/", "");
  }

  public boolean isDirLink(String lowerLine)
  {
    boolean isHref     = lowerLine.contains("href");
    boolean isUp       = lowerLine.contains("..") || lowerLine.contains("parent directory");
    boolean isPre      = lowerLine.startsWith("<pre");
    boolean isDir      = lowerLine.contains("[dir]");
    boolean hasDirLink = isHref && !isUp && !isPre && isDir;

    return hasDirLink;
  }

  public void halt()
  {
    threadPool.shutdownNow();
  }
}
