package com.nurflugel.ivytracker.handlers;

import ca.odell.glazedlists.EventList;
import com.nurflugel.externalsreporter.ui.HtmlHandler;
import com.nurflugel.externalsreporter.ui.ScanExternalsTask;
import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivytracker.Config;
import com.nurflugel.ivytracker.IvyTrackerMainFrame;
import static com.nurflugel.ivytracker.IvyTrackerMainFrame.useSingleThread;
import com.nurflugel.ivytracker.domain.Project;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static java.util.concurrent.TimeUnit.MINUTES;

/** Subversion implementation (is there going to be another? if the IvyFileFinderHandler */
@SuppressWarnings({ "CallToPrintStackTrace", "AssignmentToCollectionOrArrayFieldFromParameter", "ProhibitedExceptionDeclared" })
public class SubversionIvyFileFinderHandler extends IvyFileFinderHandler
{
  private final IvyTrackerMainFrame mainFrame;

  /** Map of Ivy files keyed by project URL. */
  private final Map<Project, List<IvyPackage>> ivyFiles;
  private final EventList<Project>             projectUrls;
  private final Config                         config;
  private final String[]                       repositories;
  private HtmlHandler                          urlHandler;
  private ExecutorService                      threadPool  = Executors.newFixedThreadPool(5);
  private boolean                              recurseDirs;

  public SubversionIvyFileFinderHandler(IvyTrackerMainFrame mainFrame, Map<Project, List<IvyPackage>> ivyFiles, EventList<Project> projectUrls,
                                        Config config, String... repositories)
  {
    this.mainFrame    = mainFrame;
    this.ivyFiles     = ivyFiles;
    this.projectUrls  = projectUrls;
    this.config       = config;
    this.repositories = repositories;
    urlHandler        = new HtmlHandler();
  }

  // -------------------------- OTHER METHODS --------------------------
  @Override
  protected Object doInBackground() throws Exception
  {
    doIt();
    threadPool.shutdown();

    // block until all threads are done, or until time limit is reached
    threadPool.awaitTermination(5, MINUTES);
    mainFrame.setProjectsDone(true);
    mainFrame.setReady(true);

    return null;
  }

  @Override
  public void doIt()
  {
    for (String repository : repositories)
    {
      try
      {
        List<String> files  = urlHandler.getFiles(repository, true);
        boolean      isRoot = ScanExternalsTask.isProjectRoot(files);

        if (isRoot)
        {
          for (String file : files)
          {
            SubversionIvyFileFinderHandlerTask task = new SubversionIvyFileFinderHandlerTask(file, config, urlHandler, mainFrame, ivyFiles,
                                                                                             projectUrls);

            if (useSingleThread)
            {
              task.run();
            }
            else
            {
              threadPool.execute(task);
            }
          }
        }
        else
        {
          // if it's not a project root, go down only one level (could be branches or trunk)
          for (String file : files)
          {
            processFile(file);
          }
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  private void processFile(String file)
  {
    try
    {
      List<String> childFiles  = urlHandler.getFiles(file, true);
      boolean      isChildRoot = isProjectRoot(childFiles);

      if (isChildRoot)
      {
        if (file.endsWith("/"))  // todo what does this do?
        {
          for (String childFile : childFiles)
          {
            mainFrame.setStatusLabel("Getting ivy files for " + file);

            SubversionIvyFileFinderHandlerTask task = new SubversionIvyFileFinderHandlerTask(childFile, config, urlHandler, mainFrame, ivyFiles,
                                                                                             projectUrls);

            if (useSingleThread)
            {
              task.run();
            }
            else
            {
              threadPool.execute(task);
            }
          }
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
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
