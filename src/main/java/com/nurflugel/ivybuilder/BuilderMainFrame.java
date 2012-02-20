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
    contentsPanel = new JPanel();
    contentsPanel.setLayout(new GridBagLayout());

    final JPanel panel1 = new JPanel();

    panel1.setLayout(new GridBagLayout());

    GridBagConstraints gbc;

    gbc           = new GridBagConstraints();
    gbc.gridx     = 0;
    gbc.gridy     = 0;
    gbc.gridwidth = 2;
    gbc.weightx   = 1.0;
    gbc.weighty   = 1.0;
    gbc.fill      = GridBagConstraints.BOTH;
    contentsPanel.add(panel1, gbc);
    panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Ivy repository location"));

    final JLabel label1 = new JLabel();

    label1.setText("Ivy repositry is located at:");
    gbc        = new GridBagConstraints();
    gbc.gridx  = 0;
    gbc.gridy  = 0;
    gbc.anchor = GridBagConstraints.EAST;
    panel1.add(label1, gbc);
    ivyReposLabel = new JLabel();
    ivyReposLabel.setText("Not selected yet");
    gbc           = new GridBagConstraints();
    gbc.gridx     = 1;
    gbc.gridy     = 0;
    gbc.gridwidth = 2;
    gbc.weightx   = 1.0;
    gbc.anchor    = GridBagConstraints.WEST;
    panel1.add(ivyReposLabel, gbc);

    final JPanel panel2 = new JPanel();

    panel2.setLayout(new GridBagLayout());
    gbc           = new GridBagConstraints();
    gbc.gridx     = 0;
    gbc.gridy     = 1;
    gbc.gridwidth = 3;
    gbc.weighty   = 1.0;
    gbc.fill      = GridBagConstraints.BOTH;
    panel1.add(panel2, gbc);
    setIvyReposButton = new JButton();
    setIvyReposButton.setText("Select Ivy Repository Dir");
    gbc         = new GridBagConstraints();
    gbc.gridx   = 1;
    gbc.gridy   = 0;
    gbc.weighty = 1.0;
    gbc.fill    = GridBagConstraints.HORIZONTAL;
    panel2.add(setIvyReposButton, gbc);

    final JPanel panel3 = new JPanel();

    panel3.setLayout(new GridBagLayout());
    gbc           = new GridBagConstraints();
    gbc.gridx     = 0;
    gbc.gridy     = 1;
    gbc.gridwidth = 2;
    gbc.weightx   = 1.0;
    gbc.weighty   = 1.0;
    gbc.fill      = GridBagConstraints.BOTH;
    contentsPanel.add(panel3, gbc);
    addNewComponentButton = new JButton();
    addNewComponentButton.setText("Add New Component");
    addNewComponentButton.setToolTipText("You must have a valid Ivy repository selected to press this");
    gbc         = new GridBagConstraints();
    gbc.gridx   = 1;
    gbc.gridy   = 0;
    gbc.weighty = 1.0;
    gbc.fill    = GridBagConstraints.HORIZONTAL;
    panel3.add(addNewComponentButton, gbc);
    quitButton = new JButton();
    quitButton.setText("Quit");
    gbc         = new GridBagConstraints();
    gbc.gridx   = 1;
    gbc.gridy   = 2;
    gbc.weighty = 1.0;
    gbc.fill    = GridBagConstraints.HORIZONTAL;
    panel3.add(quitButton, gbc);
    helpButton = new JButton();
    helpButton.setText("Help");
    gbc       = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 1;
    gbc.fill  = GridBagConstraints.HORIZONTAL;
    panel3.add(helpButton, gbc);
    statusLabel = new JLabel();
    statusLabel.setText("");
    gbc         = new GridBagConstraints();
    gbc.gridx   = 0;
    gbc.gridy   = 2;
    gbc.weightx = 1.0;
    gbc.anchor  = GridBagConstraints.WEST;
    contentsPanel.add(statusLabel, gbc);
  }

  /** @noinspection  ALL */
  public JComponent $$$getRootComponent$$$()
  {
    return contentsPanel;
  }
}
