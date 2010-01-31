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
import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivybrowser.handlers.BaseWebIvyRepositoryBrowserHandler;
import com.nurflugel.ivybrowser.ui.HandlerFactory;
import com.nurflugel.ivytracker.domain.IvyFileComparator;
import com.nurflugel.ivytracker.domain.IvyFileFilterator;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.net.Authenticator;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import static com.nurflugel.common.ui.Util.centerApp;
import static com.nurflugel.common.ui.Util.setLookAndFeel;
import static com.nurflugel.ivybrowser.ui.IvyBrowserMainFrame.IVY_REPOSITORY;
import static org.apache.commons.lang.StringUtils.isEmpty;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: May 30, 2008 Time: 11:38:58 AM To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({
    "CallToPrintStackTrace", "UseOfSystemOutOrSystemErr", "IOResourceOpenedButNotSafelyClosed", "CallToSystemExit",
    "OverlyBroadCatchBlock"
    , "ParameterHidesMemberVariable"})
public class IvyTrackerMainFrame extends JFrame implements UiMainFrame
{
  private static final long          serialVersionUID                   = 8982188831570838035L;
  private InfiniteProgressPanel      progressPanel                      = new InfiniteProgressPanel("Accessing the Ivy repository, please be patient");
  private boolean                    useTestData                        = false;  // if true, reads cannd data in from a file for fast testing
  private boolean                    saveTestData                       = false;  // if true, reads cannd data in from a file for fast testing
  private boolean                    isTest                             = false;  // if true, reads cannd data in from a file for fast testing
  private Cursor                     busyCursor                         = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
  private Cursor                     normalCursor                       = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
  private JButton                    quitButton;
  private JLabel                     statusLabel;
  private EventList<IvyPackage> ivyRepositoryList = new BasicEventList<IvyPackage>();
  private JPanel                     mainPanel;
  private JTextField                 ivyFilterField;
  private JTable                     ivyResultsTable;
  private JButton addSubversionRepositoryButton;
  private JButton specifyIvyRepositoryButton;
  private JButton showProjectsUsingSelectedButton;
  private JButton showIvyModulesUsedButton;
  private JButton showUnusedIvyModulesButton;
  private JTable subversionResultsTable;
  private JTextField subversionFilterField;
  private JButton parseRepositoriesButton;
  private IvyFileComparator          ivyFileComparator;
  private TextComponentMatcherEditor textComponentMatcherEditor;
  private IvyFileTableFormat         ivyFileTableFormat;
  private Matcher<IvyPackage>        countValueMatcherEditor;
  private List<IvyPackage>           projectIvyFiles;
  private Map<String, IvyPackage>    ivyFilesMap;
  private Set<String>                missingIvyFiles                    = new HashSet<String>();

  // todo put into a config object
  private Preferences preferences       = Preferences.userNodeForPackage(IvyTrackerMainFrame.class);
  private String      ivyRepositoryPath;
  private static final String NEW_LINE = "\n";
  private static final String TAB = "\t";

  // --------------------------- CONSTRUCTORS ---------------------------

