package com.nurflugel.externalsreporter.ui;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.UniqueList;
import static ca.odell.glazedlists.matchers.TextMatcherEditor.CONTAINS;
import static ca.odell.glazedlists.matchers.TextMatcherEditor.REGULAR_EXPRESSION;
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
import com.nurflugel.common.ui.Util;
import static com.nurflugel.Os.OS_X;
import static com.nurflugel.Os.findOs;
import static com.nurflugel.common.ui.Util.addHelpListener;
import static com.nurflugel.common.ui.Util.busyCursor;
import static com.nurflugel.common.ui.Util.center;
import static com.nurflugel.common.ui.Util.normalCursor;
import static com.nurflugel.common.ui.Util.setLookAndFeel;
import com.nurflugel.common.ui.Version;
import com.nurflugel.externalsreporter.ui.tree.ExternalTreeHandler;
import com.nurflugel.ivybrowser.AppPreferences;
import com.nurflugel.ivybrowser.InfiniteProgressPanel;
import com.nurflugel.ivybrowser.ui.CheckboxCellRenderer;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import static com.nurflugel.externalsreporter.ui.Config.REPOSITORY;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.FILES_AND_DIRECTORIES;
import static javax.swing.JTable.AUTO_RESIZE_OFF;
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
import java.io.File;
import java.io.IOException;
import static java.lang.Math.max;
import java.net.Authenticator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import javax.swing.*;
import static javax.swing.JFileChooser.OPEN_DIALOG;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.tmatesoft.svn.core.wc.SVNWCUtil.createDefaultAuthenticationManager;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: May 30, 2008 Time: 11:38:58 AM To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({ "CallToPrintStackTrace", "UseOfSystemOutOrSystemErr", "IOResourceOpenedButNotSafelyClosed", "CallToSystemExit" })
public class ExternalsFinderMainFrame extends JFrame implements UiMainFrame
{
  /** Use serialVersionUID for interoperability. */
  private static final long                    serialVersionUID                     = 7878527239782932441L;
  private boolean                              getTestDataFromFile;  // if true, reads canned data in from a file for fast testing
  private boolean                              isTest;               // if true, reads canned data in from a file for fast testing
  private ExternalTreeHandler                  treeHandler                          = new ExternalTreeHandler(true);
  private JButton                              quitButton;
  private JButton                              findDotButton;
  private JLabel                               statusLabel;
  private JPanel                               thePanel;
  private JProgressBar                         progressBar;
  private JButton                              addRepositoryButton;
  private JButton                              helpButton;
  private JRadioButton                         deepRecursiveSlowRadioButton;
  private JRadioButton                         shallowBranchTagsTrunkRadioButton;
  private JPanel                               repositoryCheckboxPanel;
  private JButton                              parseRepositoriesButton;
  private JCheckBox                            showAllExternalsForCheckBox;
  private JCheckBox                            showTagsCheckBox;
  private JCheckBox                            showBranchesCheckBox;
  private JCheckBox                            showTrunksCheckBox;
  private JTextField                           projectsFilterField;
  private JTextField                           externalsFilterField;
  private JTable                               projectsTable;
  private JTable                               externalsTable;
  private JButton                              generateReportButton;
  private JCheckBox                            trimHttpFromURLsCheckBox;
  private JButton                              clearPreviousResultsButton;
  private JRadioButton                         externalContainsRadioButton;
  private JRadioButton                         externalRegularExpressionRadioButton;
  private JRadioButton                         projectContainsRadioButton;
  private JRadioButton                         projectRegularExpressionRadioButton;
  private JCheckBox                            externalSelectAllCheckBox;
  private JCheckBox                            projectSelectAllCheckBox;
  private JButton                              clearProjectFilterFieldButton;
  private JButton                              clearExternalFilterFieldButton;
  private JSplitPane                           splitPane;
  private Os                                   os                                   = findOs();
  private Config                               config                               = new Config();
  private SubversionHandler                    subversionHandler                    = new SubversionHandler();
  private EventList<External>                  externalsList;
  private EventList<ProjectExternalReference>  projectsList;
  private Cursor                               busyCursor                           = getPredefinedCursor(WAIT_CURSOR);
  private Cursor                               normalCursor                         = getPredefinedCursor(DEFAULT_CURSOR);
  private FilterList<External>                 externalFilterList;
  private FilterList<ProjectExternalReference> projectFilterList;
  private InfiniteProgressPanel                progressPanel                        = new InfiniteProgressPanel("Scanning the Subversion repository, please be patient - click to cancel",
                                                                                                                this);
  private UniqueList<External> uniqueExternalsList;
  private UniqueList<ProjectExternalReference> uniqueProjectsList;

