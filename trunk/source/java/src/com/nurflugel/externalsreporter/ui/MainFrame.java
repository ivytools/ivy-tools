package com.nurflugel.externalsreporter.ui;

import com.nurflugel.BuildableProjects;
import com.nurflugel.Os;
import com.nurflugel.ProjectFinderTask;
import com.nurflugel.WebAuthenticator;
import com.nurflugel.common.ui.UiMainFrame;
import com.nurflugel.common.ui.Util;
import com.nurflugel.common.ui.Version;
import com.nurflugel.externalsreporter.ui.tree.BranchNode;
import com.nurflugel.externalsreporter.ui.tree.ExternalTreeHandler;
import com.nurflugel.ivytracker.IvyTrackerMainFrame;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Authenticator;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import static javax.swing.JFileChooser.OPEN_DIALOG;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: May 30, 2008 Time: 11:38:58 AM To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({ "CallToPrintStackTrace", "UseOfSystemOutOrSystemErr", "IOResourceOpenedButNotSafelyClosed", "CallToSystemExit" })
public class MainFrame extends JFrame implements UiMainFrame
{
  /** Use serialVersionUID for interoperability. */
  private static final long   serialVersionUID    = 7878527239782932441L;
  private boolean             getTestDataFromFile = false;  // if true, reads cannd data in from a file for fast testing
  private boolean             isTest              = false;  // if true, reads cannd data in from a file for fast testing
  private Cursor              busyCursor          = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
  private Cursor              normalCursor        = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
  private ExternalTreeHandler treeHandler         = new ExternalTreeHandler(true);
  private JButton             collapseTreeButton;
  private JButton             expandTreeButton;
  private JButton             quitButton;
  private JButton             findExternalsButton;
  private JButton             findDotButton;
  private JLabel              statusLabel;
  private JPanel              thePanel;
  private JProgressBar        progressBar;
  private JScrollPane         treeScrollPane;
  private Os                  os                  = Os.findOs(System.getProperty("os.name"));
  private Config              config              = new Config();

  public MainFrame()
  {
    initializeUi();
    go();
  }

  public static void main(String[] args)
  {
    MainFrame mainFrame = new MainFrame();
  }

  public void addStatus(String statusLine)
  {
    System.out.println(statusLine);
    statusLabel.setText(statusLine);
  }

