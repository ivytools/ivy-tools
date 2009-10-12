package com.nurflugel.ivybrowser.handlers;

import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivybrowser.ui.IvyBrowserMainFrame;
import org.apache.commons.lang.StringUtils;
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
import javax.swing.*;
import static javax.swing.JFileChooser.*;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Apr 27, 2009 Time: 10:31:06 PM To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({ "ProtectedField" })
public abstract class BaseWebHandler extends SwingWorker<Object, Object>
{
  public static final int NUMBER_OF_THREADS = 5;

  // public static final int NUMBER_OF_THREADS = 1;
  private static final int BLOCK_SIZE = 1024;
  protected IvyBrowserMainFrame mainFrame;
  protected boolean isTest;
  protected boolean shouldRun = true;
  protected String ivyRepositoryPath;
  protected List<IvyPackage> ivyPackages;
  private Map<String, Map<String, Map<String, IvyPackage>>> packageMap;
  private Map<String, IvyPackage> allPackages  = Collections.synchronizedMap(new HashMap<String, IvyPackage>());
  public static final String      JAVADOC      = "javadoc";
  public static final String      SOURCE       = "source";
  public static final String      DEFAULT      = "default";
  public static final String      INFO         = "info";
  public static final String      PUBLICATIONS = "publications";
  public static final String      ARTIFACT     = "artifact";
  public static final String      DEPENDENCIES = "dependencies";
  public static final String      NAME         = "name";
  public static final String      EXT          = "ext";
  public static final String      CONF         = "conf";
  public static final String      ORG          = "org";
  public static final String      REV          = "rev";

  protected BaseWebHandler(IvyBrowserMainFrame mainFrame, List<IvyPackage> ivyPackages, String ivyRepositoryPath,
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

  /**
   * Download the actual jar file to wherever the user wants it.
   *
   * <p>Todo - the jars aren't coming down right - what's up with that?</p>
   */
  public void downloadFile(JLabel fileLabel, String orgName, String moduleName, String version) throws IOException
  {
    String       text            = fileLabel.getText().split(" ")[0];
    String       newText         = StringUtils.substringBeforeLast(text, ".") + "." + StringUtils.substringAfterLast(text, ".");
    URL          fileUrl         = new URL(ivyRepositoryPath + "/" + orgName + "/" + moduleName + "/" + version + "/" + newText);
    JFileChooser fileChooser     = new JFileChooser("Save file as...");
    String       previousSaveDir = mainFrame.getPreferredSaveDir();  // todo get dir preference from Preferences, save if changed
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

    int returnVal = fileChooser.showSaveDialog(mainFrame);

    if (returnVal == APPROVE_OPTION)
    {
      File selectedFile = fileChooser.getSelectedFile();

      mainFrame.setPreferredSaveDir(selectedFile.getParent());

      BufferedInputStream  in   = new BufferedInputStream(fileUrl.openStream());
      FileOutputStream     fos  = new FileOutputStream(selectedFile);
      BufferedOutputStream bout = new BufferedOutputStream(fos, BLOCK_SIZE);
      byte[]               data = new byte[BLOCK_SIZE];

      while (in.read(data, 0, BLOCK_SIZE) >= 0)
      {
        bout.write(data);
      }

      bout.close();
      in.close();
    }
  }

  public List<String> findIncludedFiles(String repositoryUrl, String orgName, String moduleName, String version) throws IOException
  {
    List<String>  includedFiles = new ArrayList<String>();

    URL           versionUrl    = new URL(repositoryUrl + "/" + orgName + "/" + moduleName + "/" + version);
    URLConnection urlConnection = versionUrl.openConnection();

    urlConnection.setAllowUserInteraction(true);
    urlConnection.connect();

    InputStream    in     = urlConnection.getInputStream();
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    String         line   = reader.readLine();

    while (line != null)
    {
      boolean shouldProcess = shouldProcessIncludedFileLine(line);

      if (shouldProcess)
      {
        includedFiles.add(parseIncludedFileInfo(line, version));
      }

      line = reader.readLine();
    }

    return includedFiles;
  }

  protected abstract boolean shouldProcessIncludedFileLine(String line);

  /** Parse the file name out of the html line. */
  protected abstract String parseIncludedFileInfo(String line, String version);

  public void findVersions(URL repositoryUrl, String orgName, String moduleName) throws IOException, JDOMException
  {
    URL           versionUrl    = new URL(repositoryUrl + "/" + orgName + "/" + moduleName);
    URLConnection urlConnection = versionUrl.openConnection();

    urlConnection.setAllowUserInteraction(true);
    urlConnection.connect();

    InputStream    in          = urlConnection.getInputStream();
    BufferedReader reader      = new BufferedReader(new InputStreamReader(in));
    String         versionLine = reader.readLine();

    while (versionLine != null)
    {
      boolean hasVersion = hasVersion(versionLine);

      if (hasVersion && !versionLine.contains("Parent Directory"))
      {
        String version = getContents(versionLine);

        mainFrame.setStatusLabel("Parsing " + moduleName + " version " + version);
        findVersionedLibrary(repositoryUrl, stripSlash(orgName), stripSlash(moduleName), stripSlash(version));
      }

      versionLine = reader.readLine();
    }

    reader.close();
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
    List             artifacts     = publications.getChildren(ARTIFACT);
    Element          dependencies  = root.getChild(DEPENDENCIES);
    IvyPackage       ivyPackage    = getIvyPackage(orgName, moduleName, version);

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
      else if (conf.equals(DEFAULT))  // it's a jar or something
      {
        ivyPackage.addPublication(name + "." + ext);
      }
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

  protected abstract boolean shouldProcessVersionedLibraryLine(String line);
  protected abstract String getContents(String packageLine);

  private void addPackages(List<IvyPackage> localPackages)
  {
    ivyPackages.addAll(localPackages);

    for (IvyPackage localPackage : localPackages)
    {
      addPackage(localPackage);
    }
  }

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
}
