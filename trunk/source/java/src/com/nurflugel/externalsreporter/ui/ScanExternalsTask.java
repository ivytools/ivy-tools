package com.nurflugel.externalsreporter.ui;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Background task to get subversion externals so UI can refresh asynchronously. */
@SuppressWarnings({ "CallToPrintStackTrace", "UseOfSystemOutOrSystemErr" })
public class ScanExternalsTask extends SwingWorker
{
  private Set<String>       repositoryUrls;
  private MainFrame         mainFrame;
  private boolean           shallowSearch;
  private SubversionHandler subversionHandler;
  private HtmlHandler       urlHandler   = new HtmlHandler();
  private boolean           showBranches;
  private boolean           showTags;
  private boolean           showTrunks;

  public ScanExternalsTask(Set<String> repositoryUrls, MainFrame mainFrame, boolean isShallowSearch, SubversionHandler subversionHandler,
                           boolean showBranches, boolean showTags, boolean showTrunks)
  {
    this.repositoryUrls    = repositoryUrls;
    this.mainFrame         = mainFrame;
    shallowSearch          = isShallowSearch;
    this.subversionHandler = subversionHandler;
    this.showBranches      = showBranches;
    this.showTags          = showTags;
    this.showTrunks        = showTrunks;
  }

  @Override
  protected Object doInBackground() throws Exception
  {
    List<External> externalList = new ArrayList<External>();

    for (String repositoryUrl : repositoryUrls)
    {
      try
      {
        if (shallowSearch)
        {
          findShallowExternals(repositoryUrl, externalList);
        }
        else
        {
          findDeepExternals(repositoryUrl, externalList);
        }
      }
      catch (IOException e)
      {
        e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
      }
      catch (SVNException e)
      {
        e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
      }

      for (External external : externalList)
      {
        System.out.println("Found external: " + external);
      }
    }

    mainFrame.processResults(externalList);

    return null;
  }

  /** Go through all dirs recursively, and find any externals there. */
  private void findDeepExternals(String repositoryUrl, List<External> externalList)
  {
    // something
  }

  /** Don't do a recursive search - just surface,. todo - do this in a task, so the UI will update */
  private void findShallowExternals(String repositoryUrl, List<External> externalList) throws IOException, SVNException
  {
    SVNWCClient  wcClient = SVNClientManager.newInstance().getWCClient();
    List<String> files    = urlHandler.getFiles(repositoryUrl);
    boolean      isRoot   = isProjectRoot(files);

    if (isRoot)
    {
      for (String file : files)
      {
        boolean showTheBranch = file.endsWith("branches/") && showBranches;
        boolean showTheTag    = file.endsWith("tags/") && showTags;

        if (showTheBranch || showTheTag)
        {
          List<String> branches = urlHandler.getFiles(file);

          for (String branch : branches)
          {
            mainFrame.setStatus("Getting externals for " + branch);
            subversionHandler.getExternals(branch, wcClient, externalList);
          }
        }
        else if (file.endsWith("trunk/") && showTrunks)
        {
          // it's trunk
          mainFrame.setStatus("Getting externals for " + file);
          subversionHandler.getExternals(file, wcClient, externalList);
        }
      }
    }
    else
    {
      // if it's not a project root, go down only one level (could be branches or trunk)
      // try multiple projects
      for (String file : files)
      {
        List<String> childFiles  = urlHandler.getFiles(file);
        boolean      isChildRoot = isProjectRoot(childFiles);

        if (isChildRoot)
        {
          findShallowExternals(file, externalList);
        }
      }
    }
  }

  /** If this is a project root, the children will consist of branches, trunk, and tags. */
  private boolean isProjectRoot(List<String> files)
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