  public void initializeStatusBar(int minimum, int maximum, int initialValue, boolean visible)
  {
    progressBar.setMinimum(minimum);
    progressBar.setMaximum(maximum);
    progressBar.setValue(initialValue);
    progressBar.setVisible(visible);
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
    findExternalsButton.addActionListener(new ActionListener()
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
    findDotButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          findDotExecutablePath();
        }
      });
  }

  private void findDotExecutablePath()
  {
    // if ((dotExecutablePath == null) || (dotExecutablePath.length() == 0)) {
    String dotExecutablePath = os.getDefaultDotPath();
    // }

    // Create a file chooser
    JFileChooser fileChooser = new JFileChooser(dotExecutablePath);

    fileChooser.setDialogTitle("Select the location of the 'dot' executable");
    fileChooser.setDialogType(OPEN_DIALOG);
    fileChooser.setDragEnabled(true);
    fileChooser.setMultiSelectionEnabled(false);
    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

    int choice = fileChooser.showDialog(this, "Select the DOT executable");

    if (choice == JFileChooser.APPROVE_OPTION)
    {
      File dotExecutableFile = fileChooser.getSelectedFile();

      if (dotExecutableFile != null)
      {
        dotExecutablePath = dotExecutableFile.getAbsolutePath();
        config.setDotExecutablePath(dotExecutablePath);
      }
      else
      {
        JOptionPane.showMessageDialog(this,
                                      "Sorry, this program can't run without the GraphViz installation.\n" + "  Please install that and try again");
        doQuitAction();
      }
    }
  }

  private void doQuitAction()
  {
    config.saveSettings();
    System.exit(0);
  }

  public void enableGoButton(boolean enabled)
  {
    findExternalsButton.setEnabled(enabled);
  }

  private void getBuildItems()
  {
    Authenticator.setDefault(new WebAuthenticator());

    ProjectFinderTask projectFinderTask = new ProjectFinderTask(this, progressBar, treeHandler, false);

    projectFinderTask.execute();
  }

  private void getExternals(List<BranchNode> branches)
  {
    Authenticator.setDefault(new WebAuthenticator());

    ExternalsFinderTask externalsFinderTask = new ExternalsFinderTask(this, progressBar, branches);

    // externalsFinderTask.execute();
    try
    {
      externalsFinderTask.doInBackground();
      treeHandler.expandAll(false);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void go()
  {
    setCursor(busyCursor);
    os.setLookAndFeel();

    try
    {
      initializeComponents();
      IvyTrackerMainFrame.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", this);
      setSize(600, 1000);
      Util.center(this);
      setVisible(true);
      addStatus("Finding available projects...");

      if (!isTest || getTestDataFromFile)
      {
        getBuildItems();
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, "The application has experienced a fatal error\n" + e.toString());
      System.exit(1);
    }

    addListeners();
    setCursor(normalCursor);
  }

  private void initializeComponents()
  {
    Container container = getContentPane();

    container.add(thePanel);
    treeScrollPane.setViewportView(treeHandler.getTree());
  }

  private void initializeUi()
  {
    setTitle("IvyFormatter v. " + Version.VERSION);
    addStatus("");
  }

  @SuppressWarnings({ "unchecked" })
  private Map<BuildableProjects, Map<String, List<External>>> loadExternalsFromFile() throws IOException, ClassNotFoundException
  {
    File              file = new File("/Users/douglasbullard/Documents/JavaStuff/Nike Subversion Projects/JavaExternals/maintenance/IvyBrowser/externals.xml");
    XStream           xstream = new XStream(new DomDriver());
    Reader            reader = new FileReader(file);
    ObjectInputStream inputStream = xstream.createObjectInputStream(reader);
    Object            object = inputStream.readObject();

    return (Map<BuildableProjects, Map<String, List<External>>>) object;
  }

  /** Take the list of externals and projects, and build up a dot file for it. */
  public void processExternals(Map<BuildableProjects, Map<String, List<External>>> dependencies)
  {
    setBusyCursor();

    try
    {
      if (isTest)
      {
        // if (getTestDataFromFile) {
        // dependencies = loadExternalsFromFile();
        // } else {
        saveExternalsToFile(dependencies);

        // }
      }

      OutputHandler outputHandler = new OutputHandler(this);
      File          dotExecutable = config.getDotExecutablePath();
      File          dotFile       = outputHandler.writeDotFile(dependencies);
      File          imageFile     = outputHandler.launchDot(dotFile, dotExecutable);

      outputHandler.viewResultingFile(imageFile);
      dotFile.deleteOnExit();
      setNormalCursor();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
      // } catch (ClassNotFoundException e) {
      // e.printStackTrace();
    }
  }

  private void saveExternalsToFile(Map<BuildableProjects, Map<String, List<External>>> dependencies) throws IOException
  {  // todo fix

    File               file = new File("/Users/douglasbullard/Documents/JavaStuff/Nike Subversion Projects/JavaExternals/maintenance/IvyBrowser/externals.xml");
    XStream            xstream = new XStream();
    Writer             fileWriter = new FileWriter(file);
    ObjectOutputStream out = xstream.createObjectOutputStream(fileWriter);

    out.writeObject(dependencies);
    out.close();
  }

  @SuppressWarnings({ "OverlyBroadCatchBlock" })
  private void startBuildAction()
  {
    setCursor(busyCursor);
    System.out.println("\n\n\nHere are the dirs to be included:");
    addStatus("Fetching externals information...");

    // List<ProjectNode> checkedProjects = treeHandler.getCheckedProjects();
    List<BranchNode> checkedBranches = treeHandler.getCheckedBranches();
    // List<TargetNode>  checkedTargets  = treeHandler.getCheckedTargets();

    getExternals(checkedBranches);
    setCursor(normalCursor);
  }

  /**  */
  public Os getOs()
  {
    return os;
  }

  @SuppressWarnings({ "BooleanMethodNameMustStartWithQuestion" })
  public boolean getTestDataFromFile()
  {
    return getTestDataFromFile;
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
    findExternalsButton.setEnabled(isReady);
  }

  public void showSevereError(String message, Exception e)
  {
    // todo
  }

  public boolean isTest()
  {
    return isTest;
  }

  public Config getConfig()
  {
    return config;
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
   * @noinspection  ALL
   */
  private void $$$setupUI$$$()
  {
    thePanel = new JPanel();
    thePanel.setLayout(new GridBagLayout());

    final JLabel label1 = new JLabel();

    label1.setFont(new Font("Arial", Font.BOLD, 28));
    label1.setText("Enterprise Services' Externals Finder");

    GridBagConstraints gbc;

    gbc         = new GridBagConstraints();
    gbc.gridx   = 0;
    gbc.gridy   = 0;
    gbc.weightx = 1.0;
    thePanel.add(label1, gbc);

    final JPanel panel1 = new JPanel();

    panel1.setLayout(new BorderLayout(0, 0));
    gbc         = new GridBagConstraints();
    gbc.gridx   = 0;
    gbc.gridy   = 1;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill    = GridBagConstraints.BOTH;
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
    collapseTreeButton = new JButton();
    collapseTreeButton.setText("Collapse tree");
    collapseTreeButton.setMnemonic('C');
    collapseTreeButton.setDisplayedMnemonicIndex(0);
    gbc         = new GridBagConstraints();
    gbc.gridx   = 0;
    gbc.gridy   = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill    = GridBagConstraints.HORIZONTAL;
    panel4.add(collapseTreeButton, gbc);
    expandTreeButton = new JButton();
    expandTreeButton.setText("Expand tree");
    expandTreeButton.setMnemonic('E');
    expandTreeButton.setDisplayedMnemonicIndex(0);
    gbc         = new GridBagConstraints();
    gbc.gridx   = 0;
    gbc.gridy   = 1;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill    = GridBagConstraints.HORIZONTAL;
    panel4.add(expandTreeButton, gbc);
    findDotButton = new JButton();
    findDotButton.setText("Find DOT executable");
    findDotButton.setMnemonic('D');
    findDotButton.setDisplayedMnemonicIndex(5);
    gbc         = new GridBagConstraints();
    gbc.gridx   = 1;
    gbc.gridy   = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill    = GridBagConstraints.HORIZONTAL;
    panel4.add(findDotButton, gbc);
    findExternalsButton = new JButton();
    findExternalsButton.setEnabled(false);
    findExternalsButton.setText("Find Externals for selected branches");
    findExternalsButton.setMnemonic('F');
    findExternalsButton.setDisplayedMnemonicIndex(0);
    gbc         = new GridBagConstraints();
    gbc.gridx   = 1;
    gbc.gridy   = 1;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill    = GridBagConstraints.HORIZONTAL;
    panel4.add(findExternalsButton, gbc);
    quitButton = new JButton();
    quitButton.setText("Quit");
    quitButton.setMnemonic('Q');
    quitButton.setDisplayedMnemonicIndex(0);
    gbc         = new GridBagConstraints();
    gbc.gridx   = 1;
    gbc.gridy   = 2;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill    = GridBagConstraints.HORIZONTAL;
    panel4.add(quitButton, gbc);

    final JPanel panel5 = new JPanel();

    panel5.setLayout(new GridBagLayout());
    panel1.add(panel5, BorderLayout.CENTER);
    panel5.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "All projects and all branches"));
    treeScrollPane = new JScrollPane();
    gbc            = new GridBagConstraints();
    gbc.gridx      = 0;
    gbc.gridy      = 0;
    gbc.weightx    = 1.0;
    gbc.weighty    = 1.0;
    gbc.fill       = GridBagConstraints.BOTH;
    panel5.add(treeScrollPane, gbc);
  }

  /** @noinspection  ALL */
  public JComponent $$$getRootComponent$$$()
  {
    return thePanel;
  }
}
