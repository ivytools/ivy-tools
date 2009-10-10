package com.nurflugel.ivybrowser.ui;

import com.nurflugel.common.ui.Version;
import com.nurflugel.common.ui.Util;
import com.nurflugel.ivybrowser.domain.IvyRepositoryItem;
import com.nurflugel.ivybrowser.handlers.FileHandler;
import com.nurflugel.ivytracker.IvyTrackerMainFrame;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import java.net.URL;
import javax.swing.*;
import static javax.swing.JOptionPane.showConfirmDialog;
import javax.help.HelpSet;
import javax.help.HelpBroker;
import javax.help.CSH;
import javax.help.HelpSetException;

/** Created by IntelliJ IDEA. User: dbulla Date: Jan 18, 2008 Time: 9:36:34 PM To change this template use File | Settings | File Templates. */
public class BuilderMainFrame extends JFrame
{
  /** Use serialVersionUID for interoperability. */
  private static final long       serialVersionUID      = 3060125117710119253L;
  private static final String     REPOSITORY_LOCATION   = "repositoryLocation";
  private static final String     REPOSITORY            = "repository";
  private JButton                 setIvyReposButton;
  private JButton                 addNewComponentButton;
  private JLabel                  ivyReposLabel;
  private JPanel                  contentsPanel;
  private JButton                 quitButton;
  private JLabel                  statusLabel;
  private JButton                 helpButton;
  private File                    repositoryDir;
  private List<IvyRepositoryItem> ivyPackages           = new ArrayList<IvyRepositoryItem>();
  private Preferences             preferences;

  // --------------------------- CONSTRUCTORS ---------------------------
  public BuilderMainFrame()
  {
    setTitle("IvyBuild v. " + Version.VERSION);
    setContentPane(contentsPanel);
    addListeners();
    Util.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", this);
    centerApp(this);
    preferences = Preferences.userNodeForPackage(BuilderMainFrame.class);
    loadPreferences();
    pack();
    centerApp(this);
    setVisible(true);
    findFiles();
  }

  private void addListeners()
  {
    setIvyReposButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          findRepositoryDir();
        }
      });
    addNewComponentButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          addNewComponent();
        }
      });
    quitButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          doQuitAction();
        }
      });
    addWindowListener(new WindowAdapter()
      {
        @Override
        public void windowClosing(WindowEvent e)
        {
          super.windowClosing(e);
          doQuitAction();
        }
      });

    ClassLoader classLoader = BuilderMainFrame.class.getClassLoader();

    try
    {
      URL                       hsURL                 = HelpSet.findHelpSet(classLoader, "ivyBuilderHelp.hs");

      HelpSet                   helpSet               = new HelpSet(null, hsURL);
      HelpBroker                helpBroker            = helpSet.createHelpBroker();
      CSH.DisplayHelpFromSource displayHelpFromSource = new CSH.DisplayHelpFromSource(helpBroker);

      helpButton.addActionListener(displayHelpFromSource);
    }
    catch (HelpSetException ee)
    {  // Say what the exception really is
      System.out.println("Exception! " + ee.getMessage());
      // LOGGER.error("HelpSet " + ee.getMessage());
      // LOGGER.error("HelpSet " + HELP_HS + " not found");
    }
  }

  private void findRepositoryDir()
  {
    JFileChooser chooser = new JFileChooser();

    chooser.setDialogTitle("Select the repository dir");
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    int returnVal = chooser.showDialog(this, "Use this directory");

    if (returnVal == JFileChooser.APPROVE_OPTION)
    {
      File   file    = chooser.getSelectedFile();
      String dirName = file.getName();

      if (dirName.equals(REPOSITORY))
      {
        repositoryDir = file;
        ivyReposLabel.setText(file.getAbsolutePath());
      }
      else
      {
        showConfirmDialog(this, "The dir must be named \"" + REPOSITORY + "\"");
      }
    }

    validateIvyRepositoryLocation(ivyReposLabel.getText());
    pack();
    centerApp(this);
    findFiles();
  }

  private void validateIvyRepositoryLocation(String dirName)
  {
    addNewComponentButton.setEnabled(false);

    if (dirName != null)
    {
      File dir = new File(dirName);

      if (dir.exists() && dir.isDirectory())
      {
        repositoryDir = dir;
        ivyReposLabel.setText(repositoryDir.getAbsolutePath());
        addNewComponentButton.setEnabled(true);
      }
    }
  }

  private void addNewComponent()
  {
    NewComponentDialog dialog = new NewComponentDialog(ivyPackages, repositoryDir, preferences);

    dialog.setVisible(true);
  }

  private void doQuitAction()
  {
    savePreferences();
    dispose();
    System.exit(0);
  }

  private void savePreferences()
  {
    if (repositoryDir != null)
    {
      preferences.put(REPOSITORY_LOCATION, repositoryDir.getAbsolutePath());
    }
  }

  private void loadPreferences()
  {
    String dirName = preferences.get(REPOSITORY_LOCATION, null);

    validateIvyRepositoryLocation(dirName);
  }

  public static void centerApp(Object object)
  {
    if (object instanceof Component)
    {
      Component comp           = (Component) object;
      Toolkit   defaultToolkit = Toolkit.getDefaultToolkit();
      Dimension screenSize     = defaultToolkit.getScreenSize();
      int       x              = (int) ((screenSize.getWidth() - comp.getWidth()) / 2);
      int       y              = (int) ((screenSize.getHeight() - comp.getHeight()) / 2);

      comp.setBounds(x, y, comp.getWidth(), comp.getHeight());
    }
  }

  private void findFiles()
  {
    ivyPackages = new ArrayList<IvyRepositoryItem>();

    if (repositoryDir != null)
    {
      FileHandler fileHandler = new FileHandler(this, repositoryDir, ivyPackages);

      fileHandler.execute();
    }
  }

  // -------------------------- OTHER METHODS --------------------------

  public void showNormal() {}

  // --------------------------- main() method ---------------------------

  public static void main(String[] args)
  {
    BuilderMainFrame builderMainFrame = new BuilderMainFrame();
  }

  // --------------------- GETTER / SETTER METHODS ---------------------

  public void setStatusLabel(String text)
  {
    statusLabel.setText(text);
  }
}
