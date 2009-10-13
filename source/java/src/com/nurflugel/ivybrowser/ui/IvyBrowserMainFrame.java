package com.nurflugel.ivybrowser.ui;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import com.nurflugel.WebAuthenticator;
import com.nurflugel.common.ui.Version;
import com.nurflugel.ivybrowser.InfiniteProgressPanel;
import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivybrowser.handlers.BaseWebHandler;
import com.nurflugel.ivytracker.IvyTrackerMainFrame;
import java.awt.*;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.Cursor.*;
import static java.awt.Cursor.*;
import static java.awt.Cursor.*;
import static java.awt.Cursor.getPredefinedCursor;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.*;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.showInputDialog;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.help.HelpSet;
import javax.help.HelpBroker;
import javax.help.CSH;
import javax.help.HelpSetException;

/** Main UI frame for the Ivy Browser. */
@SuppressWarnings({ "MethodParameterNamingConvention", "CallToPrintStackTrace", "MethodOnlyUsedFromInnerClass" })
public class IvyBrowserMainFrame extends JFrame
{
  public static final String IVY_REPOSITORY = "IvyRepository";
  private static final long serialVersionUID = 8982188831570838035L;
  private static final String PARSE_ON_OPEN = "parseOnOpen";
  private static final String SAVE_DIR = "saveDir";
  private Map<String, Map<String, Map<String, IvyPackage>>> packageMap = Collections.synchronizedMap(new HashMap<String, Map<String, Map<String, IvyPackage>>>());
  private InfiniteProgressPanel progressPanel = new InfiniteProgressPanel("Accessing the Ivy repository, please be patient");
  private Cursor           busyCursor          = getPredefinedCursor(Cursor.WAIT_CURSOR);
  private Cursor           normalCursor        = getPredefinedCursor(Cursor.DEFAULT_CURSOR);
  private JButton          specifyButton       = new JButton("Specify Repository");
  private JButton          reparseButton       = new JButton("Re-parse Repository");
  private JButton          quitButton          = new JButton("Quit");
  private JButton          helpButton          = new JButton("Help");
  private JLabel           findLabel           = new JLabel("Find library:");
  private JLabel           statusLabel         = new JLabel();
  private JCheckBox        parseOnOpenCheckbox = new JCheckBox("Parse Repository on Open", false);
  private JTable           resultsTable        = new JTable();
  private JTextField       libraryField        = new JTextField();
  private Preferences      preferences         = Preferences.userNodeForPackage(IvyBrowserMainFrame.class);
  private List<IvyPackage> repositoryList      = Collections.synchronizedList(new ArrayList<IvyPackage>());
  private JScrollPane      scrollPane;
  private JPanel           holdingPanel;
  private String           ivyRepositoryPath;

  // --------------------------- CONSTRUCTORS ---------------------------
  public IvyBrowserMainFrame()
  {
    initializeComponents();
    pack();
    setSize(800, 600);
    BuilderMainFrame.centerApp(this);

    // this was causing problems with GlazedLists throwing NPEs
    // IvyTrackerMainFrame.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", this);
    Authenticator.setDefault(new WebAuthenticator());
    libraryField.setEnabled(false);
    setVisible(true);

    boolean parseOnOpen = preferences.getBoolean(PARSE_ON_OPEN, false);

    // parseOnOpen = false;
    parseOnOpenCheckbox.setSelected(parseOnOpen);

    if (parseOnOpen)
    {
      reparse();
    }
  }

  private void initializeComponents()
  {
    JPanel mainPanel = new JPanel(new BorderLayout(0, 0));

    setGlassPane(progressPanel);
    setContentPane(mainPanel);
    libraryField.setPreferredSize(new Dimension(200, 25));
    setTitle("Ivy Repository Browser v. " + Version.VERSION);

    JPanel textPanel   = new JPanel();
    JPanel buttonPanel = new JPanel();

    holdingPanel = new JPanel();

    BoxLayout layout = new BoxLayout(holdingPanel, Y_AXIS);

    holdingPanel.setLayout(layout);
    textPanel.add(findLabel);
    textPanel.add(libraryField);
    buttonPanel.add(specifyButton);
    buttonPanel.add(reparseButton);
    buttonPanel.add(parseOnOpenCheckbox);
    buttonPanel.add(helpButton);
    buttonPanel.add(quitButton);
    holdingPanel.add(textPanel);
    holdingPanel.add(buttonPanel);
    scrollPane = new JScrollPane(resultsTable);
    holdingPanel.add(scrollPane);
    mainPanel.add(holdingPanel, CENTER);
    mainPanel.add(statusLabel, SOUTH);
    addListeners();
  }

