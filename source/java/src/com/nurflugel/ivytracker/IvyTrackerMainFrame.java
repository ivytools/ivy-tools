package com.nurflugel.ivytracker;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import com.nurflugel.Os;
import com.nurflugel.WebAuthenticator;
import com.nurflugel.common.ui.FindMultiplePreferencesItemsDialog;
import com.nurflugel.common.ui.UiMainFrame;
import com.nurflugel.common.ui.Version;
import com.nurflugel.ivybrowser.InfiniteProgressPanel;
import com.nurflugel.ivybrowser.domain.IvyFile;
import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivybrowser.handlers.BaseWebIvyRepositoryBrowserHandler;
import com.nurflugel.ivybrowser.ui.HandlerFactory;
import com.nurflugel.ivytracker.domain.*;
import com.nurflugel.ivytracker.handlers.IvyFileFinderHandler;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.net.Authenticator;
import java.util.*;
import java.util.List;
import static com.nurflugel.common.ui.Util.centerApp;
import static com.nurflugel.common.ui.Util.setLookAndFeel;
import static com.nurflugel.ivytracker.Config.*;
import static java.awt.Cursor.getPredefinedCursor;
import static java.lang.Math.max;
import static javax.swing.BorderFactory.createEtchedBorder;
import static javax.swing.BorderFactory.createTitledBorder;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.apache.commons.lang.StringUtils.isEmpty;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: May 30, 2008 Time: 11:38:58 AM To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({
                    "CallToPrintStackTrace", "UseOfSystemOutOrSystemErr", "IOResourceOpenedButNotSafelyClosed", "CallToSystemExit",
                    "OverlyBroadCatchBlock", "ParameterHidesMemberVariable"
                  })
public class IvyTrackerMainFrame extends JFrame implements UiMainFrame
{
  private static final long                 serialVersionUID                  = 8982188831570838035L;
  private static final String               NEW_LINE                          = "\n";
  private static final String               TAB                               = "\t";
  private InfiniteProgressPanel             progressPanel                     = new InfiniteProgressPanel("Accessing the repositories, please be patient",
                                                                                                          this);
  private boolean                           useTestData                       = false;  // if true, reads canned data in from a file for fast testing
  private boolean                           saveTestData                      = false;  // if true, reads canned data in from a file for fast testing
  private boolean                           isTest                            = false;  // if true, reads canned data in from a file for fast testing
  private Cursor                            busyCursor                        = getPredefinedCursor(Cursor.WAIT_CURSOR);
  private Cursor                            normalCursor                      = getPredefinedCursor(Cursor.DEFAULT_CURSOR);
  private JButton                           quitButton;
  private JLabel                            statusLabel;
  private EventList<IvyPackage>             ivyRepositoryList                 = new BasicEventList<IvyPackage>();
  private JPanel                            mainPanel;
  private JTextField                        ivyFilterField;
  private JTable                            ivyResultsTable;
  private JButton                           specifySubversionRepositoryButton;
  private JButton                           specifyIvyRepositoryButton;
  private JButton                           showProjectsUsingSelectedButton;
  private JButton                           showIvyModulesUsedButton;
  private JButton                           showUnusedIvyModulesButton;
  private JTable                            projectResultsTable;
  private JTextField                        projectFilterField;
  private JButton                           parseRepositoriesButton;
  private JPanel                            ivyRepositoryPanel;
  private JPanel                            projectRepositoryPanel;
  private IvyFileComparator                 ivyFileComparator;
  private TextComponentMatcherEditor        ivyComponentMatcherEditor;
  private IvyFileTableFormat                ivyFileTableFormat;
  private ProjectFileTableFormat            projectsTableFormat;
  private Matcher<IvyPackage>               ivyCountValueMatcherEditor;
  private Matcher<Project>                  projectCountValueMatcherEditor;
  private Map<Project, IvyPackage>          ivyFilesMap;
  private EventList<Project>                projectUrls                       = new BasicEventList<Project>();
  private Set<String>                       missingIvyFiles                   = new HashSet<String>();
  private String                            ivyRepositoryPath;
  private final Map<Project, List<IvyFile>> projectIvyFiles                   = new TreeMap<Project, List<IvyFile>>();
  private ProjectComparator                 projectFileComparator;
  private TextComponentMatcherEditor        projectComponentMatcherEditor;
  private Config                            config                            = new Config();
  private String                            projectRepositoryPath;
  private static final String               SUBVERSION_REPOSITORY             = "Subversion Repository: ";
  private static final String               IVY_REPOSITORY                    = "Ivy Repository: ";

