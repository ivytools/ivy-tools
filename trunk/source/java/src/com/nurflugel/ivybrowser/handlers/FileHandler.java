package com.nurflugel.ivybrowser.handlers;

import com.nurflugel.ivybrowser.domain.IvyRepositoryItem;
import com.nurflugel.ivybrowser.ui.BuilderMainFrame;

import javax.swing.*;
import java.io.File;
import java.util.List;


@SuppressWarnings({"CallToPrintStackTrace", "IOResourceOpenedButNotSafelyClosed", "UseOfSystemOutOrSystemErr",
        "AssignmentToCollectionOrArrayFieldFromParameter"})
public class FileHandler extends SwingWorker<Object, Object>
{
    private List<IvyRepositoryItem> ivyPackages;
    private BuilderMainFrame mainFrame;
    private File repositoryDir;

    // --------------------------- CONSTRUCTORS ---------------------------

    public FileHandler(BuilderMainFrame mainFrame,
                       File repositoryDir,
                       List<IvyRepositoryItem> ivyPackages)
    {
        this.mainFrame = mainFrame;
        this.ivyPackages = ivyPackages;
        this.repositoryDir = repositoryDir;
    }

    // ------------------------ INTERFACE METHODS ------------------------

    // --------------------- Interface Runnable ---------------------

    public Object doInBackground()
    {
        findAllIvyPackages();
        mainFrame.showNormal();
        return null;
    }

    // -------------------------- OTHER METHODS --------------------------

    public void findAllIvyPackages()
    {
        File[] orgDirs = repositoryDir.listFiles();

        for (File orgDir : orgDirs)
        {

            if (orgDir.isDirectory() && !orgDir.getName().equals(".svn"))
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

            if (moduleDir.isDirectory() && !moduleDir.getName().equals(".svn"))
            {
                findRevs(orgDir, moduleDir);
            }
        }
    }

    private void findRevs(File orgDir,
                          File moduleDir)
    {
        File[] revDirs = moduleDir.listFiles();

        for (File revDir : revDirs)
        {

            if (revDir.isDirectory() && !revDir.getName().equals(".svn"))
            {
                IvyRepositoryItem ivyPackage =
                        new IvyRepositoryItem(orgDir.getName(), moduleDir.getName(), revDir.getName(), repositoryDir);
                ivyPackages.add(ivyPackage);
            }
        }
    }
}