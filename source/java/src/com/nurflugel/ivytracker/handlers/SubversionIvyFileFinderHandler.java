package com.nurflugel.ivytracker.handlers;

import ca.odell.glazedlists.EventList;
import com.nurflugel.externalsreporter.ui.HtmlHandler;
import com.nurflugel.externalsreporter.ui.ScanExternalsTask;
import com.nurflugel.ivybrowser.domain.IvyFile;
import com.nurflugel.ivytracker.IvyTrackerMainFrame;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Subversion implementation (is there going to be another? if the IvyFileFinderHandler */
@SuppressWarnings({ "CallToPrintStackTrace" })
public class SubversionIvyFileFinderHandler extends IvyFileFinderHandler
{
  private final IvyTrackerMainFrame mainFrame;

  /** Map of Ivy files keyed by project URL. */
  private final Map<String, List<IvyFile>> ivyFiles;
  private final EventList<String>          projectUrls;
  private final String[]                   repositories;
  private HtmlHandler                      urlHandler;
  private boolean                          showBranches = true;
  private boolean                          showTags     = true;
  private boolean                          showTrunks   = true;

  public SubversionIvyFileFinderHandler(IvyTrackerMainFrame        mainFrame,
                                        Map<String, List<IvyFile>> ivyFiles,
                                        EventList<String>          projectUrls,
                                        String...                  repositories)
  {
    this.mainFrame    = mainFrame;
    this.ivyFiles     = ivyFiles;
    this.projectUrls  = projectUrls;
    this.repositories = repositories;
    urlHandler        = new HtmlHandler();
  }

  @Override
  protected Object doInBackground() throws Exception
  {
    doIt();

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

              if (isChildRoot)
              {
                if (file.endsWith("/"))
                {
                  mainFrame.setStatusLabel("Getting ivy files for " + file);
                  getIvyFiles(file);
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
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  /** You're give the root of the repository - give a list back of any and all Ivy files found in the "build" directory. */
  private void getIvyFiles(String root) throws IOException
  {
    if (!root.endsWith("/"))
    {
      root += "/";
    }

    List<String> files = urlHandler.getFiles(root + "build", false);

    if (!files.isEmpty())  // we found some files
    {
      for (String file : files)
      {
        if (file.endsWith("ivy.xml"))
        {
          List<IvyFile> list = ivyFiles.get(root);

          if (list == null)
          {
            list = new ArrayList<IvyFile>();
            ivyFiles.put(root, list);
            projectUrls.add(root);
          }

          IvyFile ivyPackage = new IvyFile(file);

          list.add(ivyPackage);
        }
      }
    }
  }

  /** this should be the base dkdlkdjldkjdlkjdlkdjldk (branch, tag, or trunk). */
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
          mainFrame.setStatusLabel("Getting ivy files for " + branch);
          getIvyFiles(branch);
        }
      }
      else if (file.endsWith("trunk/") && showTrunks)
      {
        // it's trunk
        mainFrame.setStatusLabel("Getting externals for " + file);
        getIvyFiles(file);
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
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
