package com.nurflugel.externalsreporter.ui;

import ca.odell.glazedlists.EventList;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNWCClient;

import javax.swing.*;
import java.util.Set;

/** Background task to get subversion externals so UI can refresh asynchronously. */
@SuppressWarnings({ "CallToPrintStackTrace", "UseOfSystemOutOrSystemErr" })
public class ScanExternalsTask extends SwingWorker
{
  private Set<String>                         repositoryUrls;
  private MainFrame                           mainFrame;
  private boolean                             shallowSearch;
  private SubversionHandler                   subversionHandler;
  private HtmlHandler                         urlHandler           = new HtmlHandler();
  private boolean                             showBranches;
  private boolean                             showTags;
  private boolean                             showTrunks;
  private EventList<External>                 externalsList;
  private EventList<ProjectExternalReference> projectsList;
  private boolean                             isSelectAllExternals;
  private boolean                             isSelectAllProjects;
  public static final int                     NUMBER_OF_THREADS    = 1;

  public ScanExternalsTask(Set<String> repositoryUrls, MainFrame mainFrame, boolean isShallowSearch, SubversionHandler subversionHandler,
                           boolean showBranches, boolean showTags, boolean showTrunks, EventList<External> externalsList,
                           EventList<ProjectExternalReference> projectsList, boolean selectAllExternals, boolean selectAllProjects)
  {
    this.repositoryUrls    = repositoryUrls;
    this.mainFrame         = mainFrame;
    shallowSearch          = isShallowSearch;
    this.subversionHandler = subversionHandler;
    this.showBranches      = showBranches;
    this.showTags          = showTags;
    this.showTrunks        = showTrunks;
    this.externalsList     = externalsList;
    this.projectsList      = projectsList;
    isSelectAllExternals   = selectAllExternals;
    isSelectAllProjects    = selectAllProjects;
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
  @SuppressWarnings({ "ProhibitedExceptionDeclared" })
  protected Object doInBackground() throws Exception
  {
    SVNWCClient wcClient = SVNClientManager.newInstance().getWCClient();

    for (String repositoryUrl : repositoryUrls)
    {
      try
      {
        RepositoryScanner scanner = new RepositoryScanner(repositoryUrl, !shallowSearch, wcClient, urlHandler, showBranches, showTags, showTrunks,
                                                          subversionHandler, projectsList, mainFrame, isSelectAllExternals, isSelectAllProjects,
                                                          externalsList);

         scanner.run();
      }
      catch (Exception e)
      {
        e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
      }
      finally
      {
        mainFrame.processResults();
      }

      for (External external : externalsList)
      {
        System.out.println("Found external: " + external);
      }
    }

    return null;
  }
}
