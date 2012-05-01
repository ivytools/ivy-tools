package com.nurflugel.externalsreporter.ui;

import com.nurflugel.BuildableItem;
import com.nurflugel.common.ui.UiMainFrame;
import com.nurflugel.externalsreporter.ui.tree.BranchNode;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/** Created by IntelliJ IDEA. User: douglasbullard Date: Jun 2, 2008 Time: 9:15:47 PM To change this template use File | Settings | File Templates. */
@SuppressWarnings({ "UseOfSystemOutOrSystemErr" })
public class ExternalsFinderTask extends SwingWorker<Object, Object>
{
  private JProgressBar     progressBar;
  private List<BranchNode> branches;
  private UiMainFrame      mainFrame;

  public ExternalsFinderTask(ExternalsFinderMainFrame mainFrame, JProgressBar progressBar, List<BranchNode> branches)
  {
    this.progressBar = progressBar;
    this.branches    = branches;
    this.mainFrame   = mainFrame;
  }

  // -------------------------- OTHER METHODS --------------------------
  @Override
  protected Object doInBackground() throws Exception
  {
    System.out.println("ExternalsFinderTask.doInBackground");
    mainFrame.setBusyCursor();
    mainFrame.initializeStatusBar(0, branches.size(), 0, true);

    List<BuildableItem> list = new ArrayList<BuildableItem>();

    System.out.println("1");

    // todo make this work without the enum maps, as projects are now just urls
    // Map<BuildableProjects, Map<String, List<External>>> dependencies =
    // new EnumMap<BuildableProjects, Map<String, List<External>>>(BuildableProjects.class);
    //
    ////        if (!mainFrame.isTest()|| !mainFrame.getTestDataFromFile()) {
    // addToExternalsList(dependencies);
    ////        }
    // System.out.println("2");
    progressBar.setVisible(false);
    mainFrame.setNormalCursor();

    // ((IvyBrowserMainFrame) mainFrame).processExternals(dependencies);
    System.out.println("3");

    return list;
  }
}
