package com.nurflugel.ivytracker.handlers;

import ca.odell.glazedlists.EventList;
import com.nurflugel.ivytracker.IvyTrackerMainFrame;
import com.nurflugel.ivytracker.domain.IvyFile;
import com.nurflugel.ivytracker.domain.IvyFileImpl;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.prefs.Preferences;
import javax.swing.*;

@SuppressWarnings({ "CallToPrintStackTrace", "IOResourceOpenedButNotSafelyClosed", "UseOfSystemOutOrSystemErr" })
public class HtmlHandler extends SwingWorker<Object, Object>
{
  // private boolean useTestData = true;
  // private boolean saveTestData = false;
  protected static final String BASE_NAME                   = "/Users/douglasbullard/Documents/JavaStuff/Nike Subversion Projects/JavaExternals/trunk/maintenance/IvyBrowser/";
  private static final String   PROJECT_IVY_FILES_DATA_FILE = BASE_NAME + "projectIvyFilesData.xml";
  private static final String   ALL_IVY_FILES_DATA_FILE     = BASE_NAME + "allIvyFilesData.xml";
  private static final String   IVY_FILES_MAP_FILE          = BASE_NAME + "ivyFilesMap.xml";

  // private boolean isTest = false;
  private boolean             shouldRun         = true;
  private IvyTrackerMainFrame mainFrame;
  private URL                 startingUrl;
  private EventList<IvyFile>  repositoryList;
  private Set<String>         missingIvyFiles;
  private static final String PROJECT_IVY_FILES = "PROJECT_IVY_FILES";

  // --------------------------- CONSTRUCTORS ---------------------------
  public HtmlHandler(IvyTrackerMainFrame mainFrame, URL startingUrl, EventList<IvyFile> repositoryList, Set<String> missingIvyFiles)
  {
    this.mainFrame       = mainFrame;
    this.startingUrl     = startingUrl;
    this.repositoryList  = repositoryList;
    this.missingIvyFiles = missingIvyFiles;
  }

  // -------------------------- OTHER METHODS --------------------------
  @Override
  @SuppressWarnings({ "unchecked" })
  public Object doInBackground()
  {
    Map<String, IvyFile> ivyFilesMap     = new TreeMap<String, IvyFile>();
    List<IvyFile>        projectIvyFiles = new ArrayList<IvyFile>();
    Collection<IvyFile>  allIvyFiles     = new HashSet<IvyFile>();

    if (mainFrame.useTestData())
    {
      projectIvyFiles.addAll((Collection<? extends IvyFile>) readTestData(PROJECT_IVY_FILES_DATA_FILE));
      ivyFilesMap.putAll((Map<String, IvyFile>) readTestData(IVY_FILES_MAP_FILE));
      allIvyFiles.addAll(ivyFilesMap.values());
    }
    else
    {
      getProjectIvyFiles(projectIvyFiles, ivyFilesMap);
      findIvyPackages(allIvyFiles, ivyFilesMap);
    }

    if (mainFrame.saveTestData())
    {
      writeTestData(PROJECT_IVY_FILES_DATA_FILE, projectIvyFiles);
      writeTestData(IVY_FILES_MAP_FILE, ivyFilesMap);
    }

    touchAllUsedPackages(projectIvyFiles, ivyFilesMap, missingIvyFiles);
    generateReportOfUnusedIvyFiles(allIvyFiles);
    mainFrame.populateTable(allIvyFiles, projectIvyFiles, ivyFilesMap);
    mainFrame.showNormal();

    return null;
  }

