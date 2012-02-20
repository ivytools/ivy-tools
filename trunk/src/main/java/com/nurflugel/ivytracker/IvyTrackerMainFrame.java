package com.nurflugel.ivytracker;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.UniqueList;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.nurflugel.Os;
import com.nurflugel.WebAuthenticator;
import com.nurflugel.common.ui.FindMultiplePreferencesItemsDialog;
import com.nurflugel.common.ui.UiMainFrame;
import static com.nurflugel.common.ui.Util.centerApp;
import static com.nurflugel.common.ui.Util.setLookAndFeel;
import static com.nurflugel.common.ui.Version.VERSION;
import static com.nurflugel.externalsreporter.ui.ExternalsFinderMainFrame.sizeTableColumns;
import com.nurflugel.ivybrowser.InfiniteProgressPanel;
import com.nurflugel.ivybrowser.domain.IvyKey;
import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivybrowser.handlers.BaseWebIvyRepositoryBrowserHandler;
import com.nurflugel.ivybrowser.ui.CheckboxCellRenderer;
import static com.nurflugel.ivybrowser.ui.HandlerFactory.getIvyFileFinderHandler;
import static com.nurflugel.ivybrowser.ui.HandlerFactory.getIvyRepositoryHandler;
import static com.nurflugel.ivytracker.Config.LAST_IVY_REPOSITORY;
import static com.nurflugel.ivytracker.Config.LAST_SUBVERSION_REPOSITORY;
import com.nurflugel.ivytracker.domain.*;
import com.nurflugel.ivytracker.handlers.HtmlIvyHandler;
import com.nurflugel.ivytracker.handlers.IvyFileFinderHandler;
import static org.apache.commons.lang.StringUtils.isEmpty;
import java.awt.*;
import static java.awt.Cursor.getPredefinedCursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.Authenticator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import static javax.swing.BorderFactory.createEtchedBorder;
import static javax.swing.BorderFactory.createTitledBorder;
import javax.swing.*;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: May 30, 2008 Time: 11:38:58 AM To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({
                    "CallToPrintStackTrace", "UseOfSystemOutOrSystemErr", "IOResourceOpenedButNotSafelyClosed", "CallToSystemExit",
                    "OverlyBroadCatchBlock", "ParameterHidesMemberVariable"
                  })
public class IvyTrackerMainFrame extends JFrame implements UiMainFrame
{
  private static final long                    serialVersionUID                  = 8982188831570838035L;
  private static final String                  NEW_LINE                          = "\n";
  private static final String                  TAB                               = "\t";
  private InfiniteProgressPanel                progressPanel                     = new InfiniteProgressPanel("Accessing the repositories, please be patient",
                                                                                                             this);
  private Cursor                               busyCursor                        = getPredefinedCursor(WAIT_CURSOR);
  private Cursor                               normalCursor                      = getPredefinedCursor(DEFAULT_CURSOR);
  private JButton                              quitButton;
  private JLabel                               statusLabel;
  private EventList<IvyPackage>                ivyRepositoryList                 = new UniqueList<IvyPackage>(new BasicEventList<IvyPackage>());
  private JPanel                               mainPanel;
  private JTextField                           ivyFilterField;
  private JTable                               ivyResultsTable;
  private JButton                              specifySubversionRepositoryButton;
  private JButton                              specifyIvyRepositoryButton;
  private JButton                              showProjectsUsingSelectedButton;
  private JButton                              showIvyModulesUsedButton;
  private JButton                              showUnusedIvyModulesButton;
  private JTable                               projectResultsTable;
  private JTextField                           projectFilterField;
  private JButton                              parseRepositoriesButton;
  private JPanel                               ivyRepositoryPanel;
  private JPanel                               projectRepositoryPanel;
  private JButton                              clearProjectFilterButton;
  private JButton                              clearIvyFilterButton;
  private JPanel                               projectRepositoryHoldingPanel;
  private JPanel                               ivyRepositoryHoldingPanel;
  private JCheckBox                            projectSelectUnselectAllCheckBox;
  private JCheckBox                            ivySelectUnselectAllCheckBox;
  private JCheckBox                            showOnlySelectedIvyItemsCheckBox;
  private JCheckBox                            showOnlySelectedProjectsCheckBox;
  private JCheckBox                            showTrunksCheckBox;
  private JCheckBox                            showBranchesCheckBox;
  private JCheckBox                            showTagsCheckBox;
  private JButton                              analyzeSelectedItemsForButton;
  private JCheckBox                            recurseCheckBox;
  private JButton                              saveTestDataButton;
  private JButton                              loadTestDataButton;
  private JSplitPane                           splitPane;
  private IvyFileComparator                    ivyFileComparator;
  private TextComponentMatcherEditor           ivyComponentMatcherEditor;
  private IvyFileTableFormat                   ivyFileTableFormat;
  private ProjectFileTableFormat               projectsTableFormat;
  private Matcher<IvyPackage>                  ivyCountValueMatcherEditor;
  private Matcher<Project>                     projectCountValueMatcherEditor;
  private EventList<Project>                   projectUrls                       = new BasicEventList<Project>();
  private Set<String>                          missingIvyFiles                   = new HashSet<String>();
  private final Map<Project, List<IvyPackage>> projectIvyFilesMap                = new TreeMap<Project, List<IvyPackage>>();
  private ProjectComparator                    projectFileComparator;
  private TextComponentMatcherEditor           projectComponentMatcherEditor;
  private Config                               config                            = new Config();
  private static final String                  SUBVERSION_REPOSITORY             = "Subversion Repository: ";
  private static final String                  IVY_REPOSITORY                    = "Ivy Repository: ";
  private boolean                              isProjectsDone;  // false as long as the project parsing is