  public ExternalsFinderMainFrame()
  {
    /** Todo how to deal with changed or wrong passwords? */
    // WebAuthenticator webAuthenticator = new WebAuthenticator();
    // PasswordAuthentication authentication = webAuthenticator.getPasswordAuthentication();
    // Authenticator.setDefault(webAuthenticator);
    // Authenticator.setDefault(new WebAuthenticator(config));
    // $$$setupUI$$$();
    $$$setupUI$$$();
    Authenticator.setDefault(new WebAuthenticator(config.getUserName(), config.getPassword()));

    // Authenticator.setDefault(new WebAuthenticator());
    initializeUi();
  }

  private void initializeUi()
  {
    setTitle("Subversion Externals Finder v. " + Version.VERSION);
    addStatus("");
    setCursor(busyCursor);

    // os.setLookAndFeel(this);why doesn't this work???
    try
    {
      Container container = getContentPane();

      container.add(thePanel);
      addRepositoryButton.requestFocus();
      validateDotPath();
      setGlassPane(progressPanel);

      // todo set up checkbox with last saved repository like IvyBrowser
      setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", this);
      trimHttpFromURLsCheckBox.setSelected(config.isTrimHttpFromUrls());
      shallowBranchTagsTrunkRadioButton.setSelected(config.isShallowScan());
      deepRecursiveSlowRadioButton.setSelected(!config.isShallowScan());
      showBranchesCheckBox.setSelected(config.isShowBranches());
      showTrunksCheckBox.setSelected(config.isShowTrunks());
      showTagsCheckBox.setSelected(config.isShowTags());

      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      int       maxWidth   = (int) Math.min(1500, screenSize.getWidth());
      int       maxHeight  = (int) Math.min(1000, screenSize.getHeight());

      setSize(new Dimension(maxWidth, maxHeight));
      setupGlazedLists();
      addListeners();
      addRepository(config.getLastRepository());
      center(this);
      setVisible(true);

      UsernamePasswordDialog passwordDialog = new UsernamePasswordDialog(config);

      passwordDialog.setVisible(true);

      // sizeTableColumns(externalsTable);
      // sizeTableColumns(projectsTable);
      addStatus("Enter one or more repository to search...");
      setCursor(normalCursor);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      showMessageDialog(this, "The application has experienced a fatal error\n" + e.toString());
      System.exit(1);
    }
  }

  /** toggle the buttons on/off based on the validity of the dot executable - don't let them proceed wihtout a valid value. */
  private void validateDotPath()
  {
    File    dotPath        = config.getDotExecutablePath();
    String  path           = dotPath.getName();
    boolean isDotPathValid = dotPath.exists();

    // this doesn't work on OS X... as some versions of GraphViz don't ohave "dot"
    if (os != OS_X)
    {
      isDotPathValid &= path.startsWith("dot");
    }

    parseRepositoriesButton.setEnabled((repositoryCheckboxPanel.getComponents().length > 0) && isDotPathValid);
    addRepositoryButton.setEnabled(isDotPathValid);

    if (!isDotPathValid)
    {
      findDotButton.requestFocus();
    }
  }

