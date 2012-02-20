package com.nurflugel.mergegrapher;

import com.nurflugel.Os;
import static com.nurflugel.Os.findOs;
import com.nurflugel.WebAuthenticator;
import com.nurflugel.externalsreporter.ui.HtmlHandler;
import com.nurflugel.ivybrowser.domain.Revision;
import static com.nurflugel.ivygrapher.NodeOrder.TOP_TO_BOTTOM;
import static com.nurflugel.ivygrapher.OutputFormat.PDF;
import com.nurflugel.mergegrapher.domain.CopyInfo;
import com.nurflugel.mergegrapher.domain.Path;
import com.nurflugel.mergegrapher.domain.Type;
import static com.nurflugel.mergegrapher.domain.Type.A;
import static com.nurflugel.mergegrapher.domain.Type.D;
import static com.nurflugel.mergegrapher.domain.Type.findByValue;
import org.apache.commons.lang.StringUtils;
import static org.apache.commons.lang.StringUtils.countMatches;
import static org.apache.commons.lang.StringUtils.equals;
import static org.apache.commons.lang.StringUtils.remove;
import static org.apache.commons.lang.StringUtils.substringAfterLast;
import static org.apache.commons.lang.StringUtils.substringBeforeLast;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNPropertyData;
import org.tmatesoft.svn.core.wc.SVNRevision;
import static org.tmatesoft.svn.core.wc.SVNRevision.HEAD;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** Lets see if we can get Subversion merge information, etc from properties! */
@SuppressWarnings({ "UseOfSystemOutOrSystemErr", "CallToPrintStackTrace" })
public class SubversionMergeGrapher
{
  public static final String  TRUNK               = "/trunk";
  public static final String  TAGS                = "/tags";
  public static final String  BRANCHES            = "/branches";
  private static final String MERGE_INFO_PROPERTY = "svn:mergeinfo";
  private static final String WIKI                = "/wiki";
  // private String              projectBaseUrl      = "http://subversion/svn/cdmII";

  // private static final String projectBaseUrl      = "http://subversion/svn/admin";
  // private String projectBaseUrl = "http://subversion/svn/ordercapturerunk";
  // private String projectBaseUrl = "http://subversion/svn/buildtasks";
  private String projectBaseUrl = "http://localhost/svn/grapher04";

  // private String projectBaseUrl = "http://ivy-tools.googlecode.com/svn";
  private Map<String, Path> pathMap  = new TreeMap<String, Path>();
  private List<CopyInfo>    copyInfo = new ArrayList<CopyInfo>();
  private String            dirPath;

