package com.nurflugel.ivytracker;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import com.nurflugel.WebAuthenticator;
import com.nurflugel.common.ui.Version;
import com.nurflugel.common.ui.Util;
import static com.nurflugel.common.ui.Util.*;
import com.nurflugel.ivybrowser.InfiniteProgressPanel;
import com.nurflugel.ivybrowser.ui.BuilderMainFrame;
import com.nurflugel.ivybrowser.ui.IvyBrowserMainFrame;
import static com.nurflugel.ivybrowser.ui.IvyBrowserMainFrame.IVY_REPOSITORY;
import com.nurflugel.ivytracker.domain.CountValueMatcherEditor;
import com.nurflugel.ivytracker.domain.IvyFile;
import com.nurflugel.ivytracker.domain.IvyFileComparator;
import com.nurflugel.ivytracker.domain.IvyFileFilterator;
import com.nurflugel.ivytracker.handlers.HtmlHandler;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: May 30, 2008 Time: 11:38:58 AM To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({
                    "CallToPrintStackTrace", "UseOfSystemOutOrSystemErr", "IOResourceOpenedButNotSafelyClosed", "CallToSystemExit",
                    "OverlyBroadCatchBlock"
                  })
public class IvyTrackerMainFrame extends JFrame
{
  private static final long serialVersionUID = 8982188831570838035L;
  private InfiniteProgressPanel progressPanel = new InfiniteProgressPanel("Accessing the Ivy repository, please be patient");
  private boolean                    useTestData                        = false;  // if true, reads cannd data in from a file for fast testing
  private boolean                    saveTestData                       = false;  // if true, reads cannd data in from a file for fast testing
  private boolean                    isTest                             = false;  // if true, reads cannd data in from a file for fast testing
  private Cursor                     busyCursor                         = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
  private Cursor                     normalCursor                       = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
  private JButton                    quitButton;
  private JLabel                     statusLabel;
  private JTable                     resultsTable;
  private JTextField                 filterField;
  private EventList<IvyFile>         repositoryList                     = new BasicEventList<IvyFile>();
  private JPanel                     mainPanel;
  private JPanel                     buttonPanel;
  private JPanel                     statusPanel;
  private JRadioButton               showAllLibrariesRadioButton;
  private JRadioButton               showOnlyUsedLibrariesRadioButton;
  private JRadioButton               showOnlyUnusedLibrariesRadioButton;
  private JButton                    showProjectsTreeButton;
  private JButton                    exportTableToClipboardButton;
  private JButton                    findMissingIvyFilesButton;
  private JButton                    specifyIvyReposioryButton;
  private IvyFileComparator          ivyFileComparator;
  private TextComponentMatcherEditor textComponentMatcherEditor;
  private IvyFileTableFormat         ivyFileTableFormat;
  private Matcher<IvyFile>           countValueMatcherEditor;
  private List<IvyFile>              projectIvyFiles;
  private Map<String, IvyFile>       ivyFilesMap;
  private Set<String>                missingIvyFiles                    = new HashSet<String>();
  private Preferences                preferences                        = Preferences.userNodeForPackage(IvyTrackerMainFrame.class);

