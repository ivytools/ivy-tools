package com.nurflugel.ivybuilder;

import com.nurflugel.ivybrowser.domain.IvyRepositoryItem;
import com.nurflugel.ivybuilder.handlers.FileHandler;
import com.nurflugel.ivybuilder.ui.NewComponentDialog;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import static com.nurflugel.common.ui.Util.*;
import static com.nurflugel.common.ui.Version.VERSION;
import static java.util.prefs.Preferences.userNodeForPackage;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.DIRECTORIES_ONLY;
import static javax.swing.JOptionPane.showConfirmDialog;

/** Created by IntelliJ IDEA. User: dbulla Date: Jan 18, 2008 Time: 9:36:34 PM To change this template use File | Settings | File Templates. */
public class BuilderMainFrame extends JFrame
{
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
    setTitle("IvyBuild v. " + VERSION);
    setContentPane(contentsPanel);
    addListeners();
    setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", this);
    centerApp(this);
    preferences = userNodeForPackage(BuilderMainFrame.class);
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
    addHelpListener("ivyBuilderHelp.hs", helpButton, this);
  }

  private void findRepositoryDir()
  {
    JFileChooser chooser = new JFileChooser();

    chooser.setDialogTitle("Select the repository dir");
    chooser.setFileSelectionMode(DIRECTORIES_ONLY);

    int returnVal = chooser.showDialog(this, "Use this directory");

    if (returnVal == APPROVE_OPTION)
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
        showConfirmDialog(this, "The dir must be named \"" + REPOSITORY + '"');
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
