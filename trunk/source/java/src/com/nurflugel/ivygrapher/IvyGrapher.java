package com.nurflugel.ivygrapher;

import org.jdom.JDOMException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.FILES_ONLY;
import javax.help.HelpSet;
import javax.help.HelpBroker;
import javax.help.CSH;
import javax.help.HelpSetException;
import java.io.File;
import java.io.IOException;
import java.io.FilenameFilter;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import static java.awt.Cursor.getPredefinedCursor;
import static java.awt.Cursor.DEFAULT_CURSOR;
import static java.awt.Cursor.WAIT_CURSOR;
import java.net.URL;

import static com.nurflugel.common.ui.Util.*;
import com.nurflugel.common.ui.Version;
import static com.nurflugel.common.ui.Version.*;
import com.nurflugel.Os;
import static com.nurflugel.ivygrapher.OutputFormat.*;
import static com.nurflugel.ivygrapher.NodeOrder.*;

/** Engine for the Ivy Grapher. */
@SuppressWarnings({"CallToPrintStackTrace", "UseOfSystemOutOrSystemErr"})
public class IvyGrapher extends JFrame
{
    private File[] filesToGraph;
    private String lastVisitedDir;
    private Preferences preferences = Preferences.userNodeForPackage(IvyGrapher.class);
    private static final String DIR = "dir";
    private Os os;
    private OutputFormat outputFormat;
    private NodeOrder nodeOrder;
    private JPanel mainPanel;
    private JButton helpButton;
    private JButton selectFilesButton;
    private JButton findDotButton;
    private JButton quitButton;
    private JRadioButton tbButton;
    private JRadioButton btButton;
    private JRadioButton rlButton;
    private JRadioButton lrButton;
    private JCheckBox deleteDotButton;
    private JRadioButton pngButton;
    private JRadioButton pdfButtton;
    private JRadioButton svgButton;
    private static final String DOT_EXECUTABLE = "dotExecutable";
    private String dotExecutablePath;
    private Cursor normalCursor = getPredefinedCursor(DEFAULT_CURSOR);
    private Cursor busyCursor = getPredefinedCursor(WAIT_CURSOR);
    public static final String HELP_HS = "ivyGrapherHelp.hs";

    public IvyGrapher(String[] args)
    {
        if (args.length > 0)
        {
            List<File> files = new ArrayList<File>();

            for (String arg : args)
            {
                files.add(new File(arg));
            }

            filesToGraph = files.toArray(new File[files.size()]);
        }

        os = Os.findOs(System.getProperty("os.name"));
        preferences = Preferences.userNodeForPackage(IvyGrapher.class);
        initializeUi();
        setContentPane(mainPanel);
        setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", this);
        setTitle("Ivy Grapher v. " + VERSION);
        pack();
        center(this);
        setVisible(true);
    }


    private void doQuitAction()
    {
        preferences.put("outputFormat", outputFormat.getDisplayLabel());
        preferences.put("nodeOrder", nodeOrder.toString());
        preferences.putBoolean("deleteDotFiles", deleteDotButton.isSelected());
        preferences.put("dotLocation", dotExecutablePath);
        System.exit(0);
    }

    private void getOutputFormat()
    {
        String text = preferences.get("outputFormat", PDF.getDisplayLabel());

        if (os != Os.OS_X)
        {
            pdfButtton.setEnabled(false);
            if (text.equals("PDF"))
            {
                text = PNG.getDisplayLabel();
            }
        }

        outputFormat = OutputFormat.valueOf(text);
        pdfButtton.setSelected(outputFormat == PDF);
        pngButton.setSelected(outputFormat == PNG);
        svgButton.setSelected(outputFormat == SVG);


    }

    private void initializeUi()
    {
        getOutputFormat();
        getNodeOrdering();
        getMiscInfo();
        getDotLocation();
        addActionListeners();
        validateDotExistance();
    }

    private void getDotLocation()
    {
        dotExecutablePath = preferences.get("dotLocation", os.getDefaultDotPath());
    }

    private void getMiscInfo()
    {
        boolean deleteDotFiles = preferences.getBoolean("deleteDotFiles", true);

        deleteDotButton.setSelected(deleteDotFiles);
    }

    private void getNodeOrdering()
    {
        String text = preferences.get("nodeOrder", TOP_TO_BOTTOM.toString());

        nodeOrder = NodeOrder.find(text);
        tbButton.setSelected(nodeOrder == TOP_TO_BOTTOM);
        btButton.setSelected(nodeOrder == BOTTOM_TO_TOP);
        rlButton.setSelected(nodeOrder == RIGHT_TO_LEFT);
        lrButton.setSelected(nodeOrder == LEFT_TO_RIGHT);
    }

