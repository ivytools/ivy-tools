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
}
