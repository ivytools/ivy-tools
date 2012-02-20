package com.nurflugel.ivytracker.handlers;

import ca.odell.glazedlists.EventList;

import com.nurflugel.common.ui.UiMainFrame;

import com.nurflugel.externalsreporter.ui.HtmlHandler;

import com.nurflugel.ivybrowser.domain.IvyPackage;

import com.nurflugel.ivytracker.Config;
import com.nurflugel.ivytracker.domain.Project;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Subversion implementation (is there going to be another? if the IvyFileFinderHandler */
@SuppressWarnings({ "CallToPrintStackTrace", "AssignmentToCollectionOrArrayFieldFromParameter", "ProhibitedExceptionDeclared" })
public class SubversionIvyFileFinderHandlerTask implements Runnable
{
  private final String                         file;
  private Config                               config;
  private HtmlHandler                          urlHandler;
  private UiMainFrame                          mainFrame;
  private final Map<Project, List<IvyPackage>> ivyFiles;
  private EventList<Project>                   projectUrls;

  public SubversionIvyFileFinderHandlerTask(String file, Config config, HtmlHandler urlHandler, UiMainFrame mainFrame,
                                            Map<Project, List<IvyPackage>> ivyFiles, EventList<Project> projectUrls)
  {
    this.file        = file;
    this.config      = config;
    this.urlHandler  = urlHandler;
    this.mainFrame   = mainFrame;
    this.ivyFiles    = ivyFiles;
    this.projectUrls = projectUrls;
  }
  // ------------------------ INTERFACE METHODS ------------------------
  // --------------------- Interface Runnable ---------------------

  /** Scan the given file. */
  @Override
  public void run()
  {
    try
    {
      boolean showTheBranch = file.endsWith("branches/") && config.showBranches();
      boolean showTheTag    = file.endsWith("tags/") && config.showTags();

      if (showTheBranch || showTheTag)
      {
        List<String> branches = urlHandler.getFiles(file, true);

        for (String branch : branches)
        {
          mainFrame.setStatusLabel("Getting ivy files for " + branch);
          getIvyFiles(branch);
        }
      }
      else if (file.endsWith("trunk/") && config.showTrunks())
      {
        // it's trunk
        mainFrame.setStatusLabel("Getting Ivy files for  for " + file);
        getIvyFiles(file);
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
  // -------------------------- OTHER METHODS --------------------------

  /** You're give the root of the repository - give a list back of any and all Ivy files found in the "build" directory. */
  private void getIvyFiles(String root) throws IOException
  {
    mainFrame.setStatusLabel("Examining " + root);

    if (!root.endsWith("/"))
    {
      root += "/";
    }

    Project      project = new Project(root);
    List<String> dirs    = urlHandler.getFiles(root, true);

    for (String dir : dirs)
    {
      if (dir.endsWith("repository/") || dir.endsWith("jdk/") || dir.endsWith("ant/"))     // it's an Ivy repository, GTF out!  You'll be in here
                                                                                           // forever otherwise
      {
        break;
      }

      if (dir.endsWith("build/"))
      {
        List<String> files = urlHandler.getFiles(dir, false);

        if (!files.isEmpty())                                                              // we found some files
        {
          for (String possibleIvyFile : files)
          {
            if (possibleIvyFile.endsWith("ivy.xml"))                                       // It's an Ivy file - parse it!
            {
              List<IvyPackage> list = ivyFiles.get(project);

              if (list == null)
              {
                list = new ArrayList<IvyPackage>();
                ivyFiles.put(project, list);
                project.setIvyFile(possibleIvyFile);
                projectUrls.getReadWriteLock().writeLock().lock();
                projectUrls.add(project);
                projectUrls.getReadWriteLock().writeLock().unlock();
                mainFrame.resizeTableColumns();
              }

              IvyPackage ivyPackage = new IvyPackage(possibleIvyFile);

              list.add(ivyPackage);
            }
          }
        }
      }
      else                                                                                 // it's not a build dir, if recursive is turned on, then
                                                                                           // drill down through the dirs
      {
        if (config.recurseDirs())
        {
          getIvyFiles(dir);
        }
      }
    }
  }
}
