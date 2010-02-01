package com.nurflugel.ivybrowser.ui;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import com.nurflugel.Os;
import com.nurflugel.WebAuthenticator;
import com.nurflugel.common.ui.FindMultiplePreferencesItemsDialog;
import com.nurflugel.common.ui.UiMainFrame;
import com.nurflugel.common.ui.Util;
import com.nurflugel.common.ui.Version;
import com.nurflugel.ivybrowser.InfiniteProgressPanel;
import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivybrowser.handlers.BaseWebIvyRepositoryBrowserHandler;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.Authenticator;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import static com.nurflugel.common.ui.Util.addHelpListener;
import static com.nurflugel.common.ui.Util.centerApp;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.Cursor.getPredefinedCursor;
import static javax.swing.BoxLayout.Y_AXIS;
import static org.apache.commons.lang.StringUtils.isEmpty;

/** Main UI frame for the Ivy Browser. */
@SuppressWarnings({ "MethodParameterNamingConvention", "CallToPrintStackTrace", "MethodOnlyUsedFromInnerClass" })
public class IvyBrowserMainFrame extends JFrame implements UiMainFrame
{
  public static final String                                IVY_REPOSITORY      = "IvyRepository";
  private static final long                                 serialVersionUID    = 8982188831570838035L;
  private static final String                               PARSE_ON_OPEN       = "parseOnOpen";
  private static final String                               SAVE_DIR            = "saveDir";
  private Cursor                                            busyCursor          = getPredefinedCursor(Cursor.WAIT_CURSOR);
  private Cursor                                            normalCursor        = getPredefinedCursor(Cursor.DEFAULT_CURSOR);
  private JButton                                           specifyButton       = new JButton("Specify Repository");
  private JButton                                           reparseButton       = new JButton("Re-parse Repository");
  private JButton                                           quitButton          = new JButton("Quit");
  private JButton                                           helpButton          = new JButton("Help");
  private JLabel                                            findLabel           = new JLabel("Find library:");
  private JLabel                                            statusLabel         = new JLabel();
  private JCheckBox                                         parseOnOpenCheckbox = new JCheckBox("Parse Repository on Open", false);
  private JTable                                            resultsTable        = new JTable();
  private JTextField                                        libraryField        = new JTextField();
  private Preferences                                       preferences         = Preferences.userNodeForPackage(IvyBrowserMainFrame.class);
  private EventList<IvyPackage>                             repositoryList      = new BasicEventList<IvyPackage>();
  private JScrollPane                                       scrollPane;
  private JPanel                                            holdingPanel;
  private String                                            ivyRepositoryPath;
  private BaseWebIvyRepositoryBrowserHandler                parsingHandler;
  private Map<String, Map<String, Map<String, IvyPackage>>> packageMap          = Collections.synchronizedMap(new HashMap<String, Map<String, Map<String, IvyPackage>>>());
  private InfiniteProgressPanel                             progressPanel       = new InfiniteProgressPanel("Accessing the Ivy repository, please be patient - click to cancel",
                                                                                                            this);

  // --------------------------- CONSTRUCTORS ---------------------------
  public IvyBrowserMainFrame()
  {
    initializeComponents();
    pack();
    setSize(800, 600);
    centerApp(this);

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
    setupTable();
    Util.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", this);
  }

  private void addListeners()
  {
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
          specifyRepository(preferences);
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

    addHelpListener("ivyBrowserHelp.hs", helpButton, this);
  }

  public void specifyRepository(Preferences appPreferences)
  {
    FindMultiplePreferencesItemsDialog dialog = new FindMultiplePreferencesItemsDialog(appPreferences, "Select Ivy Repository", IVY_REPOSITORY);

    dialog.setVisible(true);

    if (dialog.isOk())
    {
      String ivyRepositoryPath = dialog.getRepositoryLocation();

      if (!isEmpty(ivyRepositoryPath) && !ivyRepositoryPath.equals(this.ivyRepositoryPath))
      {
        this.ivyRepositoryPath = ivyRepositoryPath;
        reparse();
      }
    }
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

  private void setupTable()
  {
    SortedList<IvyPackage>      sortedPackages             = new SortedList<IvyPackage>(repositoryList);
    TextComponentMatcherEditor  textComponentMatcherEditor = new TextComponentMatcherEditor(libraryField, new IvyPackageFilterator());
    FilterList<IvyPackage>      filteredPackages           = new FilterList<IvyPackage>(sortedPackages, textComponentMatcherEditor);
    EventTableModel<IvyPackage> tableModel                 = new EventTableModel<IvyPackage>(filteredPackages, new IvyPackageTableFormat());

    resultsTable.setModel(tableModel);
    resultsTable.setDefaultRenderer(Object.class, new CheckboxCellRenderer(false));

    TableComparatorChooser<IvyPackage> tableSorter = new TableComparatorChooser<IvyPackage>(resultsTable, sortedPackages, true);
  }

  private void reparse()
  {
    setBusyCursor();
    holdingPanel.remove(scrollPane);
    progressPanel.start();
    ivyRepositoryPath = preferences.get(IVY_REPOSITORY + 0, "");

    if (isEmpty(ivyRepositoryPath))
    {
      specifyRepository(preferences);
    }

    if (!isEmpty(ivyRepositoryPath))
    {
      repositoryList.clear();
      packageMap.clear();

      parsingHandler = HandlerFactory.getIvyRepositoryHandler(this, ivyRepositoryPath, repositoryList, packageMap);

      parsingHandler.execute();
      holdingPanel.add(scrollPane);
    }
  }

  // ------------------------ INTERFACE METHODS ------------------------

  // --------------------- Interface UiMainFrame ---------------------

  // tod implement these
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
    return false;  // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void setBusyCursor()
  {
    setCursor(busyCursor);
  }

  @Override
  public void setNormalCursor()
  {
    setCursor(normalCursor);
    statusLabel.setText("");
    adjustColumnWidths();
    libraryField.setEnabled(true);
    libraryField.requestFocus();
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
    parsingHandler.halt();
  }

  @Override
  public void setStatusLabel(String text)
  {
    statusLabel.setText(text);
    progressPanel.setText(text);
  }

  @Override
  public void stopProgressPanel()
  {
    progressPanel.stop();
  }

  // -------------------------- OTHER METHODS --------------------------

  private void adjustColumnWidths()
  {
    TableColumnModel columnModel = resultsTable.getColumnModel();

    for (int col = 0; col < resultsTable.getColumnCount(); col++)
    {
      int maxWidth = 50;

      for (int row = 0; row < resultsTable.getRowCount(); row++)
      {
        TableCellRenderer cellRenderer      = resultsTable.getCellRenderer(row, col);
        Object            value             = resultsTable.getValueAt(row, col);
        Component         rendererComponent = cellRenderer.getTableCellRendererComponent(resultsTable, value, false, false, row, col);

        maxWidth = Math.max(rendererComponent.getPreferredSize().width, maxWidth);
      }

      TableColumn column = columnModel.getColumn(col);

      column.setPreferredWidth(maxWidth);
      column.setMinWidth(maxWidth);
    }
  }

  public Map<String, Map<String, Map<String, IvyPackage>>> getPackageMap()
  {
    return packageMap;
  }

  public String getPreferredSaveDir()
  {
    return preferences.get(SAVE_DIR, null);
  }

  public void setPreferredSaveDir(String dir)
  {
    preferences.put(SAVE_DIR, dir);
  }

  // --------------------------- main() method ---------------------------

  @SuppressWarnings({ "ResultOfObjectAllocationIgnored" })
  public static void main(String[] args)
  {
    new IvyBrowserMainFrame();
  }
}