  // running
  private boolean                     isIvyDone;  // false as long as ivy parsing is running
  public static final boolean         useSingleThread                            = false;
  private EventTableModel<Project>    projectTableModel;
  private EventTableModel<IvyPackage> ivyPackageEventTableModel;

  // --------------------------- CONSTRUCTORS ---------------------------
  @SuppressWarnings({ "unchecked" })
  public IvyTrackerMainFrame()
  {
    WebAuthenticator webAuthenticator = new WebAuthenticator();

    initializeComponents();
    setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", this);
    ivyFileComparator             = new IvyFileComparator();
    projectFileComparator         = new ProjectComparator();
    ivyComponentMatcherEditor     = new TextComponentMatcherEditor(ivyFilterField, new IvyFileFilterator());
    projectComponentMatcherEditor = new TextComponentMatcherEditor(projectFilterField, new ProjectFileFilterator());
    ivyFileTableFormat            = new IvyFileTableFormat();
    projectsTableFormat           = new ProjectFileTableFormat();

    // webAuthenticator.showDialog();
    Authenticator.setDefault(webAuthenticator);
    pack();
    setSize(1000, 1000);
    centerApp(this);
    ivyFilterField.setEnabled(false);
    setVisible(true);
    setupTables();
    splitPane.addPropertyChangeListener(new PropertyChangeListener()
      {
        @Override
        public void propertyChange(PropertyChangeEvent propertyChangeEvent)
        {
          // System.out.println("propertyChangeEvent = " + propertyChangeEvent);
          // resizeTableColumns();
          // splitPane.layout();
        }
      });

    // todo remove
    readTestData();
  }

  private void toggleAllIvyItems(boolean selected, EventList<IvyPackage> ivyRepositoryList)
  {
    for (IvyPackage ivyPackage : ivyRepositoryList)
    {
      ivyPackage.setIncluded(selected);
    }
    // hack to make selection show

    ivyResultsTable.repaint();
  }

  private void toggleAllProjectItems(boolean selected, EventList<Project> projectUrls)
  {
    for (Project projectUrl : projectUrls)
    {
      projectUrl.setIncluded(selected);
    }
    // hack to make selection show

    projectResultsTable.repaint();
  }