  @SuppressWarnings({ "unchecked" })
  private void setupGlazedLists()
  {
    externalsList       = new BasicEventList<External>();
    uniqueExternalsList = new UniqueList<External>(externalsList);

    SortedList<External>       sortedExternals = new SortedList<External>(uniqueExternalsList);
    TextComponentMatcherEditor externalsEditor = new TextComponentMatcherEditor(externalsFilterField, new ExternalsFilterator());

    externalsEditor.setMode(externalContainsRadioButton.isSelected() ? CONTAINS
                                                                     : REGULAR_EXPRESSION);
    externalFilterList = new FilterList<External>(sortedExternals, externalsEditor);

    final EventTableModel<External> externalsTableModel = new EventTableModel<External>(externalFilterList,
                                                                                        new ExternalsTableFormat(trimHttpFromURLsCheckBox));

    externalsTable.setModel(externalsTableModel);
    externalsTable.setDefaultRenderer(Object.class, new CheckboxCellRenderer(false));

    TableComparatorChooser<External> externalsTableSorter = new TableComparatorChooser<External>(externalsTable, sortedExternals, true);

    projectsList       = new BasicEventList<ProjectExternalReference>();
    uniqueProjectsList = new UniqueList<ProjectExternalReference>(projectsList);

    SortedList<ProjectExternalReference> sortedProjects = new SortedList<ProjectExternalReference>(uniqueProjectsList);
    TextComponentMatcherEditor           projectsEditor = new TextComponentMatcherEditor(projectsFilterField, new ProjectsFilterator());

    projectsEditor.setMode(projectContainsRadioButton.isSelected() ? CONTAINS
                                                                   : REGULAR_EXPRESSION);
    projectFilterList = new FilterList<ProjectExternalReference>(sortedProjects, projectsEditor);

    final EventTableModel<ProjectExternalReference> projectsTableModel = new EventTableModel<ProjectExternalReference>(projectFilterList,
                                                                                                                       new ProjectsTableFormat(trimHttpFromURLsCheckBox));

    projectsTable.setModel(projectsTableModel);
    projectsTable.setDefaultRenderer(Object.class, new CheckboxCellRenderer(false));
    externalsTable.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mouseClicked(MouseEvent e)
        {
          Point origin = e.getPoint();
          int   row    = externalsTable.rowAtPoint(origin);
          int   column = externalsTable.columnAtPoint(origin);

          if ((row != -1) && (column == 0))
          {
            External external = externalsTableModel.getElementAt(row);

            external.setSelected(!external.isSelected());

            // hack to make selection show
            uniqueExternalsList.add(external);
          }
        }
      });
    projectsTable.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mouseClicked(MouseEvent e)
        {
          Point origin = e.getPoint();
          int   row    = projectsTable.rowAtPoint(origin);
          int   column = projectsTable.columnAtPoint(origin);

          if ((row != -1) || (column == 0))
          {
            ProjectExternalReference reference = projectsTableModel.getElementAt(row);

            reference.setSelected(!reference.isSelected());

            // hack to make selection show
            uniqueProjectsList.add(reference);
          }
        }
      });

    TableComparatorChooser<ProjectExternalReference> projectsTableSorter = new TableComparatorChooser<ProjectExternalReference>(projectsTable,
                                                                                                                                sortedProjects, true);
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
          scanRepositories();
        }
      });
    generateReportButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          generateReport();
        }
      });
    clearPreviousResultsButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          externalsList.clear();
          projectsList.clear();
          generateReportButton.setEnabled(false);
          clearPreviousResultsButton.setEnabled(false);
        }
      });
    trimHttpFromURLsCheckBox.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          config.setTrimHttpFromUrls(trimHttpFromURLsCheckBox.isSelected());
          refreshExternals();
          refreshProjects();
        }
      });
    externalSelectAllCheckBox.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          toggleAllExternalItems(externalSelectAllCheckBox.isSelected(), externalFilterList);
        }
      });
    projectSelectAllCheckBox.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          toggleAllItems(projectSelectAllCheckBox.isSelected(), projectFilterList);
        }
      });
    clearProjectFilterFieldButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          projectsFilterField.setText("");
        }
      });
    clearExternalFilterFieldButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          externalsFilterField.setText("");
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
    showBranchesCheckBox.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          config.setShowBranches(showBranchesCheckBox.isSelected());
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
    shallowBranchTagsTrunkRadioButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          config.setShallowScan(shallowBranchTagsTrunkRadioButton.isSelected());
        }
      });
    deepRecursiveSlowRadioButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          config.setShallowScan(deepRecursiveSlowRadioButton.isSelected());
        }
      });
    addHelpListener("externalsFinderHelp.hs", helpButton, this);
    splitPane.addPropertyChangeListener(new PropertyChangeListener()
      {
        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
          if (evt.getPropertyName().equals("dividerLocation"))
          {
            resizeTableColumns(projectsTable);
            resizeTableColumns(externalsTable);
          }
        }
      });
  }

  private void doQuitAction()
  {
    config.saveSettings();
    System.exit(0);
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
    fileChooser.setFileSelectionMode(FILES_AND_DIRECTORIES);

    int choice = fileChooser.showDialog(this, "Select the DOT executable");

    if (choice == APPROVE_OPTION)
    {
      File dotExecutableFile = fileChooser.getSelectedFile();

      if (dotExecutableFile != null)
      {
        dotExecutablePath = dotExecutableFile.getAbsolutePath();
        config.setDotExecutablePath(dotExecutablePath);
      }
      else
      {
        showMessageDialog(this, "Sorry, this program can't run without the GraphViz installation.\n" + "  Please install that and try again");
      }
    }

    validateDotPath();
  }

  /** Add a repository to the list. */
  private void addRepository()
  {
    String lastRepository = config.getLastRepository();

    // FindMultiplePreferencesItemsDialog dialog         = new FindMultiplePreferencesItemsDialog(config.getPreferences(), "Select Ivy Repository",
    AppPreferences                     appPreferences = new AppPreferences(config.getPreferences());
    FindMultiplePreferencesItemsDialog dialog         = new FindMultiplePreferencesItemsDialog(appPreferences, "Select Ivy Repository", REPOSITORY);

    dialog.setVisible(true);

    if (dialog.isOk())
    {
      String newUrl = dialog.getRepositoryLocation();

      if (!isEmpty(newUrl))
      {
        addRepository(newUrl);
      }
    }
  }

  /** Go through the repositories and see what's there for externals. */
  private void scanRepositories()
  {
    setCursor(busyCursor);
    progressPanel.start();

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

    // set the authentication manager credentials
    ISVNAuthenticationManager authenticationManager = createDefaultAuthenticationManager(config.getUserName(), config.getPassword());
    ScanExternalsTask         task                  = new ScanExternalsTask(repositoryUrls, this, shallowBranchTagsTrunkRadioButton.isSelected(),
                                                                            subversionHandler, showBranchesCheckBox.isSelected(),
                                                                            showTagsCheckBox.isSelected(), showTrunksCheckBox.isSelected(),
                                                                            externalsList, projectsList, externalSelectAllCheckBox.isSelected(),
                                                                            projectSelectAllCheckBox.isSelected(), authenticationManager);

    try
    {
      task.execute();

      // task.doInBackground();
    }
    catch (Exception e)
    {
      e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
    }
  }

  private void generateReport()
  {
    processExternals(externalsList, projectsList);
  }

  /** Take the list of externals and projects, and build up a dot file for it. */
  public void processExternals(EventList<External> externalsList, EventList<ProjectExternalReference> projectsList)
  {
    setBusyCursor();

    try
    {
      OutputHandler outputHandler = new OutputHandler(this);
      File          dotExecutable = config.getDotExecutablePath();
      File          dotFile       = outputHandler.writeDotFile(externalsList, projectsList);

      if (dotFile != null)
      {
        File imageFile = outputHandler.launchDot(dotFile, dotExecutable);

        outputHandler.viewResultingFile(imageFile);
        dotFile.deleteOnExit();
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    finally
    {
      setNormalCursor();
    }
  }

  @Override
  public void setBusyCursor()
  {
    setCursor(Util.busyCursor);
  }

  /** Cheesy, but this forces the list to redisplay. */
  private void refreshExternals()
  {
    List<External> list = new ArrayList<External>(uniqueExternalsList);

    for (External external : list)
    {
      uniqueExternalsList.add(external);
    }
  }

  /** Cheesy, but this forces the list to redisplay. */
  private void refreshProjects()
  {
    List<ProjectExternalReference> list = new ArrayList<ProjectExternalReference>(uniqueProjectsList);

    for (ProjectExternalReference reference : list)
    {
      uniqueProjectsList.add(reference);
    }
  }

  /** Switch all the items in the filter list to the value specified. */
  private void toggleAllExternalItems(boolean selected, FilterList<External> list)
  {
    for (External selectable : list)
    {
      selectable.setSelected(selected);
      list.add(selectable);
    }
  }

  /** Switch all the items in the filter list to the value specified. */
  private void toggleAllItems(boolean selected, FilterList<ProjectExternalReference> list)
  {
    for (ProjectExternalReference selectable : list)
    {
      selectable.setSelected(selected);
      list.add(selectable);
    }
  }

  /** Size the table columns to be just big enough to hold what they need. */
  private void resizeTableColumns(JTable table)
  {
    table.setAutoResizeMode(AUTO_RESIZE_OFF);

    TableColumnModel columnModel = table.getColumnModel();
    int              columnCount = table.getColumnCount();
    int              rowCount    = table.getRowCount();
    int[]            widths      = new int[columnCount];
    int              tableWidth  = table.getParent().getWidth();
    int              totalWidth  = 0;

    for (int col = 0; col < columnCount; col++)
    {
      int maxWidth = 70;  // no column is less than 70 pixels wide

      for (int row = 0; row < rowCount; row++)
      {
        TableCellRenderer cellRenderer      = table.getCellRenderer(row, col);
        Object            value             = table.getValueAt(row, col);
        Component         rendererComponent = cellRenderer.getTableCellRendererComponent(table, value, false, false, row, col);

        maxWidth = max(rendererComponent.getPreferredSize().width, maxWidth);
      }

      widths[col] = maxWidth;

      if (col == (columnCount - 1))
      {
        widths[col] = tableWidth - totalWidth;
      }
      else
      {
        totalWidth += widths[col];
      }
    }

    for (int col = 0; col < columnCount; col++)
    {
      TableColumn column = columnModel.getColumn(col);

      column.setPreferredWidth(widths[col]);

      // column.setMinWidth(widths[col]);
    }
  }

  /** Add a repository to the list. */
  private void addRepository(String newUrl)
  {
    if (isEmpty(newUrl))
    {
      return;
    }

    if (!newUrl.endsWith("/"))
    {
      newUrl += "/";
    }

    config.setLastRepository(newUrl);

    JCheckBox box = new JCheckBox(newUrl, true);

    repositoryCheckboxPanel.add(box);
    thePanel.validate();
    addStatus(" ");
    parseRepositoriesButton.setEnabled(true);
  }

  /** Size the table columns to be just big enough to hold what they need. */
  public static void sizeTableColumns(JTable table)
  {
    table.setAutoResizeMode(AUTO_RESIZE_OFF);

    TableColumnModel columnModel = table.getColumnModel();
    int              columnCount = table.getColumnCount();
    int[]            widths      = new int[columnCount];
    int              tableWidth  = table.getWidth();

    // if there are no rows, space the columns evenly
    widths[0] = 70;

    int totalWidth = widths[0];

    for (int col = 1; col < columnCount; col++)
    {
      widths[col] = tableWidth / (columnCount - 1);

      // widths[col] = (tableWidth - widths[0]) / (columnCount - 1);
      if (col == (columnCount - 1))
      {
        widths[col] = tableWidth - totalWidth;
      }
      else
      {
        totalWidth += widths[col];
      }
    }

    for (int col = 0; col < columnCount; col++)
    {
      TableColumn column = columnModel.getColumn(col);

      column.setPreferredWidth(widths[col]);
    }
  }

  @Override
  public void addStatus(String text)
  {
    System.out.println(text);
    statusLabel.setText(text);
    progressPanel.setText(text);
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
  @SuppressWarnings({ "BooleanMethodNameMustStartWithQuestion" })
  public boolean getTestDataFromFile()
  {
    return getTestDataFromFile;
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
  public void setReady(boolean isReady) {}

  @Override
  public void showSevereError(String message, Exception e)
  {
    // todo
  }

  @Override
  public void stopThreads() {}

  @Override
  public void setStatusLabel(String text)
  {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void stopProgressPanel()
  {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void resizeTableColumns()
  {
    sizeTableColumns(externalsTable);
    sizeTableColumns(projectsTable);
  }

  // -------------------------- OTHER METHODS --------------------------
  private void createUIComponents()
  {
    repositoryCheckboxPanel = new JPanel();

    BoxLayout layout = new BoxLayout(repositoryCheckboxPanel, Y_AXIS);

    repositoryCheckboxPanel.setLayout(layout);
  }

  public Config getConfig()
  {
    return config;
  }

  void processResults()
  {
    generateReportButton.setEnabled(true);
    clearPreviousResultsButton.setEnabled(true);
    setStatus("");
    resizeTableColumns(externalsTable);
    resizeTableColumns(projectsTable);
    setNormalCursor();
  }

  public void setStatus(String text)
  {
    statusLabel.setText(text);
    progressPanel.setText(text);
  }

  @Override
  public void setNormalCursor()
  {
    setCursor(Util.normalCursor);
    progressPanel.stop();
  }

  // --------------------------- main() method ---------------------------
  public static void main(String[] args)
  {
    ExternalsFinderMainFrame mainFrame = new ExternalsFinderMainFrame();
  }

  /**
   * Method generated by IntelliJ IDEA GUI Designer >>> IMPORTANT!! <<< DO NOT edit this method OR call it in your code!
   *
   * @noinspection  ALL
   */
  private void $$$setupUI$$$()
  {
    createUIComponents();
    thePanel = new JPanel();
    thePanel.setLayout(new GridBagLayout());

    final JLabel label1 = new JLabel();

    label1.setFont(new Font("Arial", Font.BOLD, 28));
    label1.setText("Subversion Externals Finder");

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
    gbc.gridy   = 2;
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

    panel4.setLayout(new GridLayoutManager(3, 7, new Insets(0, 0, 0, 0), -1, -1));
    panel3.add(panel4, BorderLayout.CENTER);

    final JPanel panel5 = new JPanel();

    panel5.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
    panel4.add(panel5,
               new GridConstraints(2, 3, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    quitButton = new JButton();
    quitButton.setText("Quit");
    quitButton.setMnemonic('Q');
    quitButton.setDisplayedMnemonicIndex(0);
    panel5.add(quitButton,
               new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));
    helpButton = new JButton();
    helpButton.setText("Help");
    panel5.add(helpButton,
               new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));

    final Spacer spacer1 = new Spacer();

    panel5.add(spacer1,
               new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                                   1, null, null, null, 0, false));

    final Spacer spacer2 = new Spacer();

    panel5.add(spacer2,
               new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                                   1, null, null, null, 0, false));

    final JPanel panel6 = new JPanel();

    panel6.setLayout(new GridLayoutManager(5, 3, new Insets(0, 0, 0, 0), -1, -1));
    panel4.add(panel6,
               new GridConstraints(1, 1, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    generateReportButton = new JButton();
    generateReportButton.setEnabled(false);
    generateReportButton.setText("Generate report");
    panel6.add(generateReportButton,
               new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));
    parseRepositoriesButton = new JButton();
    parseRepositoriesButton.setEnabled(false);
    parseRepositoriesButton.setText("Parse Repository(ies)");
    parseRepositoriesButton.setMnemonic('P');
    parseRepositoriesButton.setDisplayedMnemonicIndex(0);
    panel6.add(parseRepositoriesButton,
               new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));
    addRepositoryButton = new JButton();
    addRepositoryButton.setText("Add repository");
    addRepositoryButton.setMnemonic('A');
    addRepositoryButton.setDisplayedMnemonicIndex(0);
    panel6.add(addRepositoryButton,
               new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));
    findDotButton = new JButton();
    findDotButton.setText("Find DOT executable");
    findDotButton.setMnemonic('D');
    findDotButton.setDisplayedMnemonicIndex(5);
    panel6.add(findDotButton,
               new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));

    final Spacer spacer3 = new Spacer();

    panel6.add(spacer3,
               new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                                   1, null, null, null, 0, false));

    final Spacer spacer4 = new Spacer();

    panel6.add(spacer4,
               new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                                   1, null, null, null, 0, false));
    clearPreviousResultsButton = new JButton();
    clearPreviousResultsButton.setEnabled(false);
    clearPreviousResultsButton.setText("Clear previous results");
    panel6.add(clearPreviousResultsButton,
               new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));

    final JPanel panel7 = new JPanel();

    panel7.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    panel4.add(panel7,
               new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    panel7.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Options"));

    final JPanel panel8 = new JPanel();

    panel8.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
    panel7.add(panel8);
    panel8.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Include level"));
    showTagsCheckBox = new JCheckBox();
    showTagsCheckBox.setText("Show tags (lots!)");
    showTagsCheckBox.setMnemonic('S');
    showTagsCheckBox.setDisplayedMnemonicIndex(0);
    panel8.add(showTagsCheckBox,
               new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));
    showBranchesCheckBox = new JCheckBox();
    showBranchesCheckBox.setText("Show branches");
    showBranchesCheckBox.setMnemonic('B');
    showBranchesCheckBox.setDisplayedMnemonicIndex(5);
    panel8.add(showBranchesCheckBox,
               new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));
    showTrunksCheckBox = new JCheckBox();
    showTrunksCheckBox.setSelected(true);
    showTrunksCheckBox.setText("Show trunks");
    showTrunksCheckBox.setMnemonic('T');
    showTrunksCheckBox.setDisplayedMnemonicIndex(5);
    panel8.add(showTrunksCheckBox,
               new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));

    final JPanel panel9 = new JPanel();

    panel9.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
    panel7.add(panel9);
    panel9.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Search Depth"));
    deepRecursiveSlowRadioButton = new JRadioButton();
    deepRecursiveSlowRadioButton.setEnabled(true);
    deepRecursiveSlowRadioButton.setText("Deep (recursive, slow)");
    deepRecursiveSlowRadioButton.setMnemonic('E');
    deepRecursiveSlowRadioButton.setDisplayedMnemonicIndex(1);
    panel9.add(deepRecursiveSlowRadioButton,
               new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));
    shallowBranchTagsTrunkRadioButton = new JRadioButton();
    shallowBranchTagsTrunkRadioButton.setSelected(true);
    shallowBranchTagsTrunkRadioButton.setText("Shallow (branch/tags/trunk, fast)");
    shallowBranchTagsTrunkRadioButton.setMnemonic('H');
    shallowBranchTagsTrunkRadioButton.setDisplayedMnemonicIndex(1);
    panel9.add(shallowBranchTagsTrunkRadioButton,
               new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));
    showAllExternalsForCheckBox = new JCheckBox();
    showAllExternalsForCheckBox.setEnabled(false);
    showAllExternalsForCheckBox.setText("Show all externals for same project");
    showAllExternalsForCheckBox.setMnemonic('O');
    showAllExternalsForCheckBox.setDisplayedMnemonicIndex(2);
    panel9.add(showAllExternalsForCheckBox,
               new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));
    trimHttpFromURLsCheckBox = new JCheckBox();
    trimHttpFromURLsCheckBox.setSelected(true);
    trimHttpFromURLsCheckBox.setText("Trim \"http...\" from URLs");
    panel7.add(trimHttpFromURLsCheckBox);

    final JPanel panel10 = new JPanel();

    panel10.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
    panel1.add(panel10, BorderLayout.CENTER);
    panel10.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), ""));
    splitPane = new JSplitPane();
    splitPane.setContinuousLayout(true);
    splitPane.setOneTouchExpandable(true);
    panel10.add(splitPane,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null,
                                    0, false));

    final JPanel panel11 = new JPanel();

    panel11.setLayout(new BorderLayout(0, 0));
    splitPane.setLeftComponent(panel11);

    final JPanel panel12 = new JPanel();

    panel12.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
    panel11.add(panel12, BorderLayout.NORTH);

    final JLabel label2 = new JLabel();

    label2.setText("Filter text:");
    panel12.add(label2,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                                    GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    externalsFilterField = new JTextField();
    panel12.add(externalsFilterField,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                                    GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));

    final JPanel panel13 = new JPanel();

    panel13.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
    panel13.setVisible(false);
    panel12.add(panel13,
                new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    panel13.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Matching condition"));
    externalContainsRadioButton = new JRadioButton();
    externalContainsRadioButton.setSelected(true);
    externalContainsRadioButton.setText("Contains");
    panel13.add(externalContainsRadioButton,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                    null, null, null, 0, false));
    externalRegularExpressionRadioButton = new JRadioButton();
    externalRegularExpressionRadioButton.setText("Regular Expression");
    panel13.add(externalRegularExpressionRadioButton,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                    null, null, null, 0, false));
    externalSelectAllCheckBox = new JCheckBox();
    externalSelectAllCheckBox.setSelected(false);
    externalSelectAllCheckBox.setText("Select/Unselect all");
    panel12.add(externalSelectAllCheckBox,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                    null, null, null, 0, false));
    clearExternalFilterFieldButton = new JButton();
    clearExternalFilterFieldButton.setText("Clear");
    panel12.add(clearExternalFilterFieldButton,
                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                    null, null, null, 0, false));

    final JScrollPane scrollPane1 = new JScrollPane();

    panel11.add(scrollPane1, BorderLayout.CENTER);
    externalsTable = new JTable();
    externalsTable.setToolTipText("table of externals");
    scrollPane1.setViewportView(externalsTable);

    final JPanel panel14 = new JPanel();

    panel14.setLayout(new BorderLayout(0, 0));
    splitPane.setRightComponent(panel14);

    final JPanel panel15 = new JPanel();

    panel15.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
    panel14.add(panel15, BorderLayout.NORTH);
    projectsFilterField = new JTextField();
    panel15.add(projectsFilterField,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                                    GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));

    final JLabel label3 = new JLabel();

    label3.setText("Filter text:");
    panel15.add(label3,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                                    GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

    final JPanel panel16 = new JPanel();

    panel16.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
    panel16.setVisible(false);
    panel15.add(panel16,
                new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    panel16.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Matching Condition"));
    projectContainsRadioButton = new JRadioButton();
    projectContainsRadioButton.setSelected(true);
    projectContainsRadioButton.setText("Contains");
    panel16.add(projectContainsRadioButton,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                    null, null, null, 0, false));
    projectRegularExpressionRadioButton = new JRadioButton();
    projectRegularExpressionRadioButton.setText("Regular Expression");
    panel16.add(projectRegularExpressionRadioButton,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                    null, null, null, 0, false));
    projectSelectAllCheckBox = new JCheckBox();
    projectSelectAllCheckBox.setSelected(false);
    projectSelectAllCheckBox.setText("Select/Unselect all");
    panel15.add(projectSelectAllCheckBox,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                    null, null, null, 0, false));
    clearProjectFilterFieldButton = new JButton();
    clearProjectFilterFieldButton.setText("Clear");
    panel15.add(clearProjectFilterFieldButton,
                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                    null, null, null, 0, false));

    final JScrollPane scrollPane2 = new JScrollPane();

    panel14.add(scrollPane2, BorderLayout.CENTER);
    projectsTable = new JTable();
    projectsTable.setToolTipText("Table of projects using externals");
    scrollPane2.setViewportView(projectsTable);

    final JPanel panel17 = new JPanel();

    panel17.setLayout(new BorderLayout(0, 0));
    gbc       = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.fill  = GridBagConstraints.BOTH;
    thePanel.add(panel17, gbc);
    panel17.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Repositories to search"));

    final JScrollPane scrollPane3 = new JScrollPane();

    panel17.add(scrollPane3, BorderLayout.CENTER);
    scrollPane3.setViewportView(repositoryCheckboxPanel);

    ButtonGroup buttonGroup;

    buttonGroup = new ButtonGroup();
    buttonGroup.add(shallowBranchTagsTrunkRadioButton);
    buttonGroup.add(deepRecursiveSlowRadioButton);
    buttonGroup = new ButtonGroup();
    buttonGroup.add(externalRegularExpressionRadioButton);
    buttonGroup.add(externalContainsRadioButton);
    buttonGroup = new ButtonGroup();
    buttonGroup.add(projectRegularExpressionRadioButton);
    buttonGroup.add(projectContainsRadioButton);
  }

  /** @noinspection  ALL */
  public JComponent $$$getRootComponent$$$()
  {
    return thePanel;
  }
}
