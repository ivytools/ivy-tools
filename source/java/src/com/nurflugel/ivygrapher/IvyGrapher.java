package com.nurflugel.ivygrapher;

import org.jdom.JDOMException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.FILES_ONLY;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import static java.awt.Cursor.getPredefinedCursor;
import static com.nurflugel.common.ui.Util.*;
import static com.nurflugel.common.ui.Version.*;
import com.nurflugel.Os;
import static com.nurflugel.ivygrapher.OutputFormat.*;
import static com.nurflugel.ivygrapher.NodeOrder.*;

/** Engine for the Ivy Grapher. */
@SuppressWarnings({ "CallToPrintStackTrace", "UseOfSystemOutOrSystemErr" })
public class IvyGrapher extends JFrame
{
  private static final String DIR                      = "dir";
  private static final String DOT_EXECUTABLE           = "dotExecutable";
  private File[]              filesToGraph;
  private String              lastVisitedDir;
  private Preferences         preferences              = Preferences.userNodeForPackage(IvyGrapher.class);
  private Os                  os;
  private OutputFormat        outputFormat;
  private NodeOrder           nodeOrder;
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
  private JCheckBox           concentrateEdgesCheckBox;
  private String              dotExecutablePath;
  private Cursor              normalCursor             = getPredefinedCursor(DEFAULT_CURSOR);
  private Cursor              busyCursor               = getPredefinedCursor(WAIT_CURSOR);

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

    os          = Os.findOs(System.getProperty("os.name"));
    preferences = Preferences.userNodeForPackage(IvyGrapher.class);
    initializeUi();
    setContentPane(mainPanel);
    setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", this);
    setTitle("Ivy Grapher v. " + VERSION);
    pack();
    center(this);
    setVisible(true);
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

  private void getNodeOrdering()
  {
    String text = preferences.get("nodeOrder", TOP_TO_BOTTOM.toString());

    nodeOrder = NodeOrder.find(text);
    tbButton.setSelected(nodeOrder == TOP_TO_BOTTOM);
    btButton.setSelected(nodeOrder == BOTTOM_TO_TOP);
    rlButton.setSelected(nodeOrder == RIGHT_TO_LEFT);
    lrButton.setSelected(nodeOrder == LEFT_TO_RIGHT);
  }

  private void getMiscInfo()
  {
    boolean deleteDotFiles   = preferences.getBoolean("deleteDotFiles", true);
    boolean concentrateEdges = preferences.getBoolean("concentrateEdges", true);

    deleteDotButton.setSelected(deleteDotFiles);
    concentrateEdgesCheckBox.setSelected(concentrateEdges);
  }

  private void getDotLocation()
  {
    dotExecutablePath = preferences.get("dotLocation", os.getDefaultDotPath());
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
    addHelpListener("ivyGrapherHelp.hs", helpButton, this);
  }

  private void doQuitAction()
  {
    preferences.put("outputFormat", outputFormat.getDisplayLabel());
    preferences.put("nodeOrder", nodeOrder.toString());
    preferences.putBoolean("deleteDotFiles", deleteDotButton.isSelected());
    preferences.putBoolean("concentrateEdges", concentrateEdgesCheckBox.isSelected());
    preferences.put("dotLocation", dotExecutablePath);
    System.exit(0);
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
      showMessageDialog(this, "Sorry, this program can't run without the GraphViz installation.\n" + "  Please install that and try again");
    }

    validateDotExistance();
  }

  private void createGraph()
  {
    try
    {
      // todo make a file chooser that's taller adn wider!
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
      // chooser.setBounds(100,100,1000,1000); nice try

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
          xmlHandler.processXmlFile(fileToGraph, preferences, nodeOrder, os, outputFormat, dotExecutablePath, deleteDotButton.isSelected(),
                                    concentrateEdgesCheckBox.isSelected());
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

  /** Validate the existence of the DOT executatble. No executable, no executing Graphviz... */
  private void validateDotExistance()
  {
    File    dot    = new File(dotExecutablePath);
    boolean exists = dot.exists();

    selectFilesButton.setEnabled(exists);
    selectFilesButton.setToolTipText(exists ? "Select the XML files in the .ivy  directory to graph"
                                            : "You must first find Dot on your system");
  }

  // --------------------------- main() method ---------------------------

  public static void main(String[] args)
  {
    IvyGrapher grapher = new IvyGrapher(args);

    // grapher.createGraph();
  }
}