  public IvyTrackerMainFrame()
  {
    Authenticator.setDefault(new WebAuthenticator());

    initializeComponents();
    setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", this);
    ivyFileComparator          = new IvyFileComparator();
    textComponentMatcherEditor = new TextComponentMatcherEditor(ivyFilterField, new IvyFileFilterator());
    ivyFileTableFormat         = new IvyFileTableFormat();
//    countValueMatcherEditor    = new CountValueMatcherEditor(showAllLibrariesRadioButton, showOnlyUsedLibrariesRadioButton,
//                                                             showOnlyUnusedLibrariesRadioButton);
    Authenticator.setDefault(new WebAuthenticator());
    pack();
    setSize(1000, 800);
    centerApp(this);
    ivyFilterField.setEnabled(false);
    setVisible(true);
//    setCursor(busyCursor);

    try
    {
//      progressPanel.start();

      SortedList<IvyPackage>      sortedPackages    = new SortedList<IvyPackage>(ivyRepositoryList, ivyFileComparator);
      FilterList<IvyPackage>      filteredPackages  = new FilterList<IvyPackage>(sortedPackages, textComponentMatcherEditor);
      FilterList<IvyPackage>      filteredPackages2 = new FilterList<IvyPackage>(filteredPackages, countValueMatcherEditor);
      EventTableModel<IvyPackage> tableModel        = new EventTableModel<IvyPackage>(filteredPackages2, ivyFileTableFormat);

      ivyResultsTable.setModel(tableModel);

      TableComparatorChooser<IvyPackage> tableSorter = new TableComparatorChooser<IvyPackage>(ivyResultsTable, sortedPackages, true);

    }

    // catch (MalformedURLException e)
    catch (Exception e)
    {
      e.printStackTrace();
    }

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
          specifyRepository();
        }
      });
    parseRepositoriesButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
         getBuildItems();
      }
    });
  }

  private void initializeComponents()
  {
    setGlassPane(progressPanel);
    setContentPane(mainPanel);
//    ivyFilterField.setPreferredSize(new Dimension(200, 25));
    setTitle("Ivy Usage Tracker v. " + Version.VERSION);
    addListeners();
  }

  private void addListeners()
  {
//    showProjectsTreeButton.addActionListener(new ActionListener()
//      {
//        public void actionPerformed(ActionEvent actionEvent)
//        {
//          showProjectsTree();
//        }
//      });

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
//    showAllLibrariesRadioButton.addChangeListener(new ChangeListener()
//      {
//        public void stateChanged(ChangeEvent changeEvent)
//        {
//          // filterTable();
//        }
//      });
//    showOnlyUsedLibrariesRadioButton.addChangeListener(new ChangeListener()
//      {
//        public void stateChanged(ChangeEvent changeEvent)
//        {
//          // filterTable();
//        }
//      });
//    showOnlyUnusedLibrariesRadioButton.addChangeListener(new ChangeListener()
//      {
//        public void stateChanged(ChangeEvent changeEvent)
//        {
//          // filterTable();
//        }
//      });
//    exportTableToClipboardButton.addActionListener(new ActionListener()
//      {
//        public void actionPerformed(ActionEvent actionEvent)
//        {
//          exportTableToClipboard();
//        }
//      });
//    findMissingIvyFilesButton.addActionListener(new ActionListener()
//      {
//        public void actionPerformed(ActionEvent actionEvent)
//        {
//          findMissingIvyFiles();
//        }
//      });
  }

  private void showProjectsTree()
  {
    ShowProjectsDialog showProjectsDialog = new ShowProjectsDialog(projectIvyFiles, ivyFilesMap);

    showProjectsDialog.setVisible(true);
  }

  private void exportTableToClipboard()
  {
    SortedList<IvyPackage> sortedPackages   = new SortedList<IvyPackage>(ivyRepositoryList, ivyFileComparator);
    FilterList<IvyPackage> filteredPackages = new FilterList<IvyPackage>(sortedPackages, textComponentMatcherEditor);
    FilterList<IvyPackage> list             = new FilterList<IvyPackage>(filteredPackages, countValueMatcherEditor);
    StringBuilder          sb               = new StringBuilder();

    sb.append("Org\tModule\tVersion\tCount\n");

    for (IvyPackage ivyFile : list)
    {
      sb.append(ivyFile.getOrgName()).append(TAB).append(ivyFile.getModuleName()).append(TAB).append(ivyFile.getVersion()).append(TAB)
        .append(ivyFile.getCount()).append(NEW_LINE);
    }

    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()), null);
    JOptionPane.showMessageDialog(this, "Formatted text has been pasted into your buffer");
  }

  private void findMissingIvyFiles()
  {
    StringBuilder sb = new StringBuilder();

    for (String missingIvyFile : missingIvyFiles)
    {
      sb.append(missingIvyFile).append(NEW_LINE);
    }

    JOptionPane.showMessageDialog(this, sb.toString(), "Here are the missing files:", JOptionPane.PLAIN_MESSAGE);
  }

  public void specifyRepository()
  {
    FindMultiplePreferencesItemsDialog dialog = new FindMultiplePreferencesItemsDialog(preferences, "Select Ivy Repository", IVY_REPOSITORY);

    dialog.setVisible(true);

    if (dialog.isOk())
    {
      String ivyRepositoryPath = dialog.getRepositoryLocation();

      if (!isEmpty(ivyRepositoryPath) && !ivyRepositoryPath.equals(this.ivyRepositoryPath))
      {
        this.ivyRepositoryPath = ivyRepositoryPath;
        preferences.put(IVY_REPOSITORY, ivyRepositoryPath);

        // todo reparse();
//        try
//        {
//          getBuildItems();
//        }
//        catch (MalformedURLException e)
//        {
//          e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
//        }
      }
    }
  }

  private void getBuildItems()// throws MalformedURLException
  {
    ivyRepositoryPath = preferences.get(IVY_REPOSITORY, "");

    if (ivyRepositoryPath.length() == 0)
    {
      specifyRepository();
    }

    Map<String, Map<String, Map<String, IvyPackage>>> packageMap  = new HashMap<String, Map<String, Map<String, IvyPackage>>>();

    getAllIvyRepositoryPackages(packageMap);

//    getAllIvyFilesInSubversionRepository();//todo add this when Ivy fetching is working

    // todo get all ivy files in Subverison repository (reuse code if possible)

    // todo allow for reports - click on a project, see what's used.  Click on an ivy entry, see who uses it

    // todo allow filtering from text boxes for both projects and ivy entries



    System.out.println("Done!");
  }

  private void getAllIvyFilesInSubversionRepository()
  {
    String repositoryBase = "http://ivy-tools.googlecode.com/svn";
    Map<String,IvyPackage> ivyFiles=new HashMap<String, IvyPackage>();
    HandlerFactory.getIvyFileFinderHandler(this,ivyFiles,repositoryBase);

  }

  /** get all Ivy repository (reuse code). */
  private void getAllIvyRepositoryPackages(Map<String, Map<String, Map<String, IvyPackage>>> packageMap)
  {
    BaseWebIvyRepositoryBrowserHandler webHandler = HandlerFactory.getIvyRepositoryHandler(this, ivyRepositoryPath, ivyRepositoryList, packageMap);

    webHandler.doInBackground();
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
  public void setBusyCursor()
  {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void setNormalCursor()
  {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void setReady(boolean isReady)
  {
    // To change body of implemented methods use File | Settings | File Templates.
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
    // To change body of implemented methods use File | Settings | File Templates.
  }

  // -------------------------- OTHER METHODS --------------------------

  public Preferences getPreferences()
  {
    return preferences;
  }

  @SuppressWarnings({ "AssignmentToCollectionOrArrayFieldFromParameter" })
  public void populateTable(List<IvyPackage> projectIvyFiles, Map<String, IvyPackage> ivyFilesMap)
  {
    this.projectIvyFiles = projectIvyFiles;
    this.ivyFilesMap     = ivyFilesMap;

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
    setCursor(normalCursor);
    statusLabel.setText("");
    adjustColumnWidths();
    ivyFilterField.setEnabled(true);
    ivyFilterField.requestFocus();
  }

  private void adjustColumnWidths()// todo make more general and run both tables through it.  Put into utils?
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

        maxWidth = Math.max(rendererComponent.getPreferredSize().width, maxWidth);
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