  // --------------------------- CONSTRUCTORS ---------------------------

  @SuppressWarnings({ "unchecked" })
  public IvyTrackerMainFrame()
  {
    Authenticator.setDefault(new WebAuthenticator());

    ivyRepositoryPath     = config.getLastIvyRepository();
    projectRepositoryPath = config.getLastSubversionRepository();
    initializeComponents();
    setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", this);
    ivyFileComparator             = new IvyFileComparator();
    projectFileComparator         = new ProjectComparator();
    ivyComponentMatcherEditor     = new TextComponentMatcherEditor(ivyFilterField, new IvyFileFilterator());
    projectComponentMatcherEditor = new TextComponentMatcherEditor(projectFilterField, new ProjectFileFilterator());
    ivyFileTableFormat            = new IvyFileTableFormat();
    projectsTableFormat           = new ProjectFileTableFormat();

    Authenticator.setDefault(new WebAuthenticator(config));
    pack();
    setSize(1000, 800);
    centerApp(this);
    ivyFilterField.setEnabled(false);
    setVisible(true);

    setupTables();
  }

  @SuppressWarnings({ "unchecked" })
  private void setupTables()
  {
    try
    {
      SortedList<IvyPackage>      sortedPackages            = new SortedList<IvyPackage>(ivyRepositoryList, ivyFileComparator);
      FilterList<IvyPackage>      filteredPackages          = new FilterList<IvyPackage>(sortedPackages, ivyComponentMatcherEditor);
      FilterList<IvyPackage>      filteredPackages2         = new FilterList<IvyPackage>(filteredPackages, ivyCountValueMatcherEditor);
      EventTableModel<IvyPackage> ivyPackageEventTableModel = new EventTableModel<IvyPackage>(filteredPackages2, ivyFileTableFormat);

      ivyResultsTable.setModel(ivyPackageEventTableModel);

      TableComparatorChooser<IvyPackage> tableSorter       = new TableComparatorChooser<IvyPackage>(ivyResultsTable, sortedPackages, true);

      SortedList<Project>                sortedProjects    = new SortedList<Project>(projectUrls, projectFileComparator);
      FilterList<Project>                filteredProjects  = new FilterList<Project>(sortedProjects, projectComponentMatcherEditor);
      FilterList<Project>                filteredProjects2 = new FilterList<Project>(filteredProjects, projectCountValueMatcherEditor);
      EventTableModel<Project>           projectTableModel = new EventTableModel<Project>(filteredProjects2, projectsTableFormat);

      projectResultsTable.setModel(projectTableModel);

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
    setTitle("Ivy Usage Tracker v. " + Version.VERSION);
    addListeners();
    setPanelTitle(ivyRepositoryPanel, IVY_REPOSITORY, ivyRepositoryPath);
    setPanelTitle(projectRepositoryPanel, SUBVERSION_REPOSITORY, projectRepositoryPath);
  }

  private void addListeners()
  {
    ivyResultsTable.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mousePressed(MouseEvent e)
        {
          int                     row        = ivyResultsTable.getSelectedRow();
          EventTableModel         tableModel = (EventTableModel) ivyResultsTable.getModel();
          IvyPackage              ivyPackage = (IvyPackage) tableModel.getElementAt(row);
          FindUsingProjectsDialog dialog     = new FindUsingProjectsDialog(ivyPackage, ivyFilesMap);

          dialog.setVisible(true);
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

    // showProjectsTreeButton.addActionListener(new ActionListener()
    // {
    // public void actionPerformed(ActionEvent actionEvent)
    // {
    // showProjectsTree();
    // }
    // });

    // filterField.addKeyListener(new KeyAdapter()
    // {
    // @Override
    // public void keyReleased(KeyEvent e)
    // {
    // touchTa();
    // }
    // });
    quitButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          System.exit(0);
        }
      });

