package com.nike.buildmaster.ui;

import com.nike.buildmaster.Os;
import com.nike.buildmaster.WebAuthenticator;
import com.nike.buildmaster.handlers.SubversionCheckoutTask;
import com.nike.buildmaster.handlers.SubversionHandler;
import com.nike.buildmaster.projects.BuildableProjects;
import com.nike.buildmaster.ui.buildtree.BuildableItem;
import com.nike.common.ui.UiMainFrame;
import com.nike.common.ui.Util;
import com.nike.common.ui.Version;
import com.nike.externalsreporter.ui.tree.ExternalTreeHandler;
import com.nike.externalsreporter.ui.tree.ProjectBranchItem;
import com.nike.externalsreporter.ui.tree.TargetNode;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.tmatesoft.svn.core.SVNURL;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Authenticator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The main class used to run the project.
 * <p/>
 * The project can run in UI or "headless" mode.  The headless mode has no UI, and reads in a config file written by the UI mode.
 */
@SuppressWarnings({"CallToPrintStackTrace", "UseOfSystemOutOrSystemErr", "IOResourceOpenedButNotSafelyClosed", "CallToSystemExit"})
public class MainFrame extends JFrame implements UiMainFrame
{
    private static final long serialVersionUID = 7878527239782932441L;
    private JPanel thePanel;
    private JButton quitButton;
    private JButton startBuildNowButton;
    private JScrollPane treeScrollPane;
    private JButton expandTreeButton;
    private JButton collapseTreeButton;
    private JLabel statusLabel;
    private Os os = Os.findOs(System.getProperty("os.name"));
    private ExternalTreeHandler treeHandler = new ExternalTreeHandler(false);
    private JProgressBar progressBar;
    private JButton createHeadlessBuildButton;
    private JButton selectConfigDirButton;
    private boolean isTest = false;
    private boolean getTestData = false;
    private Config config = new Config();
    private boolean isHeadlessBuild;

    public MainFrame(String[] args)
    {
        isHeadlessBuild = false;

        if (args.length > 0)
        {

            if (args[0].equalsIgnoreCase("headless"))
            {
                System.out.println("Should do headless build");
                isHeadlessBuild = true;
            }
        }

        if (isHeadlessBuild)
        {
            try
            {
                System.out.println("About to do headless build");
                doHeadlessBuild();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            System.out.println("Doing a UI build");
            doUiBuild();
        }
    }

    /** Called via the UI to select the dir used to do the builds. */
    private File selectTagSnapshotsDir()
    {
        File configDir = config.getTagSnapshotsDir();
        JFileChooser fileChooser = new JFileChooser("Select dir for tag snapshots");

        if (configDir != null)
        {
            fileChooser.setCurrentDirectory(configDir);
        }

        fileChooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);

        int action = fileChooser.showDialog(this, "Put tag snapshots file in this dir");

        if (action == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = fileChooser.getSelectedFile();
            config.setTagSnapshotsDir(selectedFile);

            return selectedFile;
        }

        return null;
    }

    /** Do a build right from the UI */
    private void doUiBuild()
    {
        setBusyCursor();

        os.setLookAndFeel();

        try
        {
            initializeComponents();
            com.nike.ivytracker.MainFrame.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", this);
            setSize(600, 800);
            Util.center(this);
            setVisible(true);
            findBuildItems();
        }
        catch (Exception e)
        {
            handleFatalException(e);
        }

        Util.center(this);
        addListeners();
        setNormalCursor();
    }

    /** Do a build from the headless mode. */
    private void doHeadlessBuild() throws IOException, ClassNotFoundException
    {
        File tagSnapshotsDir = config.getTagSnapshotsDir();
        XStream xstream = new XStream(new DomDriver());
        Reader reader = new FileReader(new File(tagSnapshotsDir, "buildConfig.xml"));
        ObjectInputStream inputStream = xstream.createObjectInputStream(reader);
        Object object = inputStream.readObject();
        List<BuildableItem> buildableItems = (List<BuildableItem>) object;
        SubversionHandler subversionHandler = new SubversionHandler(this);

        buildTargets(subversionHandler, buildableItems);
    }