  // --------------------------- main() method ---------------------------
  public static void main(String[] args)
  {
    SubversionMergeGrapher grapher = new SubversionMergeGrapher();

    try
    {
      GraphVizOutput graphVizOutput = grapher.getGraphVizOutput();
      File           dotFile        = grapher.generateDotFile(graphVizOutput);

      grapher.processDotFile(graphVizOutput, dotFile);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  File generateDotFile(GraphVizOutput graphVizOutput) throws IOException
  {
    try
    {
      Authenticator.setDefault(new WebAuthenticator());
      DAVRepositoryFactory.setup();

      SVNClientManager svnClientManager = SVNClientManager.newInstance();
      SVNWCClient      wcClient         = svnClientManager.getWCClient();

      // find creation and deletion of all trunk, branches, tags - record revisions at each event.
      Map<Revision, SVNLogEntry> entryMap = findAllRevisionsWithAddDeletes(svnClientManager, projectBaseUrl);

      for (String pathKey : pathMap.keySet())
      {
        Path path = pathMap.get(pathKey);

        if (path.isActive())
        {
          System.out.println(":::::::path found: " + path);  // todo still need to toggle active tag branches
        }
      }

      /**
       * <ul>
       * <li>find all trunk, branches, and tags for each of the above,
       * <li>find all revisions
       * <li>Go through the revisions,
       * <li>for each revision find if a merge has taken place capture that revision and merge from info (branch, revisions, etc)
       */
      List<Path>  allPaths    = new ArrayList<Path>();
      HtmlHandler htmlHandler = new HtmlHandler();

      findAllPathsInRepository(true, allPaths, projectBaseUrl, htmlHandler);

      Map<Path, Set<Revision>> pathRevisions = getAllRevisionsForPaths(allPaths, svnClientManager);

      findMergeInfo(wcClient, pathRevisions);

      for (CopyInfo info : copyInfo)
      {
        System.out.println("<><><><><><> info = " + info);
      }

      String repositoryName = substringAfterLast(projectBaseUrl, "/");
      File   file           = graphVizOutput.makeDotFile(pathMap, copyInfo, repositoryName, dirPath);

      return file;
    }
    catch (SVNException e)
    {
      e.printStackTrace();

      // dbulla CODEREVIEW - icky - fix this
      return null;
    }
  }

  private void findMergeInfo(SVNWCClient wcClient, Map<Path, Set<Revision>> pathRevisions) throws SVNException
  {
    for (Path path : pathRevisions.keySet())
    {
      if (!path.getPathName().equals(BRANCHES) && !path.getPathName().equals(TAGS))
      {
        Set<Revision> revisions = pathRevisions.get(path);

        findMergeInfoForBranch(wcClient, path, revisions);
      }
    }
  }

  GraphVizOutput getGraphVizOutput()
  {
    Os             os             = findOs();
    GraphVizOutput graphVizOutput = new GraphVizOutput(os, PDF, false, os.getDefaultDotPath(), true, TOP_TO_BOTTOM);

    return graphVizOutput;
  }

  private void processDotFile(GraphVizOutput graphVizOutput, File file)
  {
    graphVizOutput.processDotFile(file);
  }

  /** find creation and deletion of all trunk, branches, tags - record revisions at each event. */
  private Map<Revision, SVNLogEntry> findAllRevisionsWithAddDeletes(SVNClientManager svnClientManager, String url) throws SVNException
  {
    final Map<Revision, SVNLogEntry> logEntries = findAllRevisionsForUrl(svnClientManager, url);

    for (Revision revision : logEntries.keySet())
    {
      parseLogEntriesForPathMap(revision, logEntries);
    }

    return logEntries;
  }

  /**
   * Make a map of all revisions found for this URL. We put this into a map so we can get the log entries for a revisions.
   *
   * @param  svnClientManager  Previously created client manager
   * @param  urlString         the URL of interest
   */
  private Map<Revision, SVNLogEntry> findAllRevisionsForUrl(SVNClientManager svnClientManager, String urlString) throws SVNException
  {
    SVNURL                           url        = SVNURL.parseURIDecoded(urlString);
    SVNLogClient                     logClient  = svnClientManager.getLogClient();
    final Map<Revision, SVNLogEntry> logEntries = new TreeMap<Revision, SVNLogEntry>();
    ISVNLogEntryHandler              handler    = new ISVNLogEntryHandler()
    {
      @Override
      public void handleLogEntry(SVNLogEntry logEntry) throws SVNException
      {
        processLogEntryForRevision(logEntry, logEntries);
      }
    };
    logClient.doLog(url, null, HEAD, SVNRevision.create(0), HEAD, true, true, 999999999, handler);

    return logEntries;
  }

  private void processLogEntryForRevision(SVNLogEntry logEntry, Map<Revision, SVNLogEntry> logEntries)
  {
    logEntries.put(new Revision(logEntry.getRevision()), logEntry);
  }

  //J-
  /**
   * For each log entry, parse it to see if it's an A or D (add or delete) of a branch.
   *
   * Here's an example of log entries:
   *     Revision 1   changed path A : = /trunk                          copyPath= null   copyRevision= -1
   *     Revision 1   changed path A : = /tags                           copyPath= null   copyRevision= -1
   *     Revision 1   changed path A : = /branches                       copyPath= null   copyRevision= -1
   *     Revision 108 changed path A : = /branches/2009-10-11-JdomReader copyPath= /trunk copyRevision= 107
   *     Revision 109 changed path A : = /tags/2009-10-11-ReleaseToWorld copyPath= /trunk copyRevision= 108
   *     Revision 196 changed path A : = /branches/trackerWork2          copyPath= /trunk copyRevision= 184
   *     Revision 197 changed path D : = /branches/2009-10-11-JdomReader copyPath= null   copyRevision= -1
   *     Revision 198 changed path A : = /branches/trackerWork           copyPath= /trunk copyRevision= 197
   *     head revision on trunk is 202
   *     Revision 202, author douglas.bullard Date Mon Feb 01 22:25:16 PST 2010
   *
   */
  //J+
  private void parseLogEntriesForPathMap(Revision revision, Map<Revision, SVNLogEntry> logEntries)
  {
    SVNLogEntry logEntry = logEntries.get(revision);
    String      author   = logEntry.getAuthor();
    String      message  = logEntry.getMessage();

    System.out.println("Revision " + revision + ", author " + author + " Date " + logEntry.getDate());  // + ", message " + message);

    Map changedPaths = logEntry.getChangedPaths();

    for (Object key : changedPaths.keySet())
    {
      parseLogEntryForPath(revision, changedPaths, key);
    }
  }

  private void parseLogEntryForPath(Revision revision, Map changedPaths, Object key)
  {
    SVNLogEntryPath entryPath    = (SVNLogEntryPath) changedPaths.get(key);
    char            typeChar     = entryPath.getType();
    Type            type         = findByValue(typeChar);
    String          pathName     = entryPath.getPath();
    String          copyPath     = entryPath.getCopyPath();
    Revision        copyRevision = new Revision(entryPath.getCopyRevision());

    // dbulla CODEREVIEW - what is this doing? break out into a method
    if ((type == A) || (type == D))
    {
      if (pathName.contains(BRANCHES) || pathName.equals(TRUNK) || pathName.contains(TAGS))
      {
        boolean isTrunk     = StringUtils.equals(pathName, TRUNK);
        boolean isNewBranch = isNewBranch(pathName, BRANCHES);
        boolean isNewTag    = isNewBranch(pathName, TAGS);

        if (isTrunk || isNewBranch || isNewTag)
        {
          Path path = getPath(pathName);

          if (path != null)
          {
            path.modifyPath(this, pathMap, copyPath, copyRevision, type, revision, copyInfo);
            System.out.println("    Revision " + revision + " changed path " + type + " : = " + pathName + " copyPath= " + copyPath
                                 + " copyRevision= " + copyRevision);
          }
        }
      }
    }
    // System.out.println("Revision " + revision + ", author " + author + " Date " + logEntry.getDate());
  }

  /** is this path JUST a branch? */
  boolean isNewBranch(String pathName, String branches)
  {
    if (!pathName.startsWith(branches))
    {
      return false;
    }

    String newPath = StringUtils.substringAfter(pathName, branches);

    // newpath should be just /xxxxx with no trailing slash or other dir afterwards
    int matches = countMatches(newPath, "/");

    return ((matches == 0) || (matches == 1));
  }

  /** If the path exists, return that. If not, create a new one and add it to the map */
  public Path getPath(String pathName)
  {
    Path path = null;

    if (pathMap.containsKey(pathName))
    {
      path = pathMap.get(pathName);
    }
    else
    {
      if (shouldProcessPath(pathName))
      {
        path = new Path(pathName);
        pathMap.put(pathName, path);
      }
    }

    return path;
  }

  /**
   * Find all the paths in the project's repository.
   *
   * @param  browseNextLevel  if true, will recurse in any directories found.
   * @param  paths            the list of paths we're going to add to
   * @param  url              the URL to search in
   * @param  htmlHandler      the handler which will do the work
   */
  private void findAllPathsInRepository(boolean browseNextLevel, List<Path> paths, String url, HtmlHandler htmlHandler) throws IOException
  {
    // this should return trunk, branches, tags (assuming they were created)
    List<String> rootDirs = htmlHandler.getFiles(url, true);

    for (String rootDir : rootDirs)
    {
      String pathName = remove(rootDir, projectBaseUrl);

      if (pathName.endsWith("/"))
      {
        pathName = substringBeforeLast(pathName, "/");
      }

      boolean shouldProcess = shouldProcessPath(pathName);

      if (shouldProcess)
      {
        Path path = getPath(pathName);

        paths.add(path);
      }

      if (browseNextLevel && !pathName.equals(TRUNK) && !pathName.equals(WIKI))
      {
        findAllPathsInRepository(false, paths, projectBaseUrl + pathName, htmlHandler);
      }
    }
  }

  /** don't bother processing anything in tags except the branches stored there. Process all branches and trunk. */
  boolean shouldProcessPath(String text)
  {
    String pathName = text.toUpperCase();

    if (pathName.equalsIgnoreCase(BRANCHES))
    {
      return false;  // don't handle branches dir
    }

    if (pathName.equalsIgnoreCase(TAGS))
    {
      return false;  // don't handle tags dir
    }

    if (pathName.startsWith(TAGS.toUpperCase() + "/PRODUCTION"))
    {
      return false;  // don't handle tags of production releases
    }

    if (pathName.startsWith(TAGS.toUpperCase() + BRANCHES.toUpperCase()))
    {
      return true;   // do handle archived branches (our convention is to put them in tags
    }

    if (pathName.startsWith(TAGS.toUpperCase() + "/"))
    {
      return false;  // don't handle ordinary tags
    }

    return true;
  }

  /** For the list of paths, make a map of all the revisions for each path. */
  private Map<Path, Set<Revision>> getAllRevisionsForPaths(List<Path> allPaths, SVNClientManager svnClientManager) throws SVNException
  {
    Map<Path, Set<Revision>> pathRevisions = new TreeMap<Path, Set<Revision>>();

    for (Path path : allPaths)
    {  // doesn't seem to be getting all revisions - check on this...

      Map<Revision, SVNLogEntry> logEntryMap    = findAllRevisionsForUrl(svnClientManager, projectBaseUrl + path.getPathName());
      Set<Revision>              revisions      = logEntryMap.keySet();
      Set<Revision>              sortedRevisons = new TreeSet<Revision>(revisions);
      List<Revision>             tempList       = new ArrayList<Revision>(sortedRevisons);
      Revision                   lastRevision   = tempList.get(tempList.size() - 1);

      path.setLastActiveRevision(lastRevision);
      pathRevisions.put(path, sortedRevisons);
    }

    return pathRevisions;
  }

  /** Find the merge info details for all the given revisions on the URL. */
  private void findMergeInfoForBranch(SVNWCClient wcClient, Path path, Set<Revision> revisionsForBranch) throws SVNException
  {
    String           urlText  = projectBaseUrl + path.getPathName();
    SVNURL           url      = SVNURL.parseURIDecoded(urlText);
    SVNPropertyValue oldValue = null;

    for (Revision revision : revisionsForBranch)
    {
      oldValue = findMergeInfoForBranchRevision(wcClient, path, url, oldValue, revision);
    }
  }

  private SVNPropertyValue findMergeInfoForBranchRevision(SVNWCClient wcClient, Path path, SVNURL url, SVNPropertyValue oldValue, Revision revision)
                                                   throws SVNException
  {
    SVNRevision     svnRevision     = SVNRevision.create(revision.getRevisionNumber());
    SVNPropertyData svnPropertyData = wcClient.doGetProperty(url, MERGE_INFO_PROPERTY, svnRevision, svnRevision);

    // System.out.println("Revision = " + revision);
    if (svnPropertyData != null)
    {
      SVNPropertyValue value = svnPropertyData.getValue();

      if (!value.equals(oldValue))
      {
        System.out.println("Path " + path.getPathName() + ",  revision " + revision + " had a change to merge info");
        path.handleMergeInfoChange(this, value, oldValue, revision, copyInfo);
        oldValue = value;
      }
    }
    else
    {
      // System.out.println("Didn't get any merge data for " + url);
    }

    return oldValue;
  }

  public void setProjectBaseUrl(String projectBaseUrl)
  {
    this.projectBaseUrl = projectBaseUrl;
  }

  public void setDirPath(String dirPath)
  {
    this.dirPath = dirPath;
  }
}
