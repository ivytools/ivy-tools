package com.nurflugel.mergegrapher;

import com.nurflugel.WebAuthenticator;
import org.apache.commons.lang.StringUtils;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.wc.*;
import java.util.*;
import java.net.Authenticator;
import static com.nurflugel.mergegrapher.Type.*;
import static org.apache.commons.lang.StringUtils.countMatches;
import static org.tmatesoft.svn.core.wc.SVNRevision.HEAD;

/** Lets see if we can get Subversion merge information, etc from properties! */
@SuppressWarnings({ "UseOfSystemOutOrSystemErr", "CallToPrintStackTrace" })
public class SubversionMergeGrapher
{
  private static final String MERGE_INFO_PROPERTY = "svn:mergeinfo";

  // private String projectBaseUrl = "http://subversion/svn/cdmII/trunk";
  // private static final String projectBaseUrl      = "http://subversion/svn/admin";
  // private String projectBaseUrl = "http://subversion/svn/ordercapture/trunk";
  // private String projectBaseUrl = "http://subversion/svn/buildtasks";
  // private String projectBaseUrl = "http://ivy-tools.googlecode.com/svn/trunk";
  private String            projectBaseUrl = "http://ivy-tools.googlecode.com/svn";
  private Map<String, Path> pathMap        = new TreeMap<String, Path>();

  public static void main(String[] args)
  {
    SubversionMergeGrapher grapher = new SubversionMergeGrapher();

    grapher.doIt();
  }

  private void doIt()
  {
    try
    {
      Authenticator.setDefault(new WebAuthenticator());
      DAVRepositoryFactory.setup();

      SVNClientManager svnClientManager = SVNClientManager.newInstance();
      SVNWCClient      wcClient         = svnClientManager.getWCClient();
      SVNURL           url              = SVNURL.parseURIDecoded(projectBaseUrl);

      // find creation and deletion of all trunk, branches, tags - record revisions at each event.
      final Map<Long, SVNLogEntry> logEntries = findAllRevisionsWithAddDeletes(svnClientManager, url);

      for (String pathKey : pathMap.keySet())
      {
        System.out.println(":::::::path found: " + pathMap.get(pathKey));
      }

      // find all trunk, branches, and tags for each of the above, find all revisions Go through the revisions, for each revision find if a merge has
      // taken place capture that revision and merge from info (branch, revisions, etc)

      Set<Long> revisionsForBranch = logEntries.keySet();

      findMergeInfoForBranch(wcClient, url, revisionsForBranch);
    }
    catch (SVNException e)
    {
      e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
    }
  }

  /** Find the merge info details for all the given revisions on the URL. */
  private void findMergeInfoForBranch(SVNWCClient wcClient, SVNURL url, Set<Long> revisionsForBranch) throws SVNException
  {
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
          System.out.println("Revision = " + revision + " had a change to merge info");

          String   textValue = value.toString();
          String[] values    = textValue.split("\n");

          for (String theValue : values)
          {
            System.out.println("      value = " + theValue);
          }

          oldValue = value;
        }
      }
      else
      {
        // System.out.println("Didn't get any merge data for " + url);
      }
    }
  }

  /** find creation and deletion of all trunk, branches, tags - record revisions at each event. */
  private Map<Long, SVNLogEntry> findAllRevisionsWithAddDeletes(SVNClientManager svnClientManager, SVNURL url) throws SVNException
  {
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

    for (Long aLong : logEntries.keySet())
    {
      handleLogEntries(aLong, logEntries);
    }

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
        if (pathName.contains("/branches") || pathName.equals("/trunk") || pathName.contains("/tags"))
        {
          boolean isTrunk     = StringUtils.equals(pathName, "/trunk");
          boolean isNewBranch = isNewBranch(pathName, "/branches");
          boolean isNewTag    = isNewBranch(pathName, "/tags");

          if (isTrunk || isNewBranch || isNewTag)
          {
            if (pathMap.containsKey(pathName))
            {
              Path path = pathMap.get(pathName);

              modifyPath(path, copyPath, copyRevision, type, revision);
            }
            else
            {
              Path path = createNewPath(pathName);

              modifyPath(path, copyPath, copyRevision, type, revision);
            }

            System.out.println("    Revision " + revision + " changed path " + type + " : = " + pathName + " copyPath= " + copyPath
                               + " copyRevision= " + copyRevision);
          }
        }
      }
    }
    // System.out.println("Revision " + revision + ", author " + author + " Date " + logEntry.getDate());
  }

  private Path createNewPath(String pathName)
  {
    Path path = new Path(pathName);

    pathMap.put(pathName, path);

    return path;
  }

  /**
   * Modify the path object with the information given. Paths can only have one copy path (where they were copied from), only one copy revision, and
   * only one delete revision.
   *
   * @param  path          The path object to modify. Note that this may already have stuff in it - we don't overwrite non-null/default information.
   * @param  copyPathName  if this is type "A", then this is the path this path was copied from (i.e., if you make a branch from trunk, then trunk is
   *                       the copyPath). Note - this WILL BE NULL for initial creation of trunk, branches, and tag dirs.
   * @param  copyRevision  the revision the branch is being created from
   * @param  type          Either A or D
   * @param  revision      the revision of whatever action is taking place (creation or deletion)
   */
  private void modifyPath(Path path, String copyPathName, long copyRevision, Type type, long revision)
  {
    Path copyPath = (copyPathName == null) ? null
                                           : pathMap.get(copyPathName);

    if ((copyPathName != null) && !pathMap.containsKey(copyPathName))
    {
      copyPath = createNewPath(copyPathName);
    }

    if (type == A)
    {
      if ((copyPath != null) && (path.getCopyPath() == null))
      {
        path.setCopyPath(copyPath);
      }

      if (path.getCreationRevision() == null)
      {
        String pathName = path.getPathName();

        // if it's trunk/branches/tags, there's no copy revision, use the revision
        if (pathName.equals("/trunk") || pathName.equals("/tags") || pathName.equals("/branches"))
        {
          path.setCreationRevision(revision);
        }
        else if ((copyRevision != -1))
        {
          path.setCreationRevision(copyRevision);
        }
      }
    }

    if ((type == D) && (path.getDeleteRevision() == null) && (revision != -1))
    {
      path.setDeleteRevision(revision);
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
}
