package com.nurflugel.externalsreporter.ui;

import ca.odell.glazedlists.EventList;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.swing.SwingWorker;

/** Background task to get subversion externals so UI can refresh asynchronously. */
@SuppressWarnings({ "CallToPrintStackTrace", "UseOfSystemOutOrSystemErr" })
public class ScanExternalsTask extends SwingWorker
{
  private Set<String>                         repositoryUrls;
  private ExternalsFinderMainFrame            mainFrame;
  private boolean                             shallowSearch;
  private SubversionHandler                   subversionHandler;
  private HtmlHandler                         urlHandler            = new HtmlHandler();
  private boolean                             showBranches;
  private boolean                             showTags;
  private boolean                             showTrunks;
  private EventList<External>                 externalsList;
  private EventList<ProjectExternalReference> projectsList;
  private boolean                             isSelectAllExternals;
  private boolean                             isSelectAllProjects;
  private final ISVNAuthenticationManager     authenticationManager;

  public ScanExternalsTask(Set<String> repositoryUrls, ExternalsFinderMainFrame mainFrame, boolean isShallowSearch,
                           SubversionHandler subversionHandler, boolean showBranches, boolean showTags, boolean showTrunks,
                           EventList<External> externalsList, EventList<ProjectExternalReference> projectsList, boolean selectAllExternals,
                           boolean selectAllProjects, ISVNAuthenticationManager authenticationManager)
  {
    this.repositoryUrls        = repositoryUrls;
    this.mainFrame             = mainFrame;
    shallowSearch              = isShallowSearch;
    this.subversionHandler     = subversionHandler;
    this.showBranches          = showBranches;
    this.showTags              = showTags;
    this.showTrunks            = showTrunks;
    this.externalsList         = externalsList;
    this.projectsList          = projectsList;
    isSelectAllExternals       = selectAllExternals;
    isSelectAllProjects        = selectAllProjects;
    this.authenticationManager = authenticationManager;
  }

  /**
   * Go through the list of repositories, and find any externals. This will do one of the following:
   *
   * <ul>
   *   <li>Look for immediate branches, tags, and trunk. If not immediately found,
   *
   *     <ul>
   *       <li>Do a shallow search, which will look for immediate child directories (projects), and then branches, tags, and trunk.</li>
   *     </ul>
   *   </li>
   *   <li>Do a deep search - a recursive search of all directories.</li>
   * </ul>
   */
  @Override
  protected Object doInBackground() throws Exception
  {
    ISVNOptions options  = SVNWCUtil.createDefaultOptions(true);
    SVNWCClient wcClient = SVNClientManager.newInstance(options, authenticationManager).getWCClient();

    for (String repositoryUrl : repositoryUrls)
    {
      try
      {
        findExternals(repositoryUrl, !shallowSearch, wcClient);
      }
      catch (Exception e)
      {
        e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
      }

      for (External external : externalsList)
      {
        System.out.println("Found external: " + external);
      }
    }

    mainFrame.processResults();

    return null;
  }

  private void findExternals(String repositoryUrl, boolean isRecursive, SVNWCClient wcClient) throws IOException, SVNException
  {
    List<String> files  = urlHandler.getFiles(repositoryUrl, true);
    boolean      isRoot = isProjectRoot(files);

    if (isRoot)
    {
      for (String file : files)
      {
        boolean showTheBranch = file.endsWith("branches/") && showBranches;
        boolean showTheTag    = file.endsWith("tags/") && showTags;

        if (showTheBranch || showTheTag)
        {
          List<String> branches = urlHandler.getFiles(file, true);

          for (String branch : branches)
          {
            mainFrame.setStatus("Getting externals for " + branch);
            subversionHandler.getExternals(branch, wcClient, externalsList, projectsList, isSelectAllExternals, isSelectAllProjects);

            if (isRecursive)
            {
              findExternals(branch, isRecursive, wcClient);
            }
          }
        }
        else if (file.endsWith("trunk/") && showTrunks)
        {
          // it's trunk
          mainFrame.setStatus("Getting externals for " + file);
          subversionHandler.getExternals(file, wcClient, externalsList, projectsList, isSelectAllExternals, isSelectAllProjects);

          if (isRecursive)
          {
            findExternals(file, isRecursive, wcClient);
          }
        }
      }
    }
    else
    {
      // if it's not a project root, go down only one level (could be branches or trunk)
      for (String file : files)
      {
        List<String> childFiles  = urlHandler.getFiles(file, true);
        boolean      isChildRoot = isProjectRoot(childFiles);

        if (isChildRoot || isRecursive)
        {
          if (file.endsWith("/"))
          {
            mainFrame.setStatus("Getting externals for " + file);
            subversionHandler.getExternals(file, wcClient, externalsList, projectsList, isSelectAllExternals, isSelectAllProjects);
            findExternals(file, isRecursive, wcClient);
          }
        }
      }
    }
  }

  /** If this is a project root, the children will consist of branches, trunk, and tags. */
  public static boolean isProjectRoot(List<String> files)
  {
    boolean hasBranches = false;
    boolean hasTrunk    = false;
    boolean hasTags     = false;

    for (String file : files)
    {
      if (file.endsWith("branches/"))
      {
        hasBranches = true;
      }

      if (file.endsWith("tags/"))
      {
        hasTags = true;
      }

      if (file.endsWith("trunk/"))
      {
        hasTrunk = true;
      }
    }

    return hasBranches && hasTags && hasTrunk;
  }
}