    @SuppressWarnings({"OverlyBroadCatchBlock"})
    private void startBuildAction()
    {
        setBusyCursor();
        System.out.println("\n\n\nHere are the builds to be built:");
        addStatus("Creating builds...");

        SubversionHandler subversionHandler = new SubversionHandler(this);
        Map<String, List<TargetNode>> buildMap = new HashMap<String, List<TargetNode>>();
        List<TargetNode> checkedTargets = getSelectedBuildTargets(buildMap);
        List<BuildableItem> buildableItems = getBuildItemsFromTargetNodes(checkedTargets);

        buildTargets(subversionHandler, buildableItems);

        setNormalCursor();
    }

    /** Create the config file used for the headless builds */
    private void createConfigFile() throws IOException
    {
        File tagSnapshotsDir = config.getTagSnapshotsDir();

        if (tagSnapshotsDir.getAbsolutePath().length() == 0)
        {
            tagSnapshotsDir = selectTagSnapshotsDir();
        }

        Map<String, List<TargetNode>> buildMap = new HashMap<String, List<TargetNode>>();
        List<TargetNode> buildList = getSelectedBuildTargets(buildMap);
        List<BuildableItem> buildItems = getBuildItemsFromTargetNodes(buildList);

        if (buildItems.size() > 0)//don't create the config file if there are no files to build
        {
            XStream xstream = new XStream();
            File file = new File(tagSnapshotsDir, "buildConfig.xml");
            Writer fileWriter = new FileWriter(file);
            ObjectOutputStream out = xstream.createObjectOutputStream(fileWriter);
            out.writeObject(buildItems);
            out.close();
            JOptionPane.showMessageDialog(this, "Headless config file " + file.getAbsolutePath() + " created.");
        }
    }

    /** Get a list of buildable items from all the target nodes */
    private List<BuildableItem> getBuildItemsFromTargetNodes(List<TargetNode> buildList)
    {
        List<BuildableItem> buildableItems = new ArrayList<BuildableItem>();

        for (TargetNode targetNode : buildList)
        {
            buildableItems.add(targetNode.getBuildableItem());
        }

        return buildableItems;
    }

