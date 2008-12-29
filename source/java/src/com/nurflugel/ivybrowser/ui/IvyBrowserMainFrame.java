package com.nurflugel.ivybrowser.ui;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import com.nurflugel.common.ui.Version;
import com.nurflugel.ivybrowser.InfiniteProgressPanel;
import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivybrowser.handlers.HtmlHandler;
import com.nurflugel.WebAuthenticator;

import javax.swing.*;
import static javax.swing.JOptionPane.showInputDialog;
import static javax.swing.JOptionPane.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.net.Authenticator;
import java.util.List;
import java.util.prefs.Preferences;


/** Created by IntelliJ IDEA. User: dbulla Date: Apr 26, 2007 Time: 12:37:50 PM To change this template use File | Settings | File Templates. */
@SuppressWarnings({ "MethodParameterNamingConvention", "CallToPrintStackTrace", "MethodOnlyUsedFromInnerClass" })
public class IvyBrowserMainFrame extends JFrame
{

    /** Use serialVersionUID for interoperability. */
    private static final long     serialVersionUID = 8982188831570838035L;
    private Cursor                busyCursor       = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
    private Cursor                normalCursor     = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    private JButton               specifyButton    = new JButton("Specify Repository");
    private JButton               reparseButton    = new JButton("Reparse Repository");
    private JButton               quitButton       = new JButton("Quit");
    private JLabel                findLabel        = new JLabel("Find library:");
    private JLabel                statusLabel      = new JLabel("Please wait while the repository is scanned...");
    private JTable                resultsTable     = new JTable();
    private JTextField            libraryField     = new JTextField();
    private EventList<IvyPackage> repositoryList;
    private Preferences preferences = Preferences.userNodeForPackage(IvyBrowserMainFrame.class);
    private InfiniteProgressPanel progressPanel = new InfiniteProgressPanel("Accessing the Ivy repository, please be patient");
    private static final String IVY_REPOSITORY = "IvyRepository";
    private JPanel mainPanel;

    // --------------------------- CONSTRUCTORS ---------------------------

    public IvyBrowserMainFrame()
    {
        initializeComponents();
        pack();
        setSize(800, 600);
        BuilderMainFrame.centerApp(this);
        //this was causing problems with GlazedLists throwing NPEs
        //com.nurflugel.ivytracker.IvyBrowserMainFrame.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel",this);
        Authenticator.setDefault(new WebAuthenticator());

        libraryField.setEnabled(false);
        setVisible(true);
         reparse();
    }

    private void initializeComponents()
    {

        mainPanel = new JPanel(new BorderLayout(0, 0));
        setGlassPane(progressPanel);
        setContentPane(mainPanel);

        libraryField.setPreferredSize(new Dimension(200, 25));
        setTitle("Ivy Repository Browser v. " + Version.VERSION);

        JPanel    textPanel    = new JPanel();
        JPanel    buttonPanel  = new JPanel();
        JPanel    holdingPanel = new JPanel();
        BoxLayout layout       = new BoxLayout(holdingPanel, BoxLayout.Y_AXIS);

        holdingPanel.setLayout(layout);
        textPanel.add(findLabel);
        textPanel.add(libraryField);
        buttonPanel.add(specifyButton);
        buttonPanel.add(reparseButton);
        buttonPanel.add(quitButton);
        holdingPanel.add(textPanel);
        holdingPanel.add(buttonPanel);

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        holdingPanel.add(scrollPane);
        mainPanel.add(holdingPanel, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        addListeners();
        setCursor(busyCursor);

    }

    private void addListeners()
    {
        libraryField.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent e)
                {
                    filterTable();
                }
            });
        quitButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    System.exit(0);
                }
            });
        reparseButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    reparse();
                }
            });
        specifyButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    specifyRepository();
                    reparse();
                }
            });
        libraryField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    filterTable();
                }
            });
        resultsTable.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e)
                {
                    showIvyLine(e);
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
    }

    private void specifyRepository()
    {
        //todo get any stored preferences, display dialog, adn store results in preferences
        String ivyRepositoryPath = preferences.get(IVY_REPOSITORY, "");
        ivyRepositoryPath  = (String) showInputDialog(this, "What is the Ivy repository URL?", "Enter Ivy repository URL", QUESTION_MESSAGE, null,null,ivyRepositoryPath);
        preferences.put(IVY_REPOSITORY,ivyRepositoryPath);
    }

    private void reparse()
    {
        progressPanel.start();
        String ivyRepositoryPath = preferences.get(IVY_REPOSITORY, "");
        if(ivyRepositoryPath.length()>0){
        HtmlHandler handler = new HtmlHandler(this,ivyRepositoryPath);
        handler.execute();
//        handler.doInBackground();}
        }
    }

    private void filterTable()
    {
        SortedList<IvyPackage>      sortedPackages   = new SortedList<IvyPackage>(repositoryList, new IvyPackageComparator());
        FilterList<IvyPackage>      filteredPackages = new FilterList<IvyPackage>(sortedPackages, new TextComponentMatcherEditor(libraryField, new IvyPackageFilterator()));
        EventTableModel<IvyPackage> tableModel       = new EventTableModel<IvyPackage>(filteredPackages, new IvyPackageTableFormat());

        resultsTable.setModel(tableModel);

        TableComparatorChooser<IvyPackage> tableSorter = new TableComparatorChooser<IvyPackage>(resultsTable, sortedPackages, true);
    }

    private void showIvyLine(MouseEvent e)
    {

        int             row        = resultsTable.getSelectedRow();
        EventTableModel tableModel = (EventTableModel) resultsTable.getModel();

        IvyPackage ivyFile = (IvyPackage) tableModel.getElementAt(row);


        IvyLineDialog dialog = new IvyLineDialog(ivyFile);
        dialog.setVisible(true);
    }

    // -------------------------- OTHER METHODS --------------------------

    public void populateTable(List<IvyPackage> list)
    {

        repositoryList = new BasicEventList<IvyPackage>();
        repositoryList.addAll(list);
        filterTable();
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

        for (int col = 0; col < resultsTable.getColumnCount(); col++) {
            int maxWidth = 0;

            for (int row = 0; row < resultsTable.getRowCount(); row++) {
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
}
