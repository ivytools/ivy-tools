package com.nurflugel.externalsreporter.ui;

import com.nurflugel.BuildableProjects;
import com.nurflugel.Os;
import com.nurflugel.WebAuthenticator;
import com.nurflugel.common.ui.UiMainFrame;
import com.nurflugel.common.ui.Util;
import com.nurflugel.common.ui.Version;
import static com.nurflugel.common.ui.Util.*;
import com.nurflugel.externalsreporter.ui.tree.BranchNode;
import com.nurflugel.externalsreporter.ui.tree.ExternalTreeHandler;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.commons.lang.StringUtils;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Authenticator;
import java.util.*;
import java.util.List;
import javax.swing.*;
import static javax.swing.JFileChooser.OPEN_DIALOG;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: May 30, 2008 Time: 11:38:58 AM To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({ "CallToPrintStackTrace", "UseOfSystemOutOrSystemErr", "IOResourceOpenedButNotSafelyClosed", "CallToSystemExit" })
public class MainFrame extends JFrame implements UiMainFrame
{
  /** Use serialVersionUID for interoperability. */
  private static final long   serialVersionUID                  = 7878527239782932441L;
  private boolean             getTestDataFromFile;  // if true, reads canned data in from a file for fast testing
  private boolean             isTest;               // if true, reads canned data in from a file for fast testing
  private Cursor              busyCursor                        = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
  private Cursor              normalCursor                      = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
  private ExternalTreeHandler treeHandler                       = new ExternalTreeHandler(true);
  private JButton             quitButton;
  private JButton             findExternalsButton;
  private JButton             findDotButton;
  private JLabel              statusLabel;
  private JPanel              thePanel;
  private JProgressBar        progressBar;
  private JScrollPane         treeScrollPane;
  private JButton             addRepositoryButton;
  private JButton             helpButton;
  private JRadioButton        deepRecursiveSlowRadioButton;
  private JRadioButton        shallowBranchTagsTrunkRadioButton;
  private JPanel              repositoryCheckboxPanel;
  private JButton             parseRepositoriesButton;
  private JCheckBox           showAllExternalsForCheckBox;
  private JCheckBox           showTagsCheckBox;
  private JCheckBox           showBranchesCheckBox;
  private JCheckBox           showTrunksCheckBox;
  private Os                  os                                = Os.findOs(System.getProperty("os.name"));
  private Config              config                            = new Config();
  private SubversionHandler   subversionHandler                 = new SubversionHandler(this);

  public MainFrame()
  {
    Authenticator.setDefault(new WebAuthenticator(config));
    initializeUi();
  }

  /** Go through the repositories and see what's there for externals. */
  private void parseRepositories()
  {
    Set<String> repositoryUrls = new TreeSet<String>();
    Component[] components     = repositoryCheckboxPanel.getComponents();

    for (Component component : components)
    {
      JCheckBox checkBox = (JCheckBox) component;

      if (checkBox.isSelected())
      {
        String text = checkBox.getText();

        repositoryUrls.add(text);
      }
    }

    populateTreeUi(repositoryUrls);
  }