    private void handleFatalException(Exception e)
    {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "The application has experienced a fatal error\n" + e.toString());
        System.exit(1);
    }

    /** build all the targets.  To make things nice, just make a single tag for all targets of a branch */
    private void buildTargets(SubversionHandler subversionHandler, List<BuildableItem> checkedTargets)
    {
        File tagSnapshotsDir = config.getTagSnapshotsDir();

        if (tagSnapshotsDir == null)
        {
            tagSnapshotsDir = selectTagSnapshotsDir();
        }

        Map<ProjectBranchItem, List<BuildableItem>> buildMap = new HashMap<ProjectBranchItem, List<BuildableItem>>();

        for (BuildableItem target : checkedTargets)
        {
            ProjectBranchItem projectBranchItem = new ProjectBranchItem(target.getProject(), target.getBranch());
            List<BuildableItem> list = buildMap.get(projectBranchItem);

            if (list == null)
            {
                list = new ArrayList<BuildableItem>();
                buildMap.put(projectBranchItem, list);
            }

            list.add(target);
        }

        for (ProjectBranchItem project : buildMap.keySet())
        {
            doBuildsForProjectBranch(tagSnapshotsDir, subversionHandler, buildMap, checkedTargets, project);
        }
    }

    /** For each branch, do a checkout */
    private void doBuildsForProjectBranch(File snapshotRoot, SubversionHandler subversionHandler,
                                          Map<ProjectBranchItem, List<BuildableItem>> buildMap, List<BuildableItem> checkedTargets,
                                          ProjectBranchItem projectName)
    {
        List<BuildableItem> buildableItems = buildMap.get(projectName);
        BuildableItem item = buildableItems.get(0);
        SVNURL svnurl = subversionHandler.makeTag(item);
        SubversionCheckoutTask checkoutTask = new SubversionCheckoutTask(snapshotRoot, this, svnurl, checkedTargets, projectName, buildMap);

        // have to do this because a headless build can't run this as a thread - caues problems
        if (isHeadlessBuild)
        {
            try
            {
                checkoutTask.doInBackground();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            checkoutTask.execute();
        }
    }

    /** Get the bulid targets that are selected */
    private List<TargetNode> getSelectedBuildTargets(Map<String, List<TargetNode>> buildMap)
    {
        List<TargetNode> checkedTargets = treeHandler.getCheckedTargets();
        checkedTargets = confirmBuildInformation(checkedTargets);

        // build up a map of builds by project
        for (TargetNode item : checkedTargets)
        {
            BuildableItem buildItem = item.getBuildableItem();
            BuildableProjects project = buildItem.getProject();

            List<TargetNode> targetList;

            if (buildMap.containsKey(project.getProjectBaseUrl()))
            {
                targetList = buildMap.get(project.getProjectBaseUrl());
            }
            else
            {
                targetList = new ArrayList<TargetNode>();
                buildMap.put(project.getProjectBaseUrl(), targetList);
            }

            targetList.add(item);
        }

        return checkedTargets;
    }


    public boolean isHeadlessBuild()
    {
        return isHeadlessBuild;
    }

    /** find all the build items */
    private void findBuildItems()
    {
        Authenticator.setDefault(new WebAuthenticator());

        ProjectFinderTask projectFinderTask = new ProjectFinderTask(this, progressBar, treeHandler, true);

        projectFinderTask.execute();
        addStatus("");
    }


    /**
     * Takes the list of build items, and shows the list for confirmation, and lets the user change the suggested tag names.
     *
     * @return the list of confirmed items to build
     */
    private List<TargetNode> confirmBuildInformation(List<TargetNode> checkedTargets)
    {
        BuildConfirmationDialog confirmationDialog = new BuildConfirmationDialog(checkedTargets);


        if (confirmationDialog.cancelBuild())
        {
            System.out.println("Build is being cancelled");

            return new ArrayList<TargetNode>();
        }
        else
        {
            return confirmationDialog.getCheckedTargets();
        }
    }

    public void addStatus(String statusLine)
    {
        System.out.println(statusLine);
        if (!isHeadlessBuild)
        {
            statusLabel.setText(statusLine);
        }
    }

    private void addListeners()
    {
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                super.windowClosing(e);
                doQuitAction();
            }
        });
        quitButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doQuitAction();

            }
        });
        startBuildNowButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                startBuildAction();
            }
        });
        expandTreeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                treeHandler.expandAll(true);
            }
        });
        collapseTreeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                treeHandler.expandAll(false);
            }
        });
        selectConfigDirButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                selectTagSnapshotsDir();
            }
        });
        createHeadlessBuildButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {

                try
                {
                    createConfigFile();
                }
                catch (IOException e1)
                {
                    handleFatalException(e1);
                }
            }
        });
    }


    public ExternalTreeHandler getTreeHandler()
    {
        return treeHandler;
    }


    private void initializeComponents()
    {
        setTitle("BuildMaster v. " + Version.VERSION);
        Container container = getContentPane();
        container.add(thePanel);
        treeScrollPane.setViewportView(treeHandler.getTree());
    }

    public void enableGoButton(boolean enabled)
    {
        if (!isHeadlessBuild)
        {
            startBuildNowButton.setEnabled(enabled);
            createHeadlessBuildButton.setEnabled(enabled);
        }
    }


    /**  */
    public Os getOs()
    {
        return os;
    }

    public void initializeStatusBar(int minimum, int maximum, int initialValue, boolean visible)
    {
        progressBar.setMinimum(minimum);
        progressBar.setMaximum(maximum);
        progressBar.setValue(initialValue);
        progressBar.setVisible(visible);
    }

    public boolean isTest()
    {
        return isTest;
    }

    public boolean getTestDataFromFile()
    {
        return getTestData;
    }

    public void setBusyCursor()
    {
        setCursor(Util.busyCursor);
    }

    public void setNormalCursor()
    {
        setCursor(Util.normalCursor);
    }

    public void setReady(boolean isReady)
    {
        enableGoButton(true);
    }

    public void showSevereError(String message, Exception e)
    {
        if (!isHeadlessBuild)
        {
            JOptionPane.showMessageDialog(this, message);
        }

        e.printStackTrace();
        error("Error making tag", e);
    }

    /*
    * Displays error information.
    */
    private void error(String message, Exception e)
    {
        String text = message + ((e != null)
                                 ? (": " + e.getMessage())
                                 : "");
        System.err.println(text);

        addStatus(text);
    }

    private void doQuitAction()
    {
        config.saveSettings();

        System.exit(0);
    }


    public static void main(String[] args)
    {

        for (String arg : args)
        {
            System.out.println("arg = " + arg);
        }

        MainFrame mainFrame = new MainFrame(args);
    }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer >>> IMPORTANT!! <<< DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        thePanel = new JPanel();
        thePanel.setLayout(new GridBagLayout());
        final JLabel label1 = new JLabel();
        label1.setFont(new Font("Arial", Font.BOLD, 28));
        label1.setText("Enterprise Services' BuildMaster");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        thePanel.add(label1, gbc);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        thePanel.add(panel1, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(0, 0));
        panel1.add(panel2, BorderLayout.SOUTH);
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        panel2.add(progressBar, BorderLayout.CENTER);
        statusLabel = new JLabel();
        statusLabel.setText("Label");
        panel2.add(statusLabel, BorderLayout.SOUTH);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout(0, 0));
        panel2.add(panel3, BorderLayout.NORTH);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        panel3.add(panel4, BorderLayout.CENTER);
        startBuildNowButton = new JButton();
        startBuildNowButton.setEnabled(false);
        startBuildNowButton.setRolloverEnabled(false);
        startBuildNowButton.setText("Build selected targets now");
        startBuildNowButton.setMnemonic('B');
        startBuildNowButton.setDisplayedMnemonicIndex(0);
        startBuildNowButton.setToolTipText("Run all the build targets now.\nThe UI will be active until all builds are done.\n");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(startBuildNowButton, gbc);
        createHeadlessBuildButton = new JButton();
        createHeadlessBuildButton.setEnabled(false);
        createHeadlessBuildButton.setText("Create headless configuration for later build");
        createHeadlessBuildButton.setToolTipText("Create a configuration file, shut down the UI,\nand let the scheduler run the build the build in \"headless\" mode.");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(createHeadlessBuildButton, gbc);
        quitButton = new JButton();
        quitButton.setText("Quit");
        quitButton.setMnemonic('Q');
        quitButton.setDisplayedMnemonicIndex(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(quitButton, gbc);
        collapseTreeButton = new JButton();
        collapseTreeButton.setText("Collapse tree");
        collapseTreeButton.setMnemonic('C');
        collapseTreeButton.setDisplayedMnemonicIndex(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(collapseTreeButton, gbc);
        expandTreeButton = new JButton();
        expandTreeButton.setText("Expand tree");
        expandTreeButton.setMnemonic('E');
        expandTreeButton.setDisplayedMnemonicIndex(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(expandTreeButton, gbc);
        selectConfigDirButton = new JButton();
        selectConfigDirButton.setText("Select dir for config file");
        selectConfigDirButton.setToolTipText("You need to tell the app where the configuration file goes for headless builds");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(selectConfigDirButton, gbc);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridBagLayout());
        panel1.add(panel5, BorderLayout.CENTER);
        panel5.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "All projects, all branches, all targets"));
        treeScrollPane = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel5.add(treeScrollPane, gbc);
    }

    /** @noinspection ALL */
    public JComponent $$$getRootComponent$$$()
    {
        return thePanel;
    }
}
