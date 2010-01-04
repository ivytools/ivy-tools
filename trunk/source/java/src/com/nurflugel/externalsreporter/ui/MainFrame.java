package com.nurflugel.externalsreporter.ui;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import com.nurflugel.Os;
import com.nurflugel.WebAuthenticator;
import com.nurflugel.common.ui.UiMainFrame;
import com.nurflugel.common.ui.Util;
import com.nurflugel.common.ui.Version;
import com.nurflugel.externalsreporter.ui.tree.BranchNode;
import com.nurflugel.externalsreporter.ui.tree.ExternalTreeHandler;
import com.nurflugel.ivybrowser.ui.CheckboxCellRenderer;
import org.apache.commons.lang.StringUtils;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import static com.nurflugel.common.ui.Util.center;
import static com.nurflugel.common.ui.Util.setLookAndFeel;
import static javax.swing.JFileChooser.OPEN_DIALOG;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: May 30, 2008 Time: 11:38:58 AM To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({ "CallToPrintStackTrace", "UseOfSystemOutOrSystemErr", "IOResourceOpenedButNotSafelyClosed", "CallToSystemExit" })
public class MainFrame extends JFrame implements UiMainFrame
{
  /** Use serialVersionUID for interoperability. */
  private static final long                   serialVersionUID                  = 7878527239782932441L;
  private boolean                             getTestDataFromFile;  // if true, reads canned data in from a file for fast testing
  private boolean                             isTest;               // if true, reads canned data in from a file for fast testing
  private ExternalTreeHandler                 treeHandler                       = new ExternalTreeHandler(true);
  private JButton                             quitButton;
  private JButton                             findDotButton;
  private JLabel                              statusLabel;
  private JPanel                              thePanel;
  private JProgressBar                        progressBar;
  private JButton                             addRepositoryButton;
  private JButton                             helpButton;
  private JRadioButton                        deepRecursiveSlowRadioButton;
  private JRadioButton                        shallowBranchTagsTrunkRadioButton;
  private JPanel                              repositoryCheckboxPanel;
  private JButton                             parseRepositoriesButton;
  private JCheckBox                           showAllExternalsForCheckBox;
  private JCheckBox                           showTagsCheckBox;
  private JCheckBox                           showBranchesCheckBox;
  private JCheckBox                           showTrunksCheckBox;
  private JTextField                          projectsFilterField;
  private JTextField                          externalsFilterField;
  private JTable                              projectsTable;
  private JTable                              externalsTable;
  private JButton                             generateReportButton;
  private JCheckBox                           trimHttpFromURLsCheckBox;
  private Os                                  os                                = Os.findOs(System.getProperty("os.name"));
  private Config                              config                            = new Config();
  private SubversionHandler                   subversionHandler                 = new SubversionHandler();
  private EventList<External>                 externalsList;
  private EventList<ProjectExternalReference> projectsList;
  private Cursor                              busyCursor                        = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
  private Cursor                              normalCursor                      = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

  public MainFrame()
  {
    /** Todo how to deal with changed or wrong passwords? */
    Authenticator.setDefault(new WebAuthenticator(config));
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
      initializeComponents();
      setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", this);
      setSize(1000, 800);
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
    setupGlazedLists();
  }