    // filterField.addActionListener(new ActionListener()
    // {
    // public void actionPerformed(ActionEvent e)
    // {
    // filterTable();
    // }
    // });
    ivyResultsTable.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mousePressed(MouseEvent e)
        {
          // showIvyLine(e);
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
    // showAllLibrariesRadioButton.addChangeListener(new ChangeListener()
    // {
    // public void stateChanged(ChangeEvent changeEvent)
    // {
    // // filterTable();
    // }
    // });
    // showOnlyUsedLibrariesRadioButton.addChangeListener(new ChangeListener()
    // {
    // public void stateChanged(ChangeEvent changeEvent)
    // {
    // // filterTable();
    // }
    // });
    // showOnlyUnusedLibrariesRadioButton.addChangeListener(new ChangeListener()
    // {
    // public void stateChanged(ChangeEvent changeEvent)
    // {
    // // filterTable();
    // }
    // });
    // exportTableToClipboardButton.addActionListener(new ActionListener()
    // {
    // public void actionPerformed(ActionEvent actionEvent)
    // {
    // exportTableToClipboard();
    // }
    // });
    // findMissingIvyFilesButton.addActionListener(new ActionListener()
    // {
    // public void actionPerformed(ActionEvent actionEvent)
    // {
    // findMissingIvyFiles();
    // }
    // });
  }

  private void setPanelTitle(JPanel panel, String text, String path)
  {
    panel.setBorder(createTitledBorder(createEtchedBorder(), text + path));
  }

  public void specifyIvyRepository()
  {
    FindMultiplePreferencesItemsDialog dialog = new FindMultiplePreferencesItemsDialog(config.getPreferences(),
                                                                                       "Select Ivy Repository",
                                                                                       LAST_IVY_REPOSITORY);

    dialog.setVisible(true);

    if (dialog.isOk())
    {
      String repositoryPath = dialog.getRepositoryLocation();

      if (!isEmpty(repositoryPath) && !repositoryPath.equals(ivyRepositoryPath))
      {
        ivyRepositoryPath = repositoryPath;
        config.setLastIvyRepository(repositoryPath);
        setPanelTitle(ivyRepositoryPanel, IVY_REPOSITORY, repositoryPath);
      }
    }
  }

  public void specifySubversionRepository()
  {
    FindMultiplePreferencesItemsDialog dialog = new FindMultiplePreferencesItemsDialog(config.getPreferences(),
                                                                                       "Select Subversion Repository",
                                                                                       LAST_SUBVERSION_REPOSITORY);

    dialog.setVisible(true);

    if (dialog.isOk())
    {
      String location = dialog.getRepositoryLocation();

      if (!isEmpty(location) && !location.equals(projectRepositoryPath))
      {
        projectRepositoryPath = location;
        config.setLastSubversionRepository(location);
        setPanelTitle(projectRepositoryPanel, SUBVERSION_REPOSITORY, location);
      }
    }
  }

