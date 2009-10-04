package com.nurflugel.ivygrapher;

import org.jdom.JDOMException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.FILES_ONLY;
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
import static com.nurflugel.common.ui.Util.*;
import com.nurflugel.Os;
import static com.nurflugel.ivygrapher.OutputFormat.*;
import static com.nurflugel.ivygrapher.NodeOrder.*;

/** Engine for the Ivy Grapher. */
@SuppressWarnings({ "CallToPrintStackTrace" })
public class IvyGrapher
{
  private File[]              filesToGraph;
  private String              lastVisitedDir;
  private Preferences         preferences       = Preferences.userNodeForPackage(IvyGrapher.class);
  private static final String DIR               = "dir";
  private Os                  os;
  private OutputFormat        outputFormat;
  private NodeOrder           nodeOrder;
  private JFrame              frame;
  private JPanel              mainPanel;
  private JButton             helpButton;
  private JButton             selectFilesButton;
  private JButton             findDotButton;
  private JButton             quitButton;
  private JRadioButton        tbButton;
  private JRadioButton        btButton;
  private JRadioButton        rlButton;
  private JRadioButton        lrButton;
  private JCheckBox           deleteDotButton;
  private JRadioButton        pngButton;
  private JRadioButton        pdfButtton;
  private JRadioButton        svgButton;
  private static final String DOT_EXECUTABLE    = "dotExecutable";
  private String              dotExecutablePath;
  private Cursor              normalCursor      = getPredefinedCursor(DEFAULT_CURSOR);
  private Cursor              busyCursor        = getPredefinedCursor(WAIT_CURSOR);

  public IvyGrapher(String[] args)
  {
    String fileName = null;

    if (args.length > 0)
    {
      List<File> files = new ArrayList<File>();

      for (String arg : args)
      {
        files.add(new File(arg));
      }

      filesToGraph = files.toArray(new File[files.size()]);
    }

    os          = Os.findOs(System.getProperty("os.name"));
    preferences = Preferences.userNodeForPackage(IvyGrapher.class);
    frame       = new JFrame();
    os.setLookAndFeel(frame);

    // setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", frame);
    initializeUi();
    frame.setContentPane(mainPanel);
    frame.pack();
    center(frame);
    frame.setVisible(true);
    // setDefaultDotLocation();
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
      text = PNG.toString();
    }

    outputFormat = OutputFormat.valueOf(text);
  }

  private void initializeUi()
  {
    getOutputFormat();
    getNodeOrdering();
    getMiscInfo();
    getDotLocation();
    addActionListeners();
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
  }

  private void findDotLocation()
  {
    dotExecutablePath = preferences.get(DOT_EXECUTABLE, "");

    if ((dotExecutablePath == null) || (dotExecutablePath.length() == 0))
    {
      dotExecutablePath = os.getDefaultDotPath();
    }

    // Create a file chooser
    NoDotDialog dialog            = new NoDotDialog(dotExecutablePath);
    File        dotExecutableFile = dialog.getFile();

    if (dotExecutableFile != null)
    {
      dotExecutablePath = dotExecutableFile.getAbsolutePath();
      preferences.put(DOT_EXECUTABLE, dotExecutablePath);
    }
    else
    {
      showMessageDialog(frame, "Sorry, this program can't run without the GraphViz installation.\n" + "  Please install that and try again");
      doQuitAction();
    }
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

      frame.setCursor(busyCursor);

      XmlHandler xmlHandler = new XmlHandler();

      if (filesToGraph != null)
      {
        for (File fileToGraph : filesToGraph)
        {
          xmlHandler.processXmlFile(fileToGraph, preferences, nodeOrder, os, outputFormat, dotExecutablePath, deleteDotButton.isSelected());
        }
      }

      frame.setCursor(normalCursor);
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
   * @noinspection  ALL
   */
  private void $$$setupUI$$$()
  {
    mainPanel = new JPanel();
    mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
    helpButton = new JButton();
    helpButton.setText("Help");
    mainPanel.add(helpButton,
                  new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
                                                                   com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL,
                                                                   com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                   | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                   com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                                   false));
    selectFilesButton = new JButton();
    selectFilesButton.setText("Select Ivy report(s) to visualize...");
    mainPanel.add(selectFilesButton,
                  new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
                                                                   com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL,
                                                                   com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                   | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                   com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                                   false));
    findDotButton = new JButton();
    findDotButton.setText("Find Dot...");
    mainPanel.add(findDotButton,
                  new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
                                                                   com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL,
                                                                   com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                   | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                   com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                                   false));
    quitButton = new JButton();
    quitButton.setText("Quit");
    mainPanel.add(quitButton,
                  new com.intellij.uiDesigner.core.GridConstraints(1, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
                                                                   com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL,
                                                                   com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                   | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                   com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                                   false));

    final JPanel panel1 = new JPanel();

    panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
    mainPanel.add(panel1,
                  new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
                                                                   com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH,
                                                                   com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                   | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                   com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                   | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null,
                                                                   null, 0, false));

    final JPanel panel2 = new JPanel();

    panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
    panel1.add(panel2,
               new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
                                                                com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
                                                                0, false));
    panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Output format"));
    pngButton = new JRadioButton();
    pngButton.setText("PNG");
    panel2.add(pngButton,
               new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST,
                                                                com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                                false));

    final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();

    panel2.add(spacer1,
               new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
                                                                com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null,
                                                                0, false));
    pdfButtton = new JRadioButton();
    pdfButtton.setSelected(true);
    pdfButtton.setText("PDF (OS X Only)");
    panel2.add(pdfButtton,
               new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST,
                                                                com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                                false));
    svgButton = new JRadioButton();
    svgButton.setText("SVG");
    panel2.add(svgButton,
               new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST,
                                                                com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                                false));

    final JPanel panel3 = new JPanel();

    panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
    panel1.add(panel3,
               new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
                                                                com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
                                                                0, false));
    panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Misc Options"));

    final JPanel panel4 = new JPanel();

    panel4.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1));
    panel3.add(panel4,
               new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
                                                                com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
                                                                0, false));
    panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Node Ordering"));
    tbButton = new JRadioButton();
    tbButton.setSelected(true);
    tbButton.setText("Top-to-bottom");
    panel4.add(tbButton,
               new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST,
                                                                com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                                false));

    final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();

    panel4.add(spacer2,
               new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
                                                                com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null,
                                                                0, false));
    btButton = new JRadioButton();
    btButton.setText("Bottom-to-top");
    panel4.add(btButton,
               new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST,
                                                                com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                                false));
    rlButton = new JRadioButton();
    rlButton.setText("Right-to-left");
    panel4.add(rlButton,
               new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST,
                                                                com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                                false));
    lrButton = new JRadioButton();
    lrButton.setText("Left-to-right");
    panel4.add(lrButton,
               new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST,
                                                                com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                                false));
    deleteDotButton = new JCheckBox();
    deleteDotButton.setSelected(true);
    deleteDotButton.setText("Delete .dot files on exit");
    panel3.add(deleteDotButton,
               new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST,
                                                                com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                                false));

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

  /** @noinspection  ALL */
  public JComponent $$$getRootComponent$$$()
  {
    return mainPanel;
  }
}