  @SuppressWarnings({ "unchecked" })
  private void setupTables()
  {
    try
    {
      SortedList<IvyPackage> sortedPackages    = new SortedList<IvyPackage>(ivyRepositoryList, ivyFileComparator);
      FilterList<IvyPackage> filteredPackages  = new FilterList<IvyPackage>(sortedPackages, ivyComponentMatcherEditor);
      FilterList<IvyPackage> filteredPackages2 = new FilterList<IvyPackage>(filteredPackages, ivyCountValueMatcherEditor);

      ivyPackageEventTableModel = new EventTableModel<IvyPackage>(filteredPackages2, ivyFileTableFormat);
      ivyResultsTable.setModel(ivyPackageEventTableModel);
      ivyResultsTable.setDefaultRenderer(Object.class, new CheckboxCellRenderer(false));

      // yes, we need this line even through the result is ignored.
      TableComparatorChooser<IvyPackage> tableSorter       = new TableComparatorChooser<IvyPackage>(ivyResultsTable, sortedPackages, true);
      SortedList<Project>                sortedProjects    = new SortedList<Project>(projectUrls, projectFileComparator);
      FilterList<Project>                filteredProjects  = new FilterList<Project>(sortedProjects, projectComponentMatcherEditor);
      FilterList<Project>                filteredProjects2 = new FilterList<Project>(filteredProjects, projectCountValueMatcherEditor);

      projectTableModel = new EventTableModel<Project>(filteredProjects2, projectsTableFormat);
      projectResultsTable.setModel(projectTableModel);
      projectResultsTable.setDefaultRenderer(Object.class, new CheckboxCellRenderer(false));

      // yes, we need this line even through the result is ignored.
      TableComparatorChooser<Project> projectTableSorter = new TableComparatorChooser<Project>(projectResultsTable, sortedProjects, true);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void initializeComponents()
  {
    setGlassPane(progressPanel);
    setContentPane(mainPanel);
    setTitle("Ivy Usage Tracker v. " + VERSION);
    addListeners();
    setPanelTitle(ivyRepositoryPanel, IVY_REPOSITORY, config.getLastIvyRepository());
    setPanelTitle(projectRepositoryPanel, SUBVERSION_REPOSITORY, config.getLastSubversionRepository());
    showTagsCheckBox.setSelected(config.showTags());
    showBranchesCheckBox.setSelected(config.showBranches());
    showTrunksCheckBox.setSelected(config.showTrunks());
    recurseCheckBox.setSelected(config.recurseDirs());
  }

  private void addListeners()
  {
    ivyResultsTable.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mousePressed(MouseEvent e)
        {
          if (e.getButton() != MouseEvent.BUTTON3)
          {
            return;
          }

          Point origin = e.getPoint();
          int   row    = ivyResultsTable.rowAtPoint(origin);
          int   col    = ivyResultsTable.columnAtPoint(origin);

          if (col == 0)
          {
            return;
          }

          if (row > -1)
          {
            EventTableModel         tableModel = (EventTableModel) ivyResultsTable.getModel();
            IvyPackage              ivyPackage = (IvyPackage) tableModel.getElementAt(row);
            FindUsingProjectsDialog dialog     = new FindUsingProjectsDialog(ivyPackage, projectIvyFilesMap, ivyRepositoryList);

            dialog.setVisible(true);
          }
        }

        @Override
        public void mouseClicked(MouseEvent e)
        {
          Point origin = e.getPoint();
          int   row    = ivyResultsTable.rowAtPoint(origin);
          int   col    = ivyResultsTable.columnAtPoint(origin);

          if ((row != -1) && (col == 0))
          {
            IvyPackage ivyPackage = ivyPackageEventTableModel.getElementAt(row);

            ivyPackage.setIncluded(!ivyPackage.isIncluded());

            // hack to make selection show
            ivyResultsTable.repaint();
          }
        }
      });
    projectResultsTable.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mouseClicked(MouseEvent e)
        {
          Point origin = e.getPoint();
          int   row    = projectResultsTable.rowAtPoint(origin);
          int   column = projectResultsTable.columnAtPoint(origin);

          if ((row != -1) && (column == 0))
          {
            Project project = projectTableModel.getElementAt(row);

            project.setIncluded(!project.isIncluded());

            // hack to make selection show
            projectResultsTable.repaint();
          }
        }
      });
    showTrunksCheckBox.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          config.setShowTrunks(showTrunksCheckBox.isSelected());
        }
      });
    showTagsCheckBox.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          config.setShowTags(showTagsCheckBox.isSelected());
        }
      });
    showBranchesCheckBox.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          config.setShowBranches(showBranchesCheckBox.isSelected());
        }
      });
    recurseCheckBox.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          config.setRecurseDirs(recurseCheckBox.isSelected());
        }
      });
    specifyIvyRepositoryButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent actionEvent)
        {
          specifyIvyRepository();
        }
      });
    specifySubversionRepositoryButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          specifySubversionRepository();
        }
      });
    parseRepositoriesButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          getBuildItems();
        }
      });
    quitButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          System.exit(0);
        }
      });
    addWindowListener(new WindowAdapter()
      {
        @Override
        public void windowClosing(WindowEvent e)
        {
          super.windowClosing(e);
          System.exit(0);
        }
      });
    projectSelectUnselectAllCheckBox.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          toggleAllProjectItems(projectSelectUnselectAllCheckBox.isSelected(), projectUrls);
        }
      });
    showOnlySelectedProjectsCheckBox.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          // todo show only selected projects
        }
      });
    showOnlySelectedIvyItemsCheckBox.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          // todo only show selected Ivy items
        }
      });
    ivySelectUnselectAllCheckBox.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          toggleAllIvyItems(ivySelectUnselectAllCheckBox.isSelected(), ivyRepositoryList);
        }
      });
    analyzeSelectedItemsForButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          analyzeSelectedItems();
        }
      });
    showProjectsUsingSelectedButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          Map<IvyKey, IvyPackage> dibble = new HashMap<IvyKey, IvyPackage>();
          ShowProjectsDialog      dialog = new ShowProjectsDialog(ivyRepositoryList, projectIvyFilesMap);

          // todo need to add ivy package to project URL, to follow dependencies...
          dialog.setVisible(true);
        }
      });
    saveTestDataButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          saveTestData();
        }
      });
    loadTestDataButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          readTestData();
        }
      });
    clearProjectFilterButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          projectFilterField.setText("");
        }
      });
    clearIvyFilterButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
          ivyFilterField.setText("");
        }
      });
  }

  private void analyzeSelectedItems()
  {
    HtmlIvyHandler handler = new HtmlIvyHandler(this, projectUrls, ivyRepositoryList, missingIvyFiles, projectIvyFilesMap);

    if (useSingleThread)
    {
      handler.doInBackground();
    }
    else
    {
      handler.execute();
    }
  }

  public void doneWithAnalysis()
  {
    showProjectsUsingSelectedButton.setEnabled(true);
    showUnusedIvyModulesButton.setEnabled(true);
    showIvyModulesUsedButton.setEnabled(true);
  }

  private void setPanelTitle(JPanel panel, String text, String path)
  {
    panel.setBorder(createTitledBorder(createEtchedBorder(), text + path));
  }

  public void specifyIvyRepository()
  {
    // FindMultiplePreferencesItemsDialog dialog = new FindMultiplePreferencesItemsDialog(config.getPreferences(), "Select Ivy Repository",
    FindMultiplePreferencesItemsDialog dialog = new FindMultiplePreferencesItemsDialog(null, "Select Ivy Repository", LAST_IVY_REPOSITORY);

    dialog.setVisible(true);

    if (dialog.isOk())
    {
      String repositoryPath = dialog.getRepositoryLocation();

      if (!isEmpty(repositoryPath) && !repositoryPath.equals(config.getLastIvyRepository()))
      {
        config.setLastIvyRepository(repositoryPath);
        setPanelTitle(ivyRepositoryPanel, IVY_REPOSITORY, repositoryPath);
      }
    }
  }

  public void specifySubversionRepository()
  {
    // FindMultiplePreferencesItemsDialog dialog = new FindMultiplePreferencesItemsDialog(config.getPreferences(), "Select Subversion Repository",
    FindMultiplePreferencesItemsDialog dialog = new FindMultiplePreferencesItemsDialog(null, "Select Subversion Repository",
                                                                                       LAST_SUBVERSION_REPOSITORY);

    dialog.setVisible(true);

    if (dialog.isOk())
    {
      String location = dialog.getRepositoryLocation();

      if (!isEmpty(location) && !location.equals(config.getLastSubversionRepository()))
      {
        config.setLastSubversionRepository(location);
        setPanelTitle(projectRepositoryPanel, SUBVERSION_REPOSITORY, location);
      }
    }
  }

  /** Go and do all the parsing of the projects and Ivy. */
  private void getBuildItems()
  {
    String ivyRepositoryPath = config.getLastIvyRepository();

    if (ivyRepositoryPath.length() == 0)
    {
      specifyIvyRepository();
    }

    String projectRepositoryPath = config.getLastSubversionRepository();

    if (projectRepositoryPath.length() == 0)
    {
      specifySubversionRepository();
    }

    startProgressPanel();

    Map<String, Map<String, Map<String, IvyPackage>>> packageMap = new HashMap<String, Map<String, Map<String, IvyPackage>>>();

    getAllIvyRepositoryPackages(packageMap);
    getAllProjectIvyFilesFromSubversion();
    System.out.println("Done!");
  }

  private void startProgressPanel()
  {
    isProjectsDone = false;
    isIvyDone      = false;
    setBusyCursor();
    progressPanel.start();
  }

  @Override
  public void setBusyCursor()
  {
    setCursor(busyCursor);
  }

  /** get all Ivy repository (reuse code). */
  private void getAllIvyRepositoryPackages(Map<String, Map<String, Map<String, IvyPackage>>> packageMap)
  {
    ivyRepositoryList.clear();

    BaseWebIvyRepositoryBrowserHandler webHandler = getIvyRepositoryHandler(this, config.getLastIvyRepository(), ivyRepositoryList, packageMap, null);

    if (useSingleThread)
    {
      webHandler.doInBackground();
    }
    else
    {
      webHandler.execute();
    }
  }

  // ------------------------ INTERFACE METHODS ------------------------
  // --------------------- Interface UiMainFrame ---------------------
  @Override
  public void addStatus(String statusLine)
  {
    statusLabel.setText(statusLine);
  }

  @Override
  public Os getOs()
  {
    return null;  // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean getTestDataFromFile()
  {
    return false;  // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void initializeStatusBar(int minimum, int maximum, int initialValue, boolean visible)
  {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean isTest()
  {
    return false;
  }

  @Override
  public void setReady(boolean isReady)
  {
    if (isIvyDone && isProjectsDone)
    {
      setStatusLabel("");
      stopProgressPanel();
      ivyFilterField.setEnabled(true);
      projectFilterField.setEnabled(true);
      analyzeSelectedItemsForButton.setEnabled(true);
    }
  }

  @Override
  public void showSevereError(String message, Exception e)
  {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void stopThreads()
  {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void setStatusLabel(String text)
  {
    statusLabel.setText(text);
  }

  @Override
  public void stopProgressPanel()
  {
    if (isIvyDone && isProjectsDone)
    {
      progressPanel.stop();
      setNormalCursor();
    }
  }
  // -------------------------- OTHER METHODS --------------------------

  // private void exportTableToClipboard()
  // {
  // SortedList<IvyPackage> sortedPackages   = new SortedList<IvyPackage>(ivyRepositoryList, ivyFileComparator);
  // FilterList<IvyPackage> filteredPackages = new FilterList<IvyPackage>(sortedPackages, ivyComponentMatcherEditor);
  // FilterList<IvyPackage> list             = new FilterList<IvyPackage>(filteredPackages, ivyCountValueMatcherEditor);
  // StringBuilder          sb               = new StringBuilder();
  //
  // sb.append("Org\tModule\tVersion\tCount\n");
  //
  // for (IvyPackage ivyFile : list)
  // {
  // sb.append(ivyFile.getOrgName()).append(TAB).append(ivyFile.getModuleName()).append(TAB).append(ivyFile.getVersion()).append(TAB)
  // .append(ivyFile.getCount()).append(NEW_LINE);
  // }
  //
  // getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()), null);
  // showMessageDialog(this, "Formatted text has been pasted into your buffer");
  // }
  private void findMissingIvyFiles()
  {
    StringBuilder sb = new StringBuilder();

    for (String missingIvyFile : missingIvyFiles)
    {
      sb.append(missingIvyFile).append(NEW_LINE);
    }

    showMessageDialog(this, sb.toString(), "Here are the missing files:", PLAIN_MESSAGE);
  }

  private void getAllProjectIvyFilesFromSubversion()
  {
    projectIvyFilesMap.clear();
    projectUrls.clear();

    String               repositoryBase       = config.getLastSubversionRepository();
    IvyFileFinderHandler ivyFileFinderHandler = getIvyFileFinderHandler(this, projectIvyFilesMap, projectUrls, config, repositoryBase);

    if (useSingleThread)
    {
      ivyFileFinderHandler.doIt();
    }
    else
    {
      ivyFileFinderHandler.execute();
    }
  }

  /** XStream really, really, really doesn't like to save the Glazed lists, so we have to get the raw data and save it separately. */
  public void saveTestData()
  {
    setBusyCursor();

    DataSerializer serializer = new DataSerializer(projectIvyFilesMap, projectUrls, ivyRepositoryList);

    serializer.saveToXml();
    setNormalCursor();
  }

  public void readTestData()
  {
    setBusyCursor();

    DataSerializer serializer = new DataSerializer(projectIvyFilesMap, projectUrls, ivyRepositoryList);

    serializer.retrieveFromXml();
    projectIvyFilesMap.clear();
    projectIvyFilesMap.putAll(serializer.getProjectIvyFilesMap());
    projectUrls.clear();
    projectUrls.addAll(serializer.getProjectUrls());
    ivyRepositoryList.clear();
    ivyRepositoryList.addAll(serializer.getIvyRepositoryList());
    resizeTableColumns();
    isIvyDone      = true;
    isProjectsDone = true;
    setReady(true);
    showNormal();
  }

  public void showNormal()
  {
    setNormalCursor();
    statusLabel.setText("");

    // adjustColumnWidths();
    ivyFilterField.setEnabled(true);
    ivyFilterField.requestFocus();
  }

  @Override
  public void setNormalCursor()
  {
    setCursor(normalCursor);
  }

  // --------------------------- main() method ---------------------------
  @SuppressWarnings({ "ResultOfObjectAllocationIgnored" })
  public static void main(String[] args)
  {
    IvyTrackerMainFrame frame = new IvyTrackerMainFrame();
  }

  @Override
  public void resizeTableColumns()
  {
    sizeTableColumns(projectResultsTable);
    sizeTableColumns(ivyResultsTable);
  }

  public void setProjectsDone(boolean projectsDone)
  {
    isProjectsDone = projectsDone;
  }

  public void setIvyDone(boolean ivyDone)
  {
    isIvyDone = ivyDone;
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
    mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout(0, 0));

    final JPanel panel1 = new JPanel();

    panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
    mainPanel.add(panel1, BorderLayout.NORTH);

    final JPanel panel2 = new JPanel();

    panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
    panel1.add(panel2,
               new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

    final JPanel panel3 = new JPanel();

    panel3.setLayout(new BorderLayout(0, 0));
    mainPanel.add(panel3, BorderLayout.SOUTH);
    statusLabel = new JLabel();
    statusLabel.setHorizontalAlignment(0);
    statusLabel.setText("Please wait while the repository is scanned...");
    panel3.add(statusLabel, BorderLayout.CENTER);

    final JPanel panel4 = new JPanel();

    panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
    panel3.add(panel4, BorderLayout.NORTH);

    final JPanel panel5 = new JPanel();

    panel5.setLayout(new GridLayoutManager(6, 2, new Insets(0, 0, 0, 0), -1, -1));
    panel4.add(panel5,
               new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    quitButton = new JButton();
    quitButton.setText("Quit");
    quitButton.setMnemonic('Q');
    quitButton.setDisplayedMnemonicIndex(0);
    panel5.add(quitButton,
               new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));

    final JPanel panel6 = new JPanel();

    panel6.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
    panel5.add(panel6,
               new GridConstraints(0, 0, 5, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    panel6.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Reports"));
    showProjectsUsingSelectedButton = new JButton();
    showProjectsUsingSelectedButton.setEnabled(true);
    showProjectsUsingSelectedButton.setText("Show projects using selected Ivy modules");
    panel6.add(showProjectsUsingSelectedButton,
               new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));
    showIvyModulesUsedButton = new JButton();
    showIvyModulesUsedButton.setEnabled(false);
    showIvyModulesUsedButton.setText("Show Ivy modules used by project");
    panel6.add(showIvyModulesUsedButton,
               new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));
    showUnusedIvyModulesButton = new JButton();
    showUnusedIvyModulesButton.setEnabled(false);
    showUnusedIvyModulesButton.setText("Show unused Ivy modules");
    panel6.add(showUnusedIvyModulesButton,
               new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));

    final JPanel panel7 = new JPanel();

    panel7.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
    panel5.add(panel7,
               new GridConstraints(0, 1, 6, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

    final JPanel panel8 = new JPanel();

    panel8.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
    panel7.add(panel8,
               new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    panel8.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Load/Save Data"));
    saveTestDataButton = new JButton();
    saveTestDataButton.setText("Save data");
    panel8.add(saveTestDataButton,
               new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));
    loadTestDataButton = new JButton();
    loadTestDataButton.setText("Load Data");
    panel8.add(loadTestDataButton,
               new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));

    final Spacer spacer1 = new Spacer();

    panel7.add(spacer1,
               new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                                   1, null, null, null, 0, false));

    final Spacer spacer2 = new Spacer();

    panel7.add(spacer2,
               new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW,
                                   null, null, null, 0, false));

    final JPanel panel9 = new JPanel();

    panel9.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
    panel4.add(panel9,
               new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

    final Spacer spacer3 = new Spacer();

    panel9.add(spacer3,
               new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW,
                                   null, null, null, 0, false));

    final Spacer spacer4 = new Spacer();

    panel9.add(spacer4,
               new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                                   1, null, null, null, 0, false));

    final JPanel panel10 = new JPanel();

    panel10.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
    panel9.add(panel10,
               new GridConstraints(0, 1, 4, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    panel10.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Actions"));
    specifyIvyRepositoryButton = new JButton();
    specifyIvyRepositoryButton.setText("Specify Ivy Repository");
    panel10.add(specifyIvyRepositoryButton,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                    null, null, null, 0, false));
    specifySubversionRepositoryButton = new JButton();
    specifySubversionRepositoryButton.setText("Specify Subversion Repository");
    panel10.add(specifySubversionRepositoryButton,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                    null, null, null, 0, false));
    parseRepositoriesButton = new JButton();
    parseRepositoriesButton.setText("Scan repositories");
    panel10.add(parseRepositoriesButton,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                    null, null, null, 0, false));
    analyzeSelectedItemsForButton = new JButton();
    analyzeSelectedItemsForButton.setEnabled(false);
    analyzeSelectedItemsForButton.setText("Analyze selected items for usage");
    panel10.add(analyzeSelectedItemsForButton,
                new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                    null, null, null, 0, false));

    final JPanel panel11 = new JPanel();

    panel11.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
    mainPanel.add(panel11, BorderLayout.CENTER);
    splitPane = new JSplitPane();
    splitPane.setDividerLocation(500);
    panel11.add(splitPane,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null,
                                    0, false));

    final JScrollPane scrollPane1 = new JScrollPane();

    splitPane.setLeftComponent(scrollPane1);

    final JPanel panel12 = new JPanel();

    panel12.setLayout(new BorderLayout(0, 0));
    scrollPane1.setViewportView(panel12);
    projectRepositoryPanel = new JPanel();
    projectRepositoryPanel.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
    panel12.add(projectRepositoryPanel, BorderLayout.NORTH);
    projectRepositoryPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Subversion Repositories"));
    projectRepositoryHoldingPanel = new JPanel();
    projectRepositoryHoldingPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
    projectRepositoryPanel.add(projectRepositoryHoldingPanel,
                               new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                                                   false));

    final JPanel panel13 = new JPanel();

    panel13.setLayout(new BorderLayout(0, 0));
    projectRepositoryPanel.add(panel13,
                               new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                                                   false));

    final JLabel label1 = new JLabel();

    label1.setText("Filter Subversion result:");
    panel13.add(label1, BorderLayout.WEST);
    projectFilterField = new JTextField();
    projectFilterField.setEnabled(false);
    panel13.add(projectFilterField, BorderLayout.CENTER);
    clearProjectFilterButton = new JButton();
    clearProjectFilterButton.setText("Clear");
    panel13.add(clearProjectFilterButton, BorderLayout.EAST);

    final JPanel panel14 = new JPanel();

    panel14.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
    projectRepositoryPanel.add(panel14,
                               new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                                                   false));
    panel14.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Project options"));

    final JPanel panel15 = new JPanel();

    panel15.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
    panel14.add(panel15,
                new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    projectSelectUnselectAllCheckBox = new JCheckBox();
    projectSelectUnselectAllCheckBox.setText("Select/Unselect all");
    panel15.add(projectSelectUnselectAllCheckBox,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                    null, null, null, 0, false));
    showOnlySelectedProjectsCheckBox = new JCheckBox();
    showOnlySelectedProjectsCheckBox.setEnabled(false);
    showOnlySelectedProjectsCheckBox.setText("Show only selected projects");
    panel15.add(showOnlySelectedProjectsCheckBox,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                    null, null, null, 0, false));

    final JPanel panel16 = new JPanel();

    panel16.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
    panel14.add(panel16,
                new GridConstraints(0, 0, 3, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    panel16.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Include level"));
    showTrunksCheckBox = new JCheckBox();
    showTrunksCheckBox.setEnabled(true);
    showTrunksCheckBox.setSelected(false);
    showTrunksCheckBox.setText("Show trunks");
    panel16.add(showTrunksCheckBox,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                    null, null, null, 0, false));
    showBranchesCheckBox = new JCheckBox();
    showBranchesCheckBox.setText("Show branches");
    panel16.add(showBranchesCheckBox,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                    null, null, null, 0, false));
    showTagsCheckBox = new JCheckBox();
    showTagsCheckBox.setText("Show tags");
    panel16.add(showTagsCheckBox,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                    null, null, null, 0, false));
    recurseCheckBox = new JCheckBox();
    recurseCheckBox.setText("Recurse");
    panel16.add(recurseCheckBox,
                new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                    null, null, null, 0, false));

    final Spacer spacer5 = new Spacer();

    panel14.add(spacer5,
                new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW,
                                    null, null, null, 0, false));

    final Spacer spacer6 = new Spacer();

    panel14.add(spacer6,
                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW,
                                    null, null, null, 0, false));

    final JScrollPane scrollPane2 = new JScrollPane();

    panel12.add(scrollPane2, BorderLayout.CENTER);
    projectResultsTable = new JTable();
    projectResultsTable.setPreferredScrollableViewportSize(new Dimension(450, 400));
    scrollPane2.setViewportView(projectResultsTable);

    final JPanel panel17 = new JPanel();

    panel17.setLayout(new BorderLayout(0, 0));
    splitPane.setRightComponent(panel17);
    ivyRepositoryPanel = new JPanel();
    ivyRepositoryPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
    panel17.add(ivyRepositoryPanel, BorderLayout.NORTH);
    ivyRepositoryPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Ivy Repository"));
    ivyRepositoryHoldingPanel = new JPanel();
    ivyRepositoryHoldingPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
    ivyRepositoryPanel.add(ivyRepositoryHoldingPanel,
                           new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                               GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                               GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                                               false));

    final JPanel panel18 = new JPanel();

    panel18.setLayout(new BorderLayout(0, 0));
    ivyRepositoryPanel.add(panel18,
                           new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                               GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                               GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                                               false));

    final JLabel label2 = new JLabel();

    label2.setText("Filter Ivy Packages");
    panel18.add(label2, BorderLayout.WEST);
    ivyFilterField = new JTextField();
    ivyFilterField.setEnabled(false);
    panel18.add(ivyFilterField, BorderLayout.CENTER);
    clearIvyFilterButton = new JButton();
    clearIvyFilterButton.setText("Clear");
    panel18.add(clearIvyFilterButton, BorderLayout.EAST);

    final JPanel panel19 = new JPanel();

    panel19.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
    ivyRepositoryPanel.add(panel19,
                           new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                               GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                               GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                                               false));
    panel19.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Ivy options"));
    showOnlySelectedIvyItemsCheckBox = new JCheckBox();
    showOnlySelectedIvyItemsCheckBox.setEnabled(false);
    showOnlySelectedIvyItemsCheckBox.setText("Show only selected items");
    panel19.add(showOnlySelectedIvyItemsCheckBox,
                new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                    null, null, null, 0, false));

    final Spacer spacer7 = new Spacer();

    panel19.add(spacer7,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                                    1, null, null, null, 0, false));

    final Spacer spacer8 = new Spacer();

    panel19.add(spacer8,
                new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                                    1, null, null, null, 0, false));
    ivySelectUnselectAllCheckBox = new JCheckBox();
    ivySelectUnselectAllCheckBox.setText("Select/Unselect all");
    panel19.add(ivySelectUnselectAllCheckBox,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                    null, null, null, 0, false));

    final JScrollPane scrollPane3 = new JScrollPane();

    panel17.add(scrollPane3, BorderLayout.CENTER);
    ivyResultsTable = new JTable();
    scrollPane3.setViewportView(ivyResultsTable);
  }

  /** @noinspection  ALL */
  public JComponent $$$getRootComponent$$$()
  {
    return mainPanel;
  }
}
