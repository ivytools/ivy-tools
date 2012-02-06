package com.nurflugel.ivybrowser.ui;

import static com.nurflugel.common.ui.Util.centerApp;

import com.nurflugel.ivybrowser.domain.IvyRepositoryItem;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import static java.awt.Toolkit.getDefaultToolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import static javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.FILES_ONLY;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

@SuppressWarnings({ "AssignmentToCollectionOrArrayFieldFromParameter", "MethodParameterNamingConvention" })
public class NewComponentDialog extends JDialog
{
  /** Use serialVersionUID for interoperability. */
  private static final long                      serialVersionUID                 = 2069426482124193511L;
  private JPanel                                 contentPane;
  private JButton                                buttonOK;
  private JButton                                buttonCancel;
  private JTextField                             orgField;
  private JTextField                             moduleField;
  private JTextField                             revField;
  private JButton                                addFilesToComponentButton;
  private JButton                                addDependenciesToComponentButton;
  private JPanel                                 dependenciesPanel;
  private JPanel                                 filesPanel;
  private List<IvyRepositoryItem>                ivyPackages                      = new ArrayList<IvyRepositoryItem>();
  private File                                   repositoryDir;
  private Preferences                            preferences;
  private Map<String, IvyFileCheckbox>           filesMap                         = new TreeMap<String, IvyFileCheckbox>();
  private Map<String, IvyRepositoryItemCheckbox> dependenciesMap                  = new TreeMap<String, IvyRepositoryItemCheckbox>();

  // --------------------------- CONSTRUCTORS ---------------------------
  public NewComponentDialog(final List<IvyRepositoryItem> ivyPackages, File repositoryDir, Preferences preferences)
  {
    this.ivyPackages   = ivyPackages;
    this.repositoryDir = repositoryDir;
    this.preferences   = preferences;
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addListeners();
    pack();
    setSize(400, 600);

    LayoutManager boxLayout = new BoxLayout(dependenciesPanel, BoxLayout.Y_AXIS);

    dependenciesPanel.setLayout(boxLayout);

    LayoutManager fboxLayout = new BoxLayout(filesPanel, BoxLayout.Y_AXIS);

    filesPanel.setLayout(fboxLayout);
    centerApp(this);
  }

