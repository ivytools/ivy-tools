package com.nurflugel.externalsreporter.ui;

import ca.odell.glazedlists.EventList;

import com.nurflugel.ivytracker.IvyTrackerMainFrame;

import org.tmatesoft.svn.core.wc.SVNWCClient;

import java.io.IOException;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jan 12, 2010 Time: 7:25:32 PM To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({ "CallToPrintStackTrace" })
public class RepositoryScanner implements Runnable
{
  private String                              repositoryUrl;
  private boolean                             recursive;
  private SVNWCClient                         wcClient;
  private HtmlHandler                         urlHandler;
  private boolean                             showBranches;
  private boolean                             showTags;
  private boolean                             showTrunks;
  private SubversionHandler                   subversionHandler;
  private EventList<ProjectExternalReference> projectsList;
  private ExternalsFinderMainFrame            mainFrame;
  private boolean                             selectAllExternals;
  private boolean                             selectAllProjects;
  private EventList<External>                 externalsList;
  private ExecutorService                     threadPool = Executors.newFixedThreadPool(1);

  public RepositoryScanner(String repositoryUrl, boolean isRecursive, SVNWCClient wcClient, HtmlHandler urlHandler, boolean showBranches,
                           boolean showTags, boolean showTrunks, SubversionHandler subversionHandler,
                           EventList<ProjectExternalReference> projectsList, ExternalsFinderMainFrame mainFrame, boolean selectAllExternals,
                           boolean selectAllProjects, EventList<External> externalsList)
  {
    this.repositoryUrl      = repositoryUrl;
    this.recursive          = isRecursive;
    this.wcClient           = wcClient;
    this.urlHandler         = urlHandler;
    this.showBranches       = showBranches;
    this.showTags           = showTags;
    this.showTrunks         = showTrunks;
    this.subversionHandler  = subversionHandler;
    this.projectsList       = projectsList;
    this.mainFrame          = mainFrame;
    this.selectAllExternals = selectAllExternals;
    this.selectAllProjects  = selectAllProjects;
    this.externalsList      = externalsList;
  }

  @Override
  public void run()
  {
    try
    {
      List<String> files = urlHandler.getFiles(repositoryUrl, true);

      for (String file : files)
      {
        ScanDirForExternalsTask task = new ScanDirForExternalsTask(file, this);

        if (IvyTrackerMainFrame.useSingleThread)
        {
          task.run();
        }
        else
        {
          threadPool.execute(task);
        }
      }

      threadPool.shutdown();

      // block until all threads are done, or until time limit is reached
      threadPool.awaitTermination(5, MINUTES);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  void findExternals(String url) throws IOException, InterruptedException
  {
    List<String> files  = urlHandler.getFiles(url, true);
    boolean      isRoot = isProjectRoot(files);

    if (isRoot)
    {
      for (String file : files)
      {
        scanRootFile(file);
      }
    }
    else
    {
      // if it's not a project root, go down only one level (could be branches or trunk)
      for (String file : files)
      {
        try
        {
          List<String> childFiles  = urlHandler.getFiles(file, true);
          boolean      isChildRoot = isProjectRoot(childFiles);

          if (isChildRoot || recursive)
          {
            if (file.endsWith("/"))
            {
              mainFrame.setStatus("Getting externals for " + file);
              subversionHandler.getExternals(file, wcClient, externalsList, projectsList, selectAllExternals, selectAllProjects);
              findExternals(file);
            }
          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    }
  }

  private void scanRootFile(String file)
  {
    try
    {
      boolean showTheBranch = file.endsWith("branches/") && showBranches;
      boolean showTheTag    = file.endsWith("tags/") && showTags;

      if (showTheBranch || showTheTag)
      {
        List<String> branches = urlHandler.getFiles(file, true);

        for (String branch : branches)
        {
          mainFrame.setStatus("Getting externals for " + branch);
          subversionHandler.getExternals(branch, wcClient, externalsList, projectsList, selectAllExternals, selectAllProjects);

          if (recursive)
          {
            try
            {
              findExternals(branch);
            }
            catch (InterruptedException e)
            {
              e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
            }
          }
        }
      }
      else if (file.endsWith("trunk/") && showTrunks)
      {
        // it's trunk
        mainFrame.setStatus("Getting externals for " + file);
        subversionHandler.getExternals(file, wcClient, externalsList, projectsList, selectAllExternals, selectAllProjects);

        if (recursive)
        {
          try
          {
            findExternals(file);
          }
          catch (InterruptedException e)
          {
            e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
          }
        }
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();        // To change body of catch statement use File | Settings | File Templates.
    }
  }

  /** If this is a project root, the children will consist of branches, trunk, and tags. */
  boolean isProjectRoot(List<String> files)
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
