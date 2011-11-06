package com.nurflugel.ivybuilder.handlers;

import com.nurflugel.ivybrowser.domain.IvyRepositoryItem;
import com.nurflugel.ivybuilder.BuilderMainFrame;
import javax.swing.*;
import java.io.File;
import java.util.List;

@SuppressWarnings({
                    "CallToPrintStackTrace", "IOResourceOpenedButNotSafelyClosed", "UseOfSystemOutOrSystemErr",
                    "AssignmentToCollectionOrArrayFieldFromParameter"
                  })
public class FileHandler extends SwingWorker<Object, Object>
{
  public static final String      SVN           = ".svn";
  private List<IvyRepositoryItem> ivyPackages;
  private BuilderMainFrame        mainFrame;
  private File                    repositoryDir;

  // --------------------------- CONSTRUCTORS ---------------------------
  public FileHandler(BuilderMainFrame mainFrame, File repositoryDir, List<IvyRepositoryItem> ivyPackages)
  {
    this.mainFrame     = mainFrame;
    this.ivyPackages   = ivyPackages;
    this.repositoryDir = repositoryDir;
  }

  // -------------------------- OTHER METHODS --------------------------
  @Override
  public Object doInBackground()
  {
    findAllIvyPackages();
    mainFrame.showNormal();

    return null;
  }

  public void findAllIvyPackages()
  {
    File[] orgDirs = repositoryDir.listFiles();

    for (File orgDir : orgDirs)
    {
      if (orgDir.isDirectory() && !orgDir.getName().equals(SVN))
      {
        findModules(orgDir);
      }
    }
  }

  private void findModules(File orgDir)
  {
    File[] moduleDirs = orgDir.listFiles();

    for (File moduleDir : moduleDirs)
    {
      if (moduleDir.isDirectory() && !moduleDir.getName().equals(SVN))
      {
        findRevs(orgDir, moduleDir);
      }
    }
  }

  private void findRevs(File orgDir, File moduleDir)
  {
    File[] revDirs = moduleDir.listFiles();

    for (File revDir : revDirs)
    {
      if (revDir.isDirectory() && !revDir.getName().equals(SVN))
      {
        IvyRepositoryItem ivyPackage = new IvyRepositoryItem(orgDir.getName(), moduleDir.getName(), revDir.getName(), repositoryDir);

        ivyPackages.add(ivyPackage);
      }
    }
  }
}