    private void addActionListeners()
    {
        quitButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                doQuitAction();
            }
        });
        findDotButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                findDotLocation();
            }
        });

        selectFilesButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                createGraph();
            }
        });
        pngButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                outputFormat = PNG;
            }
        });
        pdfButtton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                outputFormat = PDF;
            }
        });
        svgButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                outputFormat = SVG;
            }
        });
        tbButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                nodeOrder = TOP_TO_BOTTOM;
            }
        });
        btButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                nodeOrder = BOTTOM_TO_TOP;
            }
        });
        rlButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                nodeOrder = RIGHT_TO_LEFT;
            }
        });
        lrButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                nodeOrder = LEFT_TO_RIGHT;
            }
        });
        addHelpListener();
    }

    /** Add the help listener - link to the help files. */
    private void addHelpListener()
    {
        ClassLoader classLoader = IvyGrapher.class.getClassLoader();

        try
        {
            URL hsURL = HelpSet.findHelpSet(classLoader, HELP_HS);

            HelpSet helpSet = new HelpSet(null, hsURL);
            HelpBroker helpBroker = helpSet.createHelpBroker();
            CSH.DisplayHelpFromSource displayHelpFromSource = new CSH.DisplayHelpFromSource(helpBroker);

            helpButton.addActionListener(displayHelpFromSource);
        }
        catch (HelpSetException ee)
        {  // Say what the exception really is
            System.out.println("Exception! " + ee.getMessage());
//      LOGGER.error("HelpSet " + ee.getMessage());
//      LOGGER.error("HelpSet " + HELP_HS + " not found");

        }

    }

    private void findDotLocation()
    {
        dotExecutablePath = preferences.get(DOT_EXECUTABLE, "");

        if ((dotExecutablePath == null) || (dotExecutablePath.length() == 0))
        {
            dotExecutablePath = os.getDefaultDotPath();
        }

        // Create a file chooser
        NoDotDialog dialog = new NoDotDialog(dotExecutablePath);
        File dotExecutableFile = dialog.getFile();

        if (dotExecutableFile != null)
        {
            dotExecutablePath = dotExecutableFile.getAbsolutePath();
            preferences.put(DOT_EXECUTABLE, dotExecutablePath);
        }
        else
        {
            showMessageDialog(this, "Sorry, this program can't run without the GraphViz installation.\n" + "  Please install that and try again");
        }
        validateDotExistance();
    }

    /** Validate the existence of the DOT executatble.  No executable, no executing Graphviz... */
    private void validateDotExistance()
    {
        File dot = new File(dotExecutablePath);
        boolean exists = dot.exists();
        selectFilesButton.setEnabled(exists);
        selectFilesButton.setToolTipText(exists ? "Select the XML files in the .ivy  directory to graph" : "You must first find Dot on your system");
    }

    public static void main(String[] args)
    {
        IvyGrapher grapher = new IvyGrapher(args);

        // grapher.createGraph();
    }

    private void createGraph()
    {
        try
        {
            JFileChooser chooser = new JFileChooser();

            chooser.setDialogTitle("Select the Ivy file");

            String dirName = preferences.get(DIR, null);

            if (dirName != null)
            {
                File lastDir = new File(dirName);

                chooser.setCurrentDirectory(lastDir);
                chooser.setFileHidingEnabled(false);
            }

            chooser.setFileSelectionMode(FILES_ONLY);
            chooser.setMultiSelectionEnabled(true);
            chooser.setFileFilter(new FileNameExtensionFilter("XML files", "xml"));

            int returnVal = chooser.showDialog(null, "Use these files");

            if (returnVal == APPROVE_OPTION)
            {
                filesToGraph = chooser.getSelectedFiles();

                if (filesToGraph.length > 0)
                {
                    dirName = filesToGraph[0].getParent();
                    preferences.put(DIR, dirName);
                }
            }
            System.out.println("Setting busy cursor");

            setCursor(busyCursor);

            XmlHandler xmlHandler = new XmlHandler();

            if (filesToGraph != null)
            {
                for (File fileToGraph : filesToGraph)
                {
                    xmlHandler.processXmlFile(fileToGraph, preferences, nodeOrder, os, outputFormat, dotExecutablePath, deleteDotButton.isSelected());
                }
            }

            System.out.println("Setting normal cursor");
            setCursor(normalCursor);
        }
        catch (JDOMException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer >>> IMPORTANT!! <<< DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        helpButton = new JButton();
        helpButton.setEnabled(true);
        helpButton.setText("Help");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(helpButton, gbc);
        selectFilesButton = new JButton();
        selectFilesButton.setText("Select Ivy report(s) to visualize...");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(selectFilesButton, gbc);
        findDotButton = new JButton();
        findDotButton.setText("Find Dot...");
        findDotButton.setToolTipText("Locate Dot on your system");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(findDotButton, gbc);
        quitButton = new JButton();
        quitButton.setText("Quit");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(quitButton, gbc);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(panel1, gbc);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel2, gbc);
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Output format"));
        pngButton = new JRadioButton();
        pngButton.setText("PNG");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(pngButton, gbc);
        pdfButtton = new JRadioButton();
        pdfButtton.setSelected(false);
        pdfButtton.setText("PDF (OS X Only)");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(pdfButtton, gbc);
        svgButton = new JRadioButton();
        svgButton.setText("SVG");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(svgButton, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel3, gbc);
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Misc Options"));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel3.add(panel4, gbc);
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Node Ordering"));
        tbButton = new JRadioButton();
        tbButton.setSelected(false);
        tbButton.setText("Top-to-bottom");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel4.add(tbButton, gbc);
        btButton = new JRadioButton();
        btButton.setText("Bottom-to-top");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel4.add(btButton, gbc);
        rlButton = new JRadioButton();
        rlButton.setText("Right-to-left");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel4.add(rlButton, gbc);
        lrButton = new JRadioButton();
        lrButton.setText("Left-to-right");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel4.add(lrButton, gbc);
        deleteDotButton = new JCheckBox();
        deleteDotButton.setSelected(false);
        deleteDotButton.setText("Delete .dot files on exit");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(deleteDotButton, gbc);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(tbButton);
        buttonGroup.add(btButton);
        buttonGroup.add(rlButton);
        buttonGroup.add(lrButton);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(pngButton);
        buttonGroup.add(pdfButtton);
        buttonGroup.add(svgButton);
    }

    /** @noinspection ALL */
    public JComponent $$$getRootComponent$$$()
    {
        return mainPanel;
    }
}