  // --------------------------- CONSTRUCTORS ---------------------------
  public IvyTrackerMainFrame()
  {
    initializeComponents();
    setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", this);
    ivyFileComparator          = new IvyFileComparator();
    textComponentMatcherEditor = new TextComponentMatcherEditor(filterField, new IvyFileFilterator());
    ivyFileTableFormat         = new IvyFileTableFormat();
    countValueMatcherEditor    = new CountValueMatcherEditor(showAllLibrariesRadioButton, showOnlyUsedLibrariesRadioButton,
                                                             showOnlyUnusedLibrariesRadioButton);
    Authenticator.setDefault(new WebAuthenticator());
    pack();
    setSize(1000, 800);
    BuilderMainFrame.centerApp(this);
    filterField.setEnabled(false);
    setVisible(true);
    setCursor(busyCursor);

    try
    {
      progressPanel.start();
      filterTable();
      getBuildItems();
    }
    catch (MalformedURLException e)
    {
      e.printStackTrace();
    }

    resultsTable.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mousePressed(MouseEvent e)
        {
          int                     row        = resultsTable.getSelectedRow();
          EventTableModel         tableModel = (EventTableModel) resultsTable.getModel();
          IvyFile                 ivyFile    = (IvyFile) tableModel.getElementAt(row);
          FindUsingProjectsDialog dialog     = new FindUsingProjectsDialog(ivyFile, ivyFilesMap);

          dialog.setVisible(true);
        }
      });
    specifyIvyReposioryButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent actionEvent)
        {
          IvyBrowserMainFrame.specifyRepository(preferences);
        }
      });
  }

  private void initializeComponents()
  {
    setGlassPane(progressPanel);
    setContentPane(mainPanel);
    filterField.setPreferredSize(new Dimension(200, 25));
    setTitle("Ivy Usage Tracker v. " + Version.VERSION);
    addListeners();
  }

  private void addListeners()
  {
    showProjectsTreeButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent actionEvent)
        {
          showProjectsTree();
        }
      });
    filterField.addKeyListener(new KeyAdapter()
      {
        @Override
        public void keyReleased(KeyEvent e)
        {
          filterTable();
        }
      });
    quitButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          System.exit(0);
        }
      });
    filterField.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          filterTable();
        }
      });
    resultsTable.addMouseListener(new MouseAdapter()
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
    showAllLibrariesRadioButton.addChangeListener(new ChangeListener()
      {
        public void stateChanged(ChangeEvent changeEvent)
        {
          filterTable();
        }
      });
    showOnlyUsedLibrariesRadioButton.addChangeListener(new ChangeListener()
      {
        public void stateChanged(ChangeEvent changeEvent)
        {
          filterTable();
        }
      });
    showOnlyUnusedLibrariesRadioButton.addChangeListener(new ChangeListener()
      {
        public void stateChanged(ChangeEvent changeEvent)
        {
          filterTable();
        }
      });
    exportTableToClipboardButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent actionEvent)
        {
          exportTableToClipboard();
        }
      });
    findMissingIvyFilesButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent actionEvent)
        {
          findMissingIvyFiles();
        }
      });
  }

  private void findMissingIvyFiles()
  {
    StringBuilder sb = new StringBuilder();

    for (String missingIvyFile : missingIvyFiles)
    {
      sb.append(missingIvyFile).append("\n");
    }

    JOptionPane.showMessageDialog(this, sb.toString(), "Here are the missing files:", JOptionPane.PLAIN_MESSAGE);
  }

  private void showProjectsTree()
  {
    ShowProjectsDialog showProjectsDialog = new ShowProjectsDialog(projectIvyFiles, ivyFilesMap);

    showProjectsDialog.setVisible(true);
  }

  private void exportTableToClipboard()
  {
    SortedList<IvyFile> sortedPackages   = new SortedList<IvyFile>(repositoryList, ivyFileComparator);
    FilterList<IvyFile> filteredPackages = new FilterList<IvyFile>(sortedPackages, textComponentMatcherEditor);
    FilterList<IvyFile> list             = new FilterList<IvyFile>(filteredPackages, countValueMatcherEditor);
    StringBuilder       sb               = new StringBuilder();

    sb.append("Org\tModule\tVersion\tCount\n");

    for (IvyFile ivyFile : list)
    {
      sb.append(ivyFile.getOrg()).append("\t").append(ivyFile.getModule()).append("\t").append(ivyFile.getVersion()).append("\t")
        .append(ivyFile.getCount()).append("\n");
    }

    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()), null);
    JOptionPane.showMessageDialog(this, "Formatted text has been pasted into your buffer");
  }

  private void filterTable()
  {
    SortedList<IvyFile>      sortedPackages    = new SortedList<IvyFile>(repositoryList, ivyFileComparator);
    FilterList<IvyFile>      filteredPackages  = new FilterList<IvyFile>(sortedPackages, textComponentMatcherEditor);
    FilterList<IvyFile>      filteredPackages2 = new FilterList<IvyFile>(filteredPackages, countValueMatcherEditor);
    EventTableModel<IvyFile> tableModel        = new EventTableModel<IvyFile>(filteredPackages2, ivyFileTableFormat);

    resultsTable.setModel(tableModel);

    TableComparatorChooser<IvyFile> tableSorter = new TableComparatorChooser<IvyFile>(resultsTable, sortedPackages, true);
  }

  private void getBuildItems() throws MalformedURLException
  {
    Authenticator.setDefault(new WebAuthenticator());

    String ivyRepositoryPath = preferences.get(IVY_REPOSITORY, "");

    if (ivyRepositoryPath.length() == 0)
    {
      ivyRepositoryPath = IvyBrowserMainFrame.specifyRepository(preferences);
    }

    URL startingUrl = new URL(ivyRepositoryPath);

    // HtmlHandler handler = new HtmlHandler(this, startingUrl, repositoryList);
    HtmlHandler handler = new HtmlHandler(this, startingUrl, new BasicEventList<IvyFile>(), missingIvyFiles);

    // handler.doInBackground();
    handler.execute();
    System.out.println("Done!");
  }

  public Preferences getPreferences()
  {
    return preferences;
  }

  // -------------------------- OTHER METHODS --------------------------
  public boolean isTest()
  {
    return isTest;
  }

  @SuppressWarnings({ "AssignmentToCollectionOrArrayFieldFromParameter" })
  public void populateTable(Collection<IvyFile> allIvyFiles, List<IvyFile> projectIvyFiles, Map<String, IvyFile> ivyFilesMap)
  {
    this.projectIvyFiles = projectIvyFiles;
    this.ivyFilesMap     = ivyFilesMap;
    repositoryList.addAll(allIvyFiles);
    filterTable();
    progressPanel.stop();
    filterField.setEnabled(true);
  }

  public boolean saveTestData()
  {
    return saveTestData;
  }

  public void setStatusLabel(String text)
  {
    statusLabel.setText(text);
  }

  public void showNormal()
  {
    setCursor(normalCursor);
    statusLabel.setText("");
    adjustColumnWidths();
    filterField.setEnabled(true);
    filterField.requestFocus();
  }

  private void adjustColumnWidths()
  {
    TableColumnModel columnModel = resultsTable.getColumnModel();

    for (int col = 0; col < resultsTable.getColumnCount(); col++)
    {
      int maxWidth = 0;

      for (int row = 0; row < resultsTable.getRowCount(); row++)
      {
        TableCellRenderer cellRenderer      = resultsTable.getCellRenderer(row, col);
        Object            value             = resultsTable.getValueAt(row, col);
        Component         rendererComponent = cellRenderer.getTableCellRendererComponent(resultsTable, value, false, false, row, col);

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
