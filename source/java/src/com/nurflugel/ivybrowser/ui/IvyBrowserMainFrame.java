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
import static java.awt.Cursor.getPredefinedCursor;
import java.awt.event.*;

import java.net.Authenticator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.*;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.showInputDialog;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/** Created by IntelliJ IDEA. User: dbulla Date: Apr 26, 2007 Time: 12:37:50 PM To change this template use File | Settings | File Templates. */
@SuppressWarnings({ "MethodParameterNamingConvention", "CallToPrintStackTrace", "MethodOnlyUsedFromInnerClass" })
public class IvyBrowserMainFrame extends JFrame
{
    private static final long     serialVersionUID    = 8982188831570838035L;
    private Cursor                busyCursor          = getPredefinedCursor(Cursor.WAIT_CURSOR);
    private Cursor                normalCursor        = getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    private JButton               specifyButton       = new JButton("Specify Repository");
    private JButton               reparseButton       = new JButton("Reparse Repository");
    private JButton               quitButton          = new JButton("Quit");
    private JLabel                findLabel           = new JLabel("Find library:");
    private JLabel                statusLabel         = new JLabel();
    private JCheckBox             parseOnOpenCheckbox = new JCheckBox("Parse Repository on Open", false);
    private JTable                resultsTable        = new JTable();
    private JTextField            libraryField        = new JTextField();
    private Preferences           preferences         = Preferences.userNodeForPackage(IvyBrowserMainFrame.class);
    private InfiniteProgressPanel progressPanel       = new InfiniteProgressPanel("Accessing the Ivy repository, please be patient");
    public static final String    IVY_REPOSITORY      = "IvyRepository";
    // private final EventList<IvyPackage> repositoryList=new BasicEventList<IvyPackage>();
    private List<IvyPackage>      repositoryList      = Collections.synchronizedList(new ArrayList<IvyPackage>());
    private static final String   PARSE_ON_OPEN       = "parseOnOpen";
    private JScrollPane           scrollPane;
    private JPanel                holdingPanel;
    private String                ivyRepositoryPath;

    // --------------------------- CONSTRUCTORS ---------------------------
    public IvyBrowserMainFrame()
    {
        initializeComponents();

        // IvyTrackerMainFrame.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", this);
        pack();
        setSize(800, 600);
        BuilderMainFrame.centerApp(this);

        // this was causing problems with GlazedLists throwing NPEs
        // IvyTrackerMainFrame.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", this);
        Authenticator.setDefault(new WebAuthenticator());
        libraryField.setEnabled(false);
        setVisible(true);

        boolean parseOnOpen = preferences.getBoolean(PARSE_ON_OPEN, false);

//        parseOnOpen = false;
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
                @Override public void keyReleased(KeyEvent e)
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
                    specifyRepository(preferences);
                    reparse();
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
                @Override public void mousePressed(MouseEvent e)
                {
                    showIvyLine(e);
                }
            });
        addWindowListener(new WindowAdapter()
            {
                @Override public void windowClosing(WindowEvent e)
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
    }

    public static String specifyRepository(Preferences appPreferences)
    {
        String ivyRepositoryPath = appPreferences.get(IVY_REPOSITORY, "");

        ivyRepositoryPath = (String) showInputDialog(null, "What is the Ivy repository URL?", "Enter Ivy repository URL", QUESTION_MESSAGE, null, null, ivyRepositoryPath);
        appPreferences.put(IVY_REPOSITORY, ivyRepositoryPath);

        return ivyRepositoryPath;
    }

    private void reparse()
    {
        setCursor(busyCursor);

        // resultsTable.setVisible(false);
        holdingPanel.remove(scrollPane);
        progressPanel.start();
        ivyRepositoryPath = preferences.get(IVY_REPOSITORY, "http://www.nurflugel.com/Home/repository/");

        if (ivyRepositoryPath.length() > 0)  // List<IvyPackage> list = new ArrayList<IvyPackage>();
        {
            repositoryList.clear();

            // repositoryList.addAll(list);
            BaseWebHandler handler = HandlerFactory.getHandler(this, ivyRepositoryPath, repositoryList);

            handler.execute();
            // handler.doInBackground();

            // resultsTable.setVisible(true);
            holdingPanel.add(scrollPane);
        }
    }

    public void filterTable()
    {
        EventList<IvyPackage>       eventList                  = new BasicEventList<IvyPackage>(repositoryList);
        SortedList<IvyPackage>      sortedPackages             = new SortedList<IvyPackage>(eventList);
        TextComponentMatcherEditor  textComponentMatcherEditor = new TextComponentMatcherEditor(libraryField, new IvyPackageFilterator());
        FilterList<IvyPackage>      filteredPackages           = new FilterList<IvyPackage>(sortedPackages, textComponentMatcherEditor);
        EventTableModel<IvyPackage> tableModel                 = new EventTableModel<IvyPackage>(filteredPackages, new IvyPackageTableFormat());

        resultsTable.setModel(tableModel);

        TableComparatorChooser<IvyPackage> tableSorter = new TableComparatorChooser<IvyPackage>(resultsTable, sortedPackages, true);
    }

    @SuppressWarnings({ "UseOfSystemOutOrSystemErr" })
    private void showIvyLine(MouseEvent e)
    {
        int row = resultsTable.getSelectedRow();

        if (row > -1)
        {
            EventTableModel tableModel = (EventTableModel) resultsTable.getModel();
            IvyPackage      ivyFile    = (IvyPackage) tableModel.getElementAt(row);
            IvyLineDialog   dialog     = new IvyLineDialog(ivyFile, ivyRepositoryPath);

            dialog.setVisible(true);
        }
        else
        {
            System.out.println("No row selected...");
        }
    }
    // -------------------------- OTHER METHODS --------------------------
    public void stopProgressPanel()
    {
        // repositoryList = new BasicEventList<IvyPackage>();
        // repositoryList.addAll(list);
        // filterTable();
        progressPanel.stop();
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

    // --------------------------- main() method ---------------------------
    @SuppressWarnings({ "ResultOfObjectAllocationIgnored" })
    public static void main(String[] args)
    {
        new IvyBrowserMainFrame();
    }

    // public synchronized void addIvyPackage(IvyPackage localPackage)
    // public  void addIvyPackage(IvyPackage localPackage)
    // {
    ////        synchronized (repositoryList)
    ////        {
    // try
    // {
    // repositoryList.getReadWriteLock().writeLock().lock();
    // repositoryList.add(localPackage);
    // }
    // finally
    // {
    // repositoryList.getReadWriteLock().writeLock().unlock();
    // }
    ////        }
    // }
}