  private Object readTestData(String dataFile)
  {
    File    file    = new File(dataFile);
    XStream xstream = new XStream(new DomDriver());

    try
    {
      Reader            reader      = new FileReader(file);
      ObjectInputStream inputStream = xstream.createObjectInputStream(reader);

      return inputStream.readObject();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (ClassNotFoundException e)
    {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Add any project in Preferences, present in dialog for confirmation, save and go.
   *
   * <p>todo most of this!</p>
   */
  private void getProjectIvyFiles(List<IvyFile> projectIvyFiles, Map<String, IvyFile> ivyFilesMap)
  {
    // todo - get list from text box...
    try
    {
      Preferences preferences        = mainFrame.getPreferences();
      String      projectFilesString = preferences.get(PROJECT_IVY_FILES, "");

      projectFilesString = getProjectIvyFilesFromDialog(projectFilesString);
      preferences.put(PROJECT_IVY_FILES, projectFilesString);

      List<ProjectIvyFile> projectIvyFilesss = getProjectIvyFilesFromTextBlock(projectIvyFiles);

      for (ProjectIvyFile projectIvyFiless : projectIvyFilesss)
      {
        getIvyFileFromUrl(ivyFilesMap, projectIvyFiles, projectIvyFiless.getPathToIvyFile(), projectIvyFiless.getProjectName());
      }
      // getIvyFileFromUrl(ivyFilesMap, projectIvyFiles, "http://ivy-tools.googlecode.com/svn/trunk/build/", "IvyTools");
      // getIvyFileFromUrl(ivyFilesMap, projectIvyFiles, "http://camb2bp2:8090/svn/javaexternals/trunk/maintenance/ResourceBundler/build/", "Resource
      // Bundler"); getIvyFileFromUrl(ivyFilesMap, projectIvyFiles, "http://camb2bp2:8090/svn/javaexternals/trunk/maintenance/ivy/", "Ivy");
      // getIvyFileFromUrl(ivyFilesMap, projectIvyFiles, "http://camb2bp2:8090/svn/jboss.servers/trunk/build/", "Ivy");
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  private List<ProjectIvyFile> getProjectIvyFilesFromTextBlock(List<IvyFile> projectIvyFiles)
  {
    return null;
  }

  private String getProjectIvyFilesFromDialog(String projectFilesString)
  {
    return null;
  }

  private void getIvyFileFromUrl(Map<String, IvyFile> ivyFilesMap, List<IvyFile> projectIvyFiles, String urlPath, String projectName)
                          throws IOException
  {
    // get any ivy files (*.ivy.xml)
    URL           url           = new URL(urlPath);
    URLConnection urlConnection = url.openConnection();

    urlConnection.setAllowUserInteraction(true);

    // set timeout to 10 seconds if Subversion is hung on old server
    urlConnection.setConnectTimeout(10000);

    try
    {
      urlConnection.connect();

      InputStream    in          = urlConnection.getInputStream();
      BufferedReader reader      = new BufferedReader(new InputStreamReader(in));
      String         versionLine = reader.readLine();

      while (versionLine != null)
      {
        boolean shouldProcess = versionLine.contains("<li") && !versionLine.contains("..") && !versionLine.contains("md5")
                                && !versionLine.contains("sha1");

        if (shouldProcess)
        {
          boolean isIvyFile = versionLine.contains("ivy.xml<");

          if (isIvyFile)
          {
            String ivyFileName = getContents(versionLine);

            getIvyFile(url, ivyFileName, projectIvyFiles, "com.nurflugel", projectName, "trunk", ivyFilesMap);

            String text = "Found ivy file " + ivyFileName + " for project " + projectName + "/trunk";

            System.out.println(text);
            mainFrame.setStatusLabel(text);
          }
        }

        versionLine = reader.readLine();
      }

      reader.close();
    }
    catch (SocketTimeoutException e)
    {
      JOptionPane.showMessageDialog(mainFrame, "Unable to connect to " + urlPath);
    }
  }

  private String getContents(String packageLine)
  {
    int    index  = packageLine.indexOf("\"");
    String result = packageLine.substring(index + 1);

    index  = result.indexOf("/");
    result = result.substring(0, index);
    index  = result.indexOf("\"");

    if (index > -1)
    {
      result = result.substring(0, index);
    }

    return result;
  }

  private void getIvyFile(URL versionUrl, String ivyFileName, Collection<IvyFile> ivyFiles, String org, String module, String revision,
                          Map<String, IvyFile> ivyFilesMap) throws IOException
  {
    URL     ivyFileUrl = new URL(versionUrl + "/" + ivyFileName);
    IvyFile ivyFile    = new IvyFileImpl(org, module, revision, ivyFileUrl, ivyFiles, ivyFilesMap, mainFrame, repositoryList);
  }

  public Collection<IvyFile> findIvyPackages(Collection<IvyFile> ivyFiles, Map<String, IvyFile> ivyFilesMap)
  {
    try
    {
      URL           repositoryUrl = startingUrl;
      URLConnection urlConnection = repositoryUrl.openConnection();

      urlConnection.setAllowUserInteraction(true);
      urlConnection.setConnectTimeout(10000);
      urlConnection.connect();

      InputStream    in          = urlConnection.getInputStream();
      BufferedReader reader      = new BufferedReader(new InputStreamReader(in));
      String         packageLine = reader.readLine();
      int            i           = 0;

      while (packageLine != null)
      {
        boolean hasLink = packageLine.contains("href") && !packageLine.contains("..");

        if (packageLine.contains("</ul>"))
        {
          break;
        }

        if (hasLink)
        {
          String orgName = getContents(packageLine);

          findModules(repositoryUrl, orgName, ivyFiles, ivyFilesMap);

          // if left in, this populates the display real time
          if (!shouldRun || (mainFrame.isTest() && (i++ > 4)))
          {
            break;
          }
        }

        packageLine = reader.readLine();
      }

      reader.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    // todo just add to the table model, and then the UI will refersh on the fly
    // mainFrame.populateTable(ivyPackages);
    return ivyFiles;
  }

  private void findModules(URL repositoryUrl, String orgName, Collection<IvyFile> ivyFiles, Map<String, IvyFile> ivyFilesMap) throws IOException
  {
    URL           moduleUrl     = new URL(repositoryUrl + "/" + orgName);
    URLConnection urlConnection = moduleUrl.openConnection();

    urlConnection.setAllowUserInteraction(true);
    urlConnection.setConnectTimeout(10000);
    urlConnection.connect();

    InputStream    in         = urlConnection.getInputStream();
    BufferedReader reader     = new BufferedReader(new InputStreamReader(in));
    String         moduleLine = reader.readLine();

    while (moduleLine != null)
    {
      boolean isLibrary = moduleLine.contains("<li") && !moduleLine.contains("..");

      if (isLibrary)
      {
        String moduleName = getContents(moduleLine);

        findVersions(repositoryUrl, orgName, moduleName, ivyFiles, ivyFilesMap);
      }

      moduleLine = reader.readLine();
    }

    reader.close();
  }

  private void findVersions(URL repositoryUrl, String orgName, String moduleName, Collection<IvyFile> ivyFiles, Map<String, IvyFile> ivyFilesMap)
                     throws IOException
  {
    URL           versionUrl    = new URL(repositoryUrl + "/" + orgName + "/" + moduleName);
    URLConnection urlConnection = versionUrl.openConnection();

    urlConnection.setAllowUserInteraction(true);
    urlConnection.setConnectTimeout(10000);
    urlConnection.connect();

    InputStream    in          = urlConnection.getInputStream();
    BufferedReader reader      = new BufferedReader(new InputStreamReader(in));
    String         versionLine = reader.readLine();

    while (versionLine != null)
    {
      boolean hasVersion = versionLine.contains("<li") && !versionLine.contains("..");

      if (hasVersion)
      {
        String version = getContents(versionLine);

        // mainFrame.setStatusLabel("Parsing " + moduleName + " version " + version);
        findVersionedLibrary(repositoryUrl, orgName, moduleName, version, ivyFiles, ivyFilesMap);
      }

      versionLine = reader.readLine();
    }

    reader.close();
  }

  /** Get the actual Ivy xml file for this dude. */
  @SuppressWarnings({ "OverlyComplexBooleanExpression" })
  private void findVersionedLibrary(URL repositoryUrl, String orgName, String moduleName, String version, Collection<IvyFile> ivyFiles,
                                    Map<String, IvyFile> ivyFilesMap) throws IOException
  {
    URL           versionUrl    = new URL(repositoryUrl + "/" + orgName + "/" + moduleName + "/" + version);
    URLConnection urlConnection = versionUrl.openConnection();

    urlConnection.setAllowUserInteraction(true);
    urlConnection.setConnectTimeout(10000);
    urlConnection.connect();

    InputStream    in          = urlConnection.getInputStream();
    BufferedReader reader      = new BufferedReader(new InputStreamReader(in));
    String         versionLine = reader.readLine();

    while (versionLine != null)
    {
      boolean shouldProcess = versionLine.contains("<li") && !versionLine.contains("..") && !versionLine.contains("md5")
                              && !versionLine.contains("sha1");

      if (shouldProcess)
      {
        boolean isIvyFile = versionLine.contains(".ivy.xml<");

        if (isIvyFile)
        {
          String ivyFileName = getContents(versionLine);

          // todo actually read in the xml file and add this to the list of stuff
          getIvyFile(versionUrl, ivyFileName, ivyFiles, orgName, moduleName, version, ivyFilesMap);

          break;
        }
      }  // end if

      versionLine = reader.readLine();
    }    // end while

    reader.close();
  }

  private void writeTestData(String dataFile, Object data)
  {
    File    file    = new File(dataFile);
    XStream xstream = new XStream();

    try
    {
      Writer             fileWriter = new FileWriter(file);
      ObjectOutputStream out        = xstream.createObjectOutputStream(fileWriter);

      out.writeObject(data);
      out.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  /** Walk through all the project ivy files and down the dependency trees, touching anything used. */
  private void touchAllUsedPackages(List<IvyFile> projectIvyFiles, Map<String, IvyFile> ivyFilesMap, Set<String> missingIvyFiles)
  {
    System.out.println("HtmlHandler.touchAllUsedPackages");

    for (IvyFile projectIvyFile : projectIvyFiles)
    {
      touchDependency(projectIvyFile, ivyFilesMap, missingIvyFiles);
    }
  }

  private void touchDependency(IvyFile ivyFile, Map<String, IvyFile> ivyFilesMap, Set<String> missingIvyFiles)
  {
    System.out.println("HtmlHandler.touchDependency");
    ivyFile.touch();

    Collection<String> dependencies = ivyFile.getDependencies();

    for (String key : dependencies)
    {
      IvyFile childIvyFile = ivyFilesMap.get(key);

      // how can thsi be null!!!
      if (childIvyFile != null)
      {
        touchDependency(childIvyFile, ivyFilesMap, missingIvyFiles);
      }
      else
      {
        missingIvyFiles.add(new StringBuilder().append(key).append(" is required by ").append(ivyFile.getKey()).toString());
      }
    }
  }

  private void generateReportOfUnusedIvyFiles(Collection<IvyFile> allIvyFiles)
  {
    System.out.println("\n\n\n\n\nHere are the ivy files that have no usages:");

    for (IvyFile ivyFile : allIvyFiles)  // System.out.println(ivyFile.getKey()+ " count="+ivyFile.getCount());
    {
      if (ivyFile.getCount() == 0)
      {
        System.out.println("====>    " + ivyFile.getKey() + " is not used");
      }
    }
  }
}