  private void addListeners()
  {
    libraryField.addKeyListener(new KeyAdapter()
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
    reparseButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          reparse();
        }
      });
    specifyButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          String path = specifyRepository(preferences);

          if ((path != null) && (path.length() > 0))
          {
            reparse();
          }
        }
      });
    libraryField.addActionListener(new ActionListener()
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
          try
          {
            showIvyLine(e);
          }
          catch (IOException e1)
          {
            e1.printStackTrace();  // todo show error dialog
          }
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
    parseOnOpenCheckbox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent actionEvent)
        {
          boolean isSelected = parseOnOpenCheckbox.isSelected();

          preferences.putBoolean(PARSE_ON_OPEN, isSelected);
        }
      });

    ClassLoader classLoader = IvyBrowserMainFrame.class.getClassLoader();

    try
    {
      URL                       hsURL                 = HelpSet.findHelpSet(classLoader, "ivyBrowserHelp.hs");
      HelpSet                   helpSet               = new HelpSet(null, hsURL);
      HelpBroker                helpBroker            = helpSet.createHelpBroker();
      CSH.DisplayHelpFromSource displayHelpFromSource = new CSH.DisplayHelpFromSource(helpBroker);

      helpButton.addActionListener(displayHelpFromSource);
    }
    catch (HelpSetException ee)
    {                              // Say what the exception really is
      System.out.println("Exception! " + ee.getMessage());
      // LOGGER.error("HelpSet " + ee.getMessage());
      // LOGGER.error("HelpSet " + HELP_HS + " not found");
    }
  }

  public static String specifyRepository(Preferences appPreferences)
  {
    // String ivyRepositoryPath = appPreferences.get(IVY_REPOSITORY, "");
    FindIvyRepositoryDialog dialog = new FindIvyRepositoryDialog(appPreferences);

    dialog.setVisible(true);

    String ivyRepositoryPath = dialog.getRepositoryLocation();

    // ivyRepositoryPath = (String) showInputDialog(null, "What is the Ivy repository URL?", "Enter Ivy repository URL", QUESTION_MESSAGE, null, null,
    // ivyRepositoryPath); appPreferences.put(IVY_REPOSITORY, ivyRepositoryPath);
    return ivyRepositoryPath;
  }

  public void filterTable()
  {
    resultsTable.setModel(new DefaultTableModel());

    EventList<IvyPackage> eventList = new BasicEventList<IvyPackage>(repositoryList);
    SortedList<IvyPackage> sortedPackages = new SortedList<IvyPackage>(eventList);
    TextComponentMatcherEditor textComponentMatcherEditor = new TextComponentMatcherEditor(libraryField, new IvyPackageFilterator());
    FilterList<IvyPackage>      filteredPackages = new FilterList<IvyPackage>(sortedPackages, textComponentMatcherEditor);
    EventTableModel<IvyPackage> tableModel       = new EventTableModel<IvyPackage>(filteredPackages, new IvyPackageTableFormat());

    resultsTable.setModel(tableModel);

    TableComparatorChooser<IvyPackage> tableSorter = new TableComparatorChooser<IvyPackage>(resultsTable, sortedPackages, true);
  }

  @SuppressWarnings({ "UseOfSystemOutOrSystemErr" })
  private void showIvyLine(MouseEvent e) throws IOException
  {
    int row = resultsTable.getSelectedRow();

    if (row > -1)
    {
      setCursor(Cursor.WAIT_CURSOR);

      EventTableModel tableModel = (EventTableModel) resultsTable.getModel();
      IvyPackage      ivyFile    = (IvyPackage) tableModel.getElementAt(row);
      IvyLineDialog   dialog     = new IvyLineDialog(ivyFile, ivyRepositoryPath, this);

      setCursor(Cursor.DEFAULT_CURSOR);
      dialog.setVisible(true);
    }
    else
    {
      System.out.println("No row selected...");
    }
  }

  private void reparse()
  {
    setCursor(busyCursor);
    holdingPanel.remove(scrollPane);
    progressPanel.start();
    ivyRepositoryPath = preferences.get(IVY_REPOSITORY + 0, "");

    if (ivyRepositoryPath.equalsIgnoreCase(""))
    {
      ivyRepositoryPath = specifyRepository(preferences);
    }

    if (ivyRepositoryPath.length() > 0)
    {
      repositoryList.clear();
      packageMap.clear();

      BaseWebHandler handler = HandlerFactory.getHandler(this, ivyRepositoryPath, repositoryList, packageMap);

      handler.execute();
      holdingPanel.add(scrollPane);
    }
  }

  // -------------------------- OTHER METHODS --------------------------

  public String getPreferredSaveDir()
  {
    return preferences.get(SAVE_DIR, null);
  }

  public void showNormal()
  {
    setCursor(normalCursor);
    statusLabel.setText("");
    adjustColumnWidths();
    libraryField.setEnabled(true);
    libraryField.requestFocus();
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

  public void stopProgressPanel()
  {
    progressPanel.stop();
  }

  // --------------------------- main() method ---------------------------

  @SuppressWarnings({ "ResultOfObjectAllocationIgnored" })
  public static void main(String[] args)
  {
    new IvyBrowserMainFrame();
  }

  // --------------------- GETTER / SETTER METHODS ---------------------

  public Map<String, Map<String, Map<String, IvyPackage>>> getPackageMap()
  {
    return packageMap;
  }

  public void setPreferredSaveDir(String dir)
  {
    preferences.put(SAVE_DIR, dir);
  }

  public void setStatusLabel(String text)
  {
    statusLabel.setText(text);
  }
}
