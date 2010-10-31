package com.nurflugel.ivytracker.handlers;

import ca.odell.glazedlists.EventList;
import com.nurflugel.ivybrowser.domain.IvyKey;
import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivytracker.IvyTrackerMainFrame;
import com.nurflugel.ivytracker.domain.Project;
import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class goes through the HTML-based Ivy repository and finds any Ivy files in the build dir of anything in trunk, a branch, or a tag.
 * Directories not conforming to this standard may be missed. //todo get rid of this and use IvyBrowser handler instead
 */
@SuppressWarnings({ "CallToPrintStackTrace", "IOResourceOpenedButNotSafelyClosed", "UseOfSystemOutOrSystemErr" })
public class HtmlIvyHandler extends SwingWorker<Object, Object>
{
  private IvyTrackerMainFrame                  mainFrame;
  private EventList<Project>                   projects;
  private EventList<IvyPackage>                repositoryList;
  private Set<String>                          missingIvyFiles;
  private final Map<Project, List<IvyPackage>> projectIvyFilesMap;

  // --------------------------- CONSTRUCTORS ---------------------------
  public HtmlIvyHandler(IvyTrackerMainFrame mainFrame, EventList<Project> projects, EventList<IvyPackage> repositoryList, Set<String> missingIvyFiles,
                        Map<Project, List<IvyPackage>> projectIvyFilesMap)
  {
    this.mainFrame          = mainFrame;
    this.projects           = projects;
    this.repositoryList     = repositoryList;
    this.missingIvyFiles    = missingIvyFiles;
    this.projectIvyFilesMap = projectIvyFilesMap;
  }

  // -------------------------- OTHER METHODS --------------------------
  @Override
  @SuppressWarnings({ "unchecked" })
  public Object doInBackground()
  {
    Map<IvyKey, IvyPackage> keyMap = generateIvyFilesMap();

    touchAllUsedPackages(keyMap);
    generateReportOfUnusedIvyFiles();
    mainFrame.showNormal();
    mainFrame.doneWithAnalysis();

    return null;
  }

  private Map<IvyKey, IvyPackage> generateIvyFilesMap()
  {
    Map<IvyKey, IvyPackage> packages = new HashMap<IvyKey, IvyPackage>(repositoryList.size());

    for (IvyPackage ivyPackage : repositoryList)
    {
      packages.put(ivyPackage.getKey(), ivyPackage);
    }

    return packages;
  }

  /** Walk through all the project ivy files and down the dependency trees, touching anything used. */
  private void touchAllUsedPackages(Map<IvyKey, IvyPackage> keyMap)
  {
    System.out.println("HtmlHandler.touchAllUsedPackages");

    for (Project project : projects)
    {
      List<IvyPackage> packages = projectIvyFilesMap.get(project);

      for (IvyPackage ivyPackage : packages)
      {
        touchDependency(ivyPackage, keyMap);
      }
    }
  }

  private void touchDependency(IvyPackage ivyFile, Map<IvyKey, IvyPackage> keyMap)
  {
    ivyFile.touch();

    List<IvyPackage> dependencies = ivyFile.getDependencies();

    for (IvyPackage dependency : dependencies)
    {
      IvyKey     key          = dependency.getKey();
      IvyPackage childIvyFile = keyMap.get(key);

      // how can this be null???
      if (childIvyFile != null)
      {
        touchDependency(childIvyFile, keyMap);
      }
      else
      {
        missingIvyFiles.add(new StringBuilder().append(key).append(" is required by ").append(ivyFile.getKey()).toString());
      }
    }
  }

  private void generateReportOfUnusedIvyFiles()
  {
    System.out.println("\n\n\n\n\nHere are the ivy files that have no usages:");

    for (IvyPackage ivyFile : repositoryList)  // System.out.println(ivyFile.getKey()+ " count="+ivyFile.getCount());
    {
      if (ivyFile.getCount() == 0)
      {
        System.out.println("====>    " + ivyFile.getKey() + " is not used");
      }
    }
  }
}
