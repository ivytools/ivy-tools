package com.nurflugel.ivytracker;

import ca.odell.glazedlists.EventList;
import com.nurflugel.common.ui.UiMainFrame;
import com.nurflugel.externalsreporter.ui.HtmlHandler;
import com.nurflugel.ivybrowser.domain.IvyPackage;
import org.tmatesoft.svn.core.SVNException;
import javax.swing.*;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/** Background task to get subversion externals so UI can refresh asynchronously. */
@SuppressWarnings({ "CallToPrintStackTrace", "UseOfSystemOutOrSystemErr" })
public class ScanSubversionsRepositoriesTask extends SwingWorker
{
  private Set<String>           repositoryUrls;
  private UiMainFrame           mainFrame;
  private boolean               shallowSearch;
  private EventList<IvyPackage> foundIvyFilesList;
  private HtmlHandler           urlHandler           = new HtmlHandler();
  private boolean               showBranches;
  private boolean               showTags;
  private boolean               showTrunks;
  private boolean               isSelectAllExternals;
  private boolean               isSelectAllProjects;

  public ScanSubversionsRepositoriesTask(Set<String> repositoryUrls, UiMainFrame mainFrame, boolean isShallowSearch, boolean showBranches,
                                         boolean showTags, boolean showTrunks, EventList<IvyPackage> foundIvyFilesList)
  {
    this.repositoryUrls    = repositoryUrls;
    this.mainFrame         = mainFrame;
    shallowSearch          = isShallowSearch;
    this.foundIvyFilesList = foundIvyFilesList;
    this.showBranches      = showBranches;
    this.showTags          = showTags;
    this.showTrunks        = showTrunks;
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
    for (String repositoryUrl : repositoryUrls)
    {
      try
      {
        findIvyFiles(repositoryUrl, !shallowSearch);
      }
      catch (Exception e)
      {
        e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
      }
    }

    // mainFrame.processResults();todo
    return null;
  }

  private void findIvyFiles(String repositoryUrl, boolean isRecursive) throws IOException, SVNException
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
            mainFrame.setStatusLabel("Getting externals for " + branch);
            getIvyFiles(repositoryUrl, file);

            if (isRecursive)
            {
              findIvyFiles(branch, isRecursive);
            }
          }
        }
        else if (file.endsWith("trunk/") && showTrunks)
        {
          // it's trunk
          mainFrame.setStatusLabel("Getting externals for " + file);
          getIvyFiles(repositoryUrl, file);

          if (isRecursive)
          {
            findIvyFiles(file, isRecursive);
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
            mainFrame.setStatusLabel("Getting externals for " + file);
            getIvyFiles(repositoryUrl, file);
            findIvyFiles(file, isRecursive);
          }
        }
      }
    }
  }

  /** Find any Ivy files in the build directory. */
  private void getIvyFiles(String repositoryUrl, String file) throws IOException
  {
    List<String> files = urlHandler.getFiles(repositoryUrl + "/" + file + "/" + "build", true);

    for (String buildFile : files)
    {
      if (buildFile.endsWith("ivy.xml"))
      {
        handleFoundIvyFile(buildFile);
      }
    }
  }

  /**
   * Great - now we found an Ivy file, handle it. We need to log what application this is, and the Ivy file(s). Yes, some projects will have more than
   * one ivy file (bad precedent, but it's happened)
   */
  private void handleFoundIvyFile(String ivyFile) {}

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