  private void getBuildItems()  // throws MalformedURLException
  {
    ivyRepositoryPath = config.getLastIvyRepository();

    if (ivyRepositoryPath.length() == 0)
    {
      specifyIvyRepository();
    }

    if (projectRepositoryPath.length() == 0)
    {
      specifySubversionRepository();
    }

    startProgressPanel();

    Map<String, Map<String, Map<String, IvyPackage>>> packageMap = new HashMap<String, Map<String, Map<String, IvyPackage>>>();

    getAllIvyRepositoryPackages(packageMap);

    getAllIvyFilesInSubversionRepository();

    // todo get all ivy files in Subversion repository (reuse code if possible)

    // todo allow for reports - click on a project, see what's used.  Click on an ivy entry, see who uses it

    // todo allow filtering from text boxes for both projects and ivy entries

    System.out.println("Done!");
  }

  private void startProgressPanel()
  {
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
    BaseWebIvyRepositoryBrowserHandler webHandler = HandlerFactory.getIvyRepositoryHandler(this, ivyRepositoryPath, ivyRepositoryList, packageMap);

    webHandler.execute();

    // webHandler.doInBackground();
  }

  // ------------------------ INTERFACE METHODS ------------------------

  // --------------------- Interface UiMainFrame ---------------------

  @Override
  public void addStatus(String statusLine)
  {
    // To change body of implemented methods use File | Settings | File Templates.
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
    return isTest;
  }

  @Override
  public void setReady(boolean isReady)
  {
    stopProgressPanel();
    ivyFilterField.setEnabled(true);
    projectFilterField.setEnabled(true);
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
    progressPanel.stop();
    setNormalCursor();
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

    showMessageDialog(this, sb.toString(), "Here are the missing files:", JOptionPane.PLAIN_MESSAGE);
  }

  private void getAllIvyFilesInSubversionRepository()
  {
    // todo add the whole list of projects...
    String               repositoryBase       = config.getLastSubversionRepository();
    IvyFileFinderHandler ivyFileFinderHandler = HandlerFactory.getIvyFileFinderHandler(this, projectIvyFiles, projectUrls, repositoryBase);

    // ivyFileFinderHandler.doIt();
    //
    ivyFileFinderHandler.execute();
  }

  @SuppressWarnings({ "AssignmentToCollectionOrArrayFieldFromParameter" })
  public void populateTable(List<IvyPackage> projectIvyFiles, Map<Project, IvyPackage> ivyFilesMap)
  {
    // this.projectIvyFiles = projectIvyFiles;
    this.ivyFilesMap = ivyFilesMap;

    // repositoryList.addAll(allIvyFiles);
    progressPanel.stop();
    ivyFilterField.setEnabled(true);
  }

  public boolean saveTestData()
  {
    return saveTestData;
  }

  public void showNormal()
  {
    setNormalCursor();
    statusLabel.setText("");
    adjustColumnWidths();
    ivyFilterField.setEnabled(true);
    ivyFilterField.requestFocus();
  }

  @Override
  public void setNormalCursor()
  {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  private void adjustColumnWidths()  // todo make more general and run both tables through it.  Put into utils?
  {
    TableColumnModel columnModel = ivyResultsTable.getColumnModel();

    for (int col = 0; col < ivyResultsTable.getColumnCount(); col++)
    {
      int maxWidth = 0;

      for (int row = 0; row < ivyResultsTable.getRowCount(); row++)
      {
        TableCellRenderer cellRenderer      = ivyResultsTable.getCellRenderer(row, col);
        Object            value             = ivyResultsTable.getValueAt(row, col);
        Component         rendererComponent = cellRenderer.getTableCellRendererComponent(ivyResultsTable, value, false, false, row, col);

        maxWidth = max(rendererComponent.getPreferredSize().width, maxWidth);
      }

      TableColumn column = columnModel.getColumn(col);

      column.setPreferredWidth(maxWidth);
    }
  }

  public boolean useTestData()
  {
    return useTestData;
  }

  // --------------------------- main() method ---------------------------

  @SuppressWarnings({ "ResultOfObjectAllocationIgnored" })
  public static void main(String[] args)
  {
    IvyTrackerMainFrame frame = new IvyTrackerMainFrame();
  }
}