  private void addListeners()
  {
    buttonOK.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          onOK();
        }
      });
    buttonCancel.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          onCancel();
        }
      });
    addWindowListener(new WindowAdapter()
      {
        @Override
        public void windowClosing(WindowEvent e)
        {
          onCancel();
        }
      });
    contentPane.registerKeyboardAction(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          onCancel();
        }
      }, KeyStroke.getKeyStroke(VK_ESCAPE, 0), WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    orgField.addKeyListener(new KeyAdapter()
      {
        @Override
        public void keyTyped(KeyEvent e)
        {
          enableButtons();
        }
      });
    moduleField.addKeyListener(new KeyAdapter()
      {
        @Override
        public void keyTyped(KeyEvent e)
        {
          enableButtons();
        }
      });
    revField.addKeyListener(new KeyAdapter()
      {
        @Override
        public void keyTyped(KeyEvent e)
        {
          enableButtons();
        }
      });
    addFilesToComponentButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          addFilesToComponent();
        }
      });
    addDependenciesToComponentButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          addDependencies();
        }
      });
  }

  private void onOK()
  {
    IvyRepositoryItem       newItem      = new IvyRepositoryItem(orgField.getText(), moduleField.getText(), revField.getText(), repositoryDir);
    List<IvyRepositoryItem> dependencies = new ArrayList<IvyRepositoryItem>();

    for (Component component : dependenciesPanel.getComponents())
    {
      IvyRepositoryItemCheckbox checkbox          = (IvyRepositoryItemCheckbox) component;
      IvyRepositoryItem         ivyRepositoryItem = checkbox.getItem();

      dependencies.add(ivyRepositoryItem);
    }

    newItem.setDependencies(dependencies);

    List<File> files = getFilesFromFilesPanel();

    if (!files.isEmpty())
    {
      String parentDir = files.get(0).getParent();

      preferences.put("basefilename", parentDir);
    }

    newItem.setFiles(files);
    saveItem(newItem);
    dispose();
  }

  private List<File> getFilesFromFilesPanel()
  {
    List<File> files = new ArrayList<File>();

    for (Component component : filesPanel.getComponents())
    {
      IvyFileCheckbox checkbox = (IvyFileCheckbox) component;

      if (checkbox.isSelected())
      {
        files.add(checkbox.getFile());
      }
    }

    return files;
  }

  private void saveItem(IvyRepositoryItem newItem)
  {
    newItem.saveToDisk();
    ivyPackages.add(newItem);

    String          ivyLine = newItem.getIvyLine();
    StringSelection text    = new StringSelection(ivyLine);

    getDefaultToolkit().getSystemClipboard().setContents(text, null);
    JOptionPane.showMessageDialog(null,
                                  "Files and checksums generated, maint line copied into \nthe paste buffer (if you wish to run it):\n" + ivyLine);
  }

  private void onCancel()
  {
    dispose();
  }

  private void enableButtons()
  {
    boolean isOkToSave = isOkToSave();

    buttonOK.setEnabled(isOkToSave);
    addFilesToComponentButton.setEnabled(isOkToSave);
    addDependenciesToComponentButton.setEnabled(isOkToSave);
  }

  @SuppressWarnings({ "OverlyComplexBooleanExpression" })
  private boolean isOkToSave()
  {
    String org    = orgField.getText();
    String module = moduleField.getText();
    String rev    = revField.getText();

    return (org != null) && (org.length() > 0) && (module != null) && (module.length() > 0) && (rev != null) && (rev.length() > 0);
  }

  private void addFilesToComponent()
  {
    File baseFile = null;

    if (preferences != null)
    {
      String baseFileName = preferences.get("basefilename", ".");

      if (!baseFileName.equals("."))
      {
        baseFile = new File(baseFileName);
      }
    }

    JFileChooser chooser = new JFileChooser();

    if (baseFile != null)
    {
      chooser.setCurrentDirectory(baseFile);
    }

    chooser.setDialogTitle("Pick files to add to the component");
    chooser.setFileSelectionMode(FILES_ONLY);
    chooser.setMultiSelectionEnabled(true);

    int returnVal = chooser.showDialog(this, "Add files to Ivy");

    if (returnVal == APPROVE_OPTION)  // add any selected files to the map of files - this eliminates dupes
    {
      File[] selectedFiles = chooser.getSelectedFiles();

      for (File file : selectedFiles)
      {
        IvyFileCheckbox checkbox = new IvyFileCheckbox(file);

        filesMap.put(file.getAbsolutePath(), checkbox);
        preferences.put("basefilename", file.getParentFile().getAbsolutePath());
      }

      // now get all checkboxes in order, add them to the panel
      filesPanel.removeAll();

      Collection<IvyFileCheckbox> checkboxes = filesMap.values();

      for (IvyFileCheckbox checkbox : checkboxes)
      {
        filesPanel.add(checkbox);
      }

      pack();
      centerApp(this);
    }
  }

  private void addDependencies()
  {
    CreateDependenciesDialog dependenciesDialog = new CreateDependenciesDialog(ivyPackages);
    List<IvyRepositoryItem>  dependencies       = dependenciesDialog.getDependancies();

    for (IvyRepositoryItem dependency : dependencies)
    {
      IvyRepositoryItemCheckbox checkbox = new IvyRepositoryItemCheckbox(dependency);

      dependenciesMap.put(dependency.getIvyLine(), checkbox);
    }

    dependenciesPanel.removeAll();

    Collection<IvyRepositoryItemCheckbox> checkboxes = dependenciesMap.values();

    for (IvyRepositoryItemCheckbox checkbox : checkboxes)
    {
      dependenciesPanel.add(checkbox);
    }

    dependenciesPanel.invalidate();
    invalidate();

    Dimension size = getSize();

    pack();
    setSize(size);
    addDependenciesToComponentButton.requestFocus();
  }

  // --------------------------- main() method ---------------------------
  @SuppressWarnings({ "CallToSystemExit" })
  public static void main(String[] args)
  {
    NewComponentDialog dialog = new NewComponentDialog(new ArrayList<IvyRepositoryItem>(), new File("dibble"), null);

    dialog.pack();
    dialog.setVisible(true);
    System.exit(0);
  }
}
