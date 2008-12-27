package com.nike.buildmaster.ui;

import com.nike.buildmaster.projects.Branch;
import com.nike.buildmaster.projects.BuildableProjects;
import com.nike.buildmaster.projects.MasterProjects;
import com.nike.buildmaster.projects.Targets;
import com.nike.buildmaster.ui.buildtree.BuildableItem;
import com.nike.common.ui.UiMainFrame;
import com.nike.common.ui.Util;
import com.nike.externalsreporter.ui.tree.*;
import org.jdesktop.swingworker.SwingWorker;

import javax.swing.*;
import java.io.IOException;
import java.util.Date;
import java.util.List;


/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jun 2, 2008 Time: 9:15:47 PM To change this template use File | Settings | File
 * Templates.
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public class ProjectFinderTask extends SwingWorker<Object, Object>
{
    private boolean showTargets;
    private ExternalTreeHandler treeHandler;
    private JProgressBar progressBar;
    private UiMainFrame mainFrame;

    public ProjectFinderTask(UiMainFrame mainFrame, JProgressBar progressBar, ExternalTreeHandler treeHandler, boolean showTargets)
    {
        this.progressBar = progressBar;
        this.treeHandler = treeHandler;
        this.mainFrame = mainFrame;
        this.showTargets = showTargets;
    }

    @Override
    protected Object doInBackground() throws Exception
    {
        System.out.println("ProjectFinderTask.doInBackground");
        mainFrame.initializeStatusBar(0, BuildableProjects.values().length, 0, true);
        mainFrame.setBusyCursor();
        long startTime = new Date().getTime();
        addToUi(MasterProjects.ATLAS, startTime);
        addToUi(MasterProjects.OM, startTime);
        addToUi(MasterProjects.NONE, startTime);

        progressBar.setVisible(false);
//        treeHandler.expandAll(false);

        mainFrame.addStatus("");
        mainFrame.setNormalCursor();
        mainFrame.setReady(true);

        return null;
    }

    private void addToUi(MasterProjects masterProject, long startTime) throws IOException
    {
        List<BuildableProjects> projects = BuildableProjects.getProjectsForMaster(masterProject);
        System.out.println("ExternalsFinderTask.addToUi");

        HtmlParser htmlParser = new HtmlParser();
        CheckableNode currentNode;

        if (masterProject == MasterProjects.NONE)
        {
            currentNode = treeHandler.getTopNode();
        }
        else
        {
            ProjectNode projectNode = new ProjectNode(masterProject.name());
            currentNode = projectNode;
            treeHandler.addItem(projectNode);
        }


        for (BuildableProjects project : projects)
        {
            int currentCount = progressBar.getValue();
            mainFrame.addStatus("Parsing project " + project.getProjectName() + "    Time remaining: " +
                                Util.calculateTimeRemaining(startTime, currentCount, BuildableProjects.values().length));

            ProjectNode projectNode = new ProjectNode(project);
            treeHandler.addItem(currentNode, projectNode);

            // list.add(projectNode);
            List<String> buildableUrls = htmlParser.getProjectBuildableUrls(project.getProjectBaseUrl(), mainFrame);
            progressBar.setValue(currentCount + 1);

            for (String buildableUrl : buildableUrls)
            {
                Branch branch = new Branch(getEndingLink(buildableUrl));
                BranchNode branchNode = new BranchNode(project, branch, showTargets);
                projectNode.add(branchNode);

                Targets[] targets = project.getBuildTargets();

                for (Targets target : targets)
                {
                    BuildableItem buildableItem = new BuildableItem(project, target, branch);
                    TargetNode targetNode = new TargetNode(buildableItem);
                    branchNode.add(targetNode);
                }
            }
        } // end for

        mainFrame.addStatus("");
    }


    private String getEndingLink(String buildableUrl)
    {
        String[] strings = buildableUrl.split("/");

        return strings[strings.length - 1];
    }
}
