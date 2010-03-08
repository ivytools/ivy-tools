package com.nurflugel.mergegrapher;

import com.nurflugel.WebAuthenticator;
import com.nurflugel.externalsreporter.ui.HtmlHandler;
import com.nurflugel.mergegrapher.domain.CopyInfo;
import com.nurflugel.mergegrapher.domain.Path;
import com.nurflugel.mergegrapher.domain.Type;
import org.apache.commons.lang.StringUtils;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.wc.*;
import java.io.IOException;
import java.util.*;
import java.net.Authenticator;
import static com.nurflugel.mergegrapher.domain.Type.*;
import static org.apache.commons.lang.StringUtils.countMatches;
import static org.apache.commons.lang.StringUtils.remove;
import static org.apache.commons.lang.StringUtils.substringBeforeLast;
import static org.tmatesoft.svn.core.wc.SVNRevision.HEAD;

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
  private String projectBaseUrl = "http://localhost/svn/mergeTest";

  // private String projectBaseUrl = "http://ivy-tools.googlecode.com/svn";
  private Map<String, Path> pathMap  = new TreeMap<String, Path>();
  private List<CopyInfo>    copyInfo = new ArrayList<CopyInfo>();

  // --------------------------- main() method ---------------------------

  public static void main(String[] args)
  {
    SubversionMergeGrapher grapher = new SubversionMergeGrapher();

    try
    {
      grapher.doIt();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  private void doIt() throws IOException
  {
    try
    {
      Authenticator.setDefault(new WebAuthenticator());
      DAVRepositoryFactory.setup();

      SVNClientManager svnClientManager = SVNClientManager.newInstance();
      SVNWCClient      wcClient         = svnClientManager.getWCClient();

      // find creation and deletion of all trunk, branches, tags - record revisions at each event.
      findAllRevisionsWithAddDeletes(svnClientManager, projectBaseUrl);

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

      findAllPaths(true, allPaths, projectBaseUrl, htmlHandler);

      Map<Path, Set<Long>> pathRevisions = getAllRevisionsForPaths(allPaths, svnClientManager);

      for (Path path : pathRevisions.keySet())
      {
        if (!path.getPathName().equals(BRANCHES) && !path.getPathName().equals(TAGS))
        {
          Set<Long> revisions = pathRevisions.get(path);

          findMergeInfoForBranch(wcClient, path, revisions);
        }
      }

      for (CopyInfo info : copyInfo)
      {
        System.out.println("<><><><><><> info = " + info);
      }

      GraphVizOutput graphVizOutput=new GraphVizOutput(pathMap, copyInfo);
    }
    catch (SVNException e)
    {
      e.printStackTrace();
    }
  }



  /** find creation and deletion of all trunk, branches, tags - record revisions at each event. */
  private Map<Long, SVNLogEntry> findAllRevisionsWithAddDeletes(SVNClientManager svnClientManager, String url) throws SVNException
  {
    final Map<Long, SVNLogEntry> logEntries = findAllRevisionsForUrl(svnClientManager, url);

    for (Long aLong : logEntries.keySet())
    {
      handleLogEntries(aLong, logEntries);
    }

    return logEntries;
  }

  /**
   * Make a map of all revisions found for this URL. We put this into a map so we can get the log entries for a revisions.
   *
   * @param  svnClientManager  Previously created client manager
   * @param  urlString         the URL of interest
   */
  private Map<Long, SVNLogEntry> findAllRevisionsForUrl(SVNClientManager svnClientManager, String urlString) throws SVNException
  {
    SVNURL                       url        = SVNURL.parseURIDecoded(urlString);
    SVNLogClient                 logClient  = svnClientManager.getLogClient();
    final Map<Long, SVNLogEntry> logEntries = new TreeMap<Long, SVNLogEntry>();

    ISVNLogEntryHandler          handler    = new ISVNLogEntryHandler()
    {
      @Override
      public void handleLogEntry(SVNLogEntry logEntry) throws SVNException
      {
        logEntries.put(logEntry.getRevision(), logEntry);
      }
    };
    logClient.doLog(url, null, HEAD, SVNRevision.create(0), HEAD, true, true, 999999999, handler);

    return logEntries;
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
  private void handleLogEntries(Long revision, Map<Long, SVNLogEntry> logEntries)
  {
    SVNLogEntry logEntry = logEntries.get(revision);
    String      author   = logEntry.getAuthor();
    String      message  = logEntry.getMessage();

    System.out.println("Revision " + revision + ", author " + author + " Date " + logEntry.getDate());  // + ", message " + message);

    Map changedPaths = logEntry.getChangedPaths();

    for (Object key : changedPaths.keySet())
    {
      SVNLogEntryPath entryPath    = (SVNLogEntryPath) changedPaths.get(key);
      char            typeChar     = entryPath.getType();
      Type            type         = findByValue(typeChar);
      String          pathName     = entryPath.getPath();
      String          copyPath     = entryPath.getCopyPath();
      long            copyRevision = entryPath.getCopyRevision();

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

            path.modifyPath(this, pathMap, copyPath, copyRevision, type, revision, copyInfo);

            System.out.println("    Revision " + revision + " changed path " + type + " : = " + pathName + " copyPath= " + copyPath
                               + " copyRevision= " + copyRevision);
          }
        }
      }
      // System.out.println("Revision " + revision + ", author " + author + " Date " + logEntry.getDate());
    }
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
    Path path;

    if (pathMap.containsKey(pathName))
    {
      path = pathMap.get(pathName);
    }
    else
    {
      path = new Path(pathName);
      pathMap.put(pathName, path);
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
  private void findAllPaths(boolean browseNextLevel, List<Path> paths, String url, HtmlHandler htmlHandler) throws IOException
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
        findAllPaths(false, paths, projectBaseUrl + pathName, htmlHandler);
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
  private Map<Path, Set<Long>> getAllRevisionsForPaths(List<Path> allPaths, SVNClientManager svnClientManager) throws SVNException
  {
    Map<Path, Set<Long>> pathRevisions = new TreeMap<Path, Set<Long>>();

    for (Path path : allPaths)
    {
      Map<Long, SVNLogEntry> logEntryMap    = findAllRevisionsForUrl(svnClientManager, projectBaseUrl + path.getPathName());
      Set<Long>              revisions      = logEntryMap.keySet();
      Set<Long>              sortedRevisons = new TreeSet<Long>(revisions);

      pathRevisions.put(path, sortedRevisons);
    }

    return pathRevisions;
  }

  /** Find the merge info details for all the given revisions on the URL. */
  private void findMergeInfoForBranch(SVNWCClient wcClient, Path path, Set<Long> revisionsForBranch) throws SVNException
  {
    String           urlText  = projectBaseUrl + path.getPathName();
    SVNURL           url      = SVNURL.parseURIDecoded(urlText);
    SVNPropertyValue oldValue = null;

    for (Long revision : revisionsForBranch)
    {
      SVNRevision     svnRevision     = SVNRevision.create(revision);
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
    }
  }
}