  @SuppressWarnings({ "unchecked" })
  private void setupGlazedLists()
  {
    externalsList = new BasicEventList<External>();

    final UniqueList<External>      uniqueExternalsList = new UniqueList<External>(externalsList);
    SortedList<External>            sortedExternals     = new SortedList<External>(uniqueExternalsList);
    TextComponentMatcherEditor      externalsEditor     = new TextComponentMatcherEditor(externalsFilterField, new ExternalsFilterator());
    FilterList<External>            externalFilterList  = new FilterList<External>(sortedExternals, externalsEditor);
    final EventTableModel<External> externalsTableModel = new EventTableModel<External>(externalFilterList,
                                                                                        new ExternalsTableFormat(trimHttpFromURLsCheckBox));

    externalsTable.setModel(externalsTableModel);
    externalsTable.setDefaultRenderer(Object.class, new CheckboxCellRenderer(false));

    TableComparatorChooser<External> externalsTableSorter = new TableComparatorChooser<External>(externalsTable, sortedExternals, true);

    // EventList<External>         projectsEventList    = new BasicEventList<External>(projectsList);
    projectsList = new BasicEventList<ProjectExternalReference>();

    final UniqueList<ProjectExternalReference>      uniqueProjectsList = new UniqueList<ProjectExternalReference>(projectsList);
    final SortedList<ProjectExternalReference>      sortedProjects     = new SortedList<ProjectExternalReference>(uniqueProjectsList);
    TextComponentMatcherEditor                      projectsEditor     = new TextComponentMatcherEditor(projectsFilterField,
                                                                                                        new ProjectsFilterator());
    FilterList<ProjectExternalReference>            projectFilterList = new FilterList<ProjectExternalReference>(sortedProjects, projectsEditor);
    final EventTableModel<ProjectExternalReference> projectsTableModel = new EventTableModel<ProjectExternalReference>(projectFilterList,
                                                                                                                       new ProjectsTableFormat(trimHttpFromURLsCheckBox));

    projectsTable.setModel(projectsTableModel);
    projectsTable.setDefaultRenderer(Object.class, new CheckboxCellRenderer(false));

    sizeTableColumns(projectsTable);
    sizeTableColumns(externalsTable);

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

  private void initializeComponents()
  {
    Container container = getContentPane();

    container.add(thePanel);
    addRepositoryButton.requestFocus();
    validateDotPath();
  }

  /** Size the table columns to be just big enough to hold what they need. */
  private void sizeTableColumns(JTable table)
  {
    TableColumnModel columnModel = table.getColumnModel();

    for (int col = 0; col < table.getColumnCount(); col++)
    {
      int maxWidth = 0;

      for (int row = 0; row < table.getRowCount(); row++)
      {
        TableCellRenderer cellRenderer      = table.getCellRenderer(row, col);
        Object            value             = table.getValueAt(row, col);
        Component         rendererComponent = cellRenderer.getTableCellRendererComponent(table, value, false, false, row, col);

        maxWidth = Math.max(rendererComponent.getPreferredSize().width, maxWidth);
      }

      TableColumn column = columnModel.getColumn(col);

      column.setPreferredWidth(maxWidth * 100);
    }
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

    if (!isDotPathValid)
    {
      findDotButton.requestFocus();
    }
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
    generateReportButton.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          generateReport();
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
      }
    }

    validateDotPath();
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
  }

  /** Go through the repositories and see what's there for externals. */
  private void parseRepositories()
  {
    setCursor(busyCursor);

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

    ScanExternalsTask task = new ScanExternalsTask(repositoryUrls, this, shallowBranchTagsTrunkRadioButton.isSelected(), subversionHandler,
                                                   showBranchesCheckBox.isSelected(), showTagsCheckBox.isSelected(), showTrunksCheckBox.isSelected(),
                                                   externalsList, projectsList);

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
  public void setReady(boolean isReady) {}

  @Override
  public void showSevereError(String message, Exception e)
  {
    // todo
  }

  // -------------------------- OTHER METHODS --------------------------

  private void createUIComponents()
  {
    repositoryCheckboxPanel = new JPanel();

    BoxLayout layout = new BoxLayout(repositoryCheckboxPanel, BoxLayout.Y_AXIS);

    repositoryCheckboxPanel.setLayout(layout);
  }

  public Config getConfig()
  {
    return config;
  }

  void processResults()
  {
    // ExternalResultsFilterSelector filterSelector = new ExternalResultsFilterSelector(externalsList);
    addExternalsToLists(externalsList);
    generateReportButton.setEnabled(true);
    setNormalCursor();
  }

  private void generateReport()
  {
    // todo now do something...  filter on external or project...
    // externalsList=filterSelector.getExternalsList();
    processExternals(externalsList, projectsList);
  }

  private void addExternalsToLists(List<External> externalList)
  {
    // Set<String> externalsSet = new TreeSet<String>();
    // Set<String> projectsSet  = new TreeSet<String>();
    //
    // for (External external : externalList)
    // {
    // externalsSet.add(external.getUrl());
    // projectsSet.add(external.getProjectBaseUrl());
    // }
    // todo dibble
    // for (String external : externalsSet)
    // {
    // externalsPanel.add(new JCheckBox(external));
    // }
    //
    // for (String project : projectsSet)
    // {
    // projectPanel.add(new JCheckBox(project));
    // }
  }

  /** Take the list of externals and projects, and build up a dot file for it. */
  public void processExternals(EventList<External> externalsList, EventList<ProjectExternalReference> projectsList)
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
      File          dotFile       = outputHandler.writeDotFile(externalsList, projectsList);
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

  @Override
  public void setNormalCursor()
  {
    setCursor(Util.normalCursor);
  }

  public void setStatus(String text)
  {
    statusLabel.setText(text);
  }

  private void getExternals(List<BranchNode> branches)
  {
    Authenticator.setDefault(new WebAuthenticator());

    ExternalsFinderTask externalsFinderTask = new ExternalsFinderTask(this, progressBar, branches);

    try
    {
      // externalsFinderTask.execute();
      externalsFinderTask.doInBackground();
      treeHandler.expandAll(false);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  // --------------------------- main() method ---------------------------

  public static void main(String[] args)
  {
    MainFrame mainFrame = new MainFrame();
  }
}
