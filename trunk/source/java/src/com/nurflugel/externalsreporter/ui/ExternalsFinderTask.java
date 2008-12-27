package com.nurflugel.externalsreporter.ui;

import com.nurflugel.common.ui.UiMainFrame;
import com.nurflugel.common.ui.Util;
import com.nurflugel.externalsreporter.ui.tree.BranchNode;
import com.nurflugel.BuildableProjects;
import com.nurflugel.BuildableItem;

import javax.swing.*;
import java.util.*;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNWCClient;


/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jun 2, 2008 Time: 9:15:47 PM To change this template use File | Settings | File
 * Templates.
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public class ExternalsFinderTask extends SwingWorker<Object, Object>
{
    private JProgressBar progressBar;
    private List<BranchNode> branches;
    private UiMainFrame mainFrame;

    public ExternalsFinderTask(MainFrame mainFrame, JProgressBar progressBar, List<BranchNode> branches)
    {
        this.progressBar = progressBar;
        this.branches = branches;
        this.mainFrame = mainFrame;
    }

    @Override
    protected Object doInBackground() throws Exception
    {
        System.out.println("ExternalsFinderTask.doInBackground");
        mainFrame.setBusyCursor();
        mainFrame.initializeStatusBar(0, branches.size(), 0, true);

        List<BuildableItem> list = new ArrayList<BuildableItem>();
        System.out.println("1");
        //todo make this work without the enum maps, as projects are now just urls 
//        Map<BuildableProjects, Map<String, List<External>>> dependencies =
//                new EnumMap<BuildableProjects, Map<String, List<External>>>(BuildableProjects.class);
//
////        if (!mainFrame.isTest()|| !mainFrame.getTestDataFromFile()) {
//        addToExternalsList(dependencies);
////        }
//        System.out.println("2");
        progressBar.setVisible(false);
        mainFrame.setNormalCursor();
//        ((IvyBrowserMainFrame) mainFrame).processExternals(dependencies);
        System.out.println("3");
        return list;
    }

    /** The map is a map of externals with the URL as the key, and a list of URLs to the dependent project dirs. */
    private void addToExternalsList(Map<BuildableProjects, Map<String, List<External>>> projectMap) throws SVNException
    {
        System.out.println("ExternalsFinderTask.addToExternalsList");
//        SVNWCClient wcClient = SVNClientManager.newInstance().getWCClient();
        SubversionHandler subversionHandler = new SubversionHandler(mainFrame);
        System.out.println("4");
        long startTime = new Date().getTime();
        int currentCount = 0;

        for (BranchNode branch : branches)
        {
            System.out.println("branch = " + branch);
            Map<String, List<External>> listHashMap = new HashMap<String, List<External>>();
            projectMap.put(branch.getProject(), listHashMap);
            String url = branch.getBranchUrl();
            mainFrame.addStatus("Getting externals for " + url + "        Time remaining: " +
                                Util.calculateTimeRemaining(startTime, currentCount++, branches.size()));
            progressBar.setValue(progressBar.getValue() + 1);
            System.out.println("5");
            SVNWCClient wcClient = subversionHandler.getWcClient();
            List<External> externals = subversionHandler.getExternals(url, wcClient);
            listHashMap.put(url, externals);
        }
    }
}