  public void setStatus(String text)
  {
    statusLabel.setText(text);
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
  private void populateTreeUi(Set<String> repositoryUrls)
  {
    ScanExternalsTask task = new ScanExternalsTask(repositoryUrls, this, shallowBranchTagsTrunkRadioButton.isSelected(), subversionHandler,
                                                   showBranchesCheckBox.isSelected(), showTagsCheckBox.isSelected(), showTrunksCheckBox.isSelected());

    task.execute();
  }

  void processResults(List<External> externalsList)
  {
    ExternalResultsFilterSelector filterSelector = new ExternalResultsFilterSelector(externalsList);

    // todo now do something...  filter on external or project...
    // externalsList=filterSelector.getExternalsList();
    processExternals(externalsList);
  }

  /** Add a repository to the list. */
  private void addRepository()
  {
    String lastRepository = config.getLastRepository();
    String newUrl         = JOptionPane.showInputDialog(this, "Enter repository URL", lastRepository);

    if (!StringUtils.isEmpty(newUrl))
    {
      if (!newUrl.endsWith("/"))
      {
        newUrl += "/";
      }

      config.setLastRepository(newUrl);

      JCheckBox box = new JCheckBox(newUrl, true);

      repositoryCheckboxPanel.add(box);
      thePanel.validate();
      addStatus(" ");
    }
    // todo maybe a table with model, to prevent duplicate URLs - also, store repositories in preferences, show dialog of past choices
  }

  private void initializeUi()
  {
    setTitle("IvyFormatter v. " + Version.VERSION);
    addStatus("");
    setCursor(busyCursor);
    os.setLookAndFeel(this);

    try
    {
      initializeComponents();
      setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", this);
      setSize(600, 1000);
      center(this);
      setVisible(true);
      addStatus("Enter one or more repository to search...");
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
    validateDotPath();
  }

  /** toggle the buttons on/off based on the validity of the dot executable - don't let them proceed wihtout a valid value. */
  private void validateDotPath()
  {
    File    dotPath        = config.getDotExecutablePath();
    String  path           = dotPath.getName();
    boolean isDotPathValid = dotPath.exists();

    isDotPathValid &= path.startsWith("dot");
    parseRepositoriesButton.setEnabled(isDotPathValid);
    addRepositoryButton.setEnabled(isDotPathValid);
  }

  @Override
  public void addStatus(String statusLine)
  {
    System.out.println(statusLine);
    statusLabel.setText(statusLine);
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
          startSearchAction();
        }
      });
    findDotButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          findDotExecutablePath();
        }
      });
    addRepositoryButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          addRepository();
        }
      });
    parseRepositoriesButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          parseRepositories();
        }
      });
  }

  private void doQuitAction()
  {
    config.saveSettings();
    System.exit(0);
  }

  @SuppressWarnings({ "OverlyBroadCatchBlock" })
  private void startSearchAction()
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

        // doQuitAction();
      }
    }

    validateDotPath();
  }

  // ------------------------ INTERFACE METHODS ------------------------

  // --------------------- Interface UiMainFrame ---------------------

  /**  */
  @Override
  public Os getOs()
  {
    return os;
  }

  @Override
  public void initializeStatusBar(int minimum, int maximum, int initialValue, boolean visible)
  {
    progressBar.setMinimum(minimum);
    progressBar.setMaximum(maximum);
    progressBar.setValue(initialValue);
    progressBar.setVisible(visible);
  }

  @Override
  public boolean isTest()
  {
    return isTest;
  }

  @Override
  @SuppressWarnings({ "BooleanMethodNameMustStartWithQuestion" })
  public boolean getTestDataFromFile()
  {
    return getTestDataFromFile;
  }

  @Override
  public void setReady(boolean isReady)
  {
    findExternalsButton.setEnabled(isReady);
  }

  @Override
  public void showSevereError(String message, Exception e)
  {
    // todo
  }

  // -------------------------- OTHER METHODS --------------------------

  public void enableGoButton(boolean enabled)
  {
    findExternalsButton.setEnabled(enabled);
  }

  @SuppressWarnings({ "unchecked" })
  private Map<BuildableProjects, Map<String, List<External>>> loadExternalsFromFile() throws IOException, ClassNotFoundException
  {
    File              file        = new File("/Users/douglasbullard/Documents/JavaStuff/Nike Subversion Projects/JavaExternals/maintenance/IvyBrowser/externals.xml");
    XStream           xstream     = new XStream(new DomDriver());
    Reader            reader      = new FileReader(file);
    ObjectInputStream inputStream = xstream.createObjectInputStream(reader);
    Object            object      = inputStream.readObject();

    return (Map<BuildableProjects, Map<String, List<External>>>) object;
  }

  /** Take the list of externals and projects, and build up a dot file for it. */
  public void processExternals(List<External> externals)
  {
    setBusyCursor();

    try
    {
      // if (isTest)
      // {
      // if (getTestDataFromFile) {
      // dependencies = loadExternalsFromFile();
      // } else {
      // saveExternalsToFile(dependencies);

      // }
      // }

      OutputHandler outputHandler = new OutputHandler(this);
      File          dotExecutable = config.getDotExecutablePath();
      File          dotFile       = outputHandler.writeDotFile(externals);
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

  @Override
  public void setBusyCursor()
  {
    setCursor(Util.busyCursor);
  }

  private void saveExternalsToFile(Map<BuildableProjects, Map<String, List<External>>> dependencies) throws IOException
  {  // todo fix

    File               file       = new File("/Users/douglasbullard/Documents/JavaStuff/Nike Subversion Projects/JavaExternals/maintenance/IvyBrowser/externals.xml");
    XStream            xstream    = new XStream();
    Writer             fileWriter = new FileWriter(file);
    ObjectOutputStream out        = xstream.createObjectOutputStream(fileWriter);

    out.writeObject(dependencies);
    out.close();
  }

  @Override
  public void setNormalCursor()
  {
    setCursor(Util.normalCursor);
  }

  // --------------------------- main() method ---------------------------

  public static void main(String[] args)
  {
    MainFrame mainFrame = new MainFrame();
  }

  // --------------------- GETTER / SETTER METHODS ---------------------

  public Config getConfig()
  {
    return config;
  }

  private void createUIComponents()
  {
    repositoryCheckboxPanel = new JPanel();

    BoxLayout layout = new BoxLayout(repositoryCheckboxPanel, BoxLayout.Y_AXIS);

    repositoryCheckboxPanel.setLayout(layout);
  }

  public SubversionHandler getSubversionHandler()
  {
    return subversionHandler;
  }
}
