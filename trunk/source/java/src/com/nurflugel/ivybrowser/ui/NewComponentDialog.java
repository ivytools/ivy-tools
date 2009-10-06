package com.nurflugel.ivybrowser.ui;

import com.nurflugel.ivybrowser.domain.IvyRepositoryItem;
import static com.nurflugel.ivybrowser.ui.BuilderMainFrame.centerApp;
import java.awt.*;
import static java.awt.Toolkit.getDefaultToolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.*;
import static javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.FILES_ONLY;

@SuppressWarnings({"AssignmentToCollectionOrArrayFieldFromParameter", "MethodParameterNamingConvention"})
public class NewComponentDialog extends JDialog
{
    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 2069426482124193511L;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField orgField;
    private JTextField moduleField;
    private JTextField revField;
    private JButton addFilesToComponentButton;
    private JButton addDependenciesToComponentButton;
    private JPanel dependenciesPanel;
    private JPanel filesPanel;
    private List<IvyRepositoryItem> ivyPackages = new ArrayList<IvyRepositoryItem>();
    private File repositoryDir;
    private Preferences preferences;
    private Map<String, IvyFileCheckbox> filesMap = new TreeMap<String, IvyFileCheckbox>();
    private Map<String, IvyRepositoryItemCheckbox> dependenciesMap = new TreeMap<String, IvyRepositoryItemCheckbox>();

    // --------------------------- CONSTRUCTORS ---------------------------
    public NewComponentDialog(final List<IvyRepositoryItem> ivyPackages, File repositoryDir, Preferences preferences)
    {
        this.ivyPackages = ivyPackages;
        this.repositoryDir = repositoryDir;
        this.preferences = preferences;
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

    private void addDependencies()
    {
        CreateDependenciesDialog dependenciesDialog = new CreateDependenciesDialog(ivyPackages);
        List<IvyRepositoryItem> dependencies = dependenciesDialog.getDependancies();

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

    private void onOK()
    {
        IvyRepositoryItem newItem = new IvyRepositoryItem(orgField.getText(), moduleField.getText(), revField.getText(), repositoryDir);
        List<IvyRepositoryItem> dependencies = new ArrayList<IvyRepositoryItem>();

        for (Component component : dependenciesPanel.getComponents())
        {
            IvyRepositoryItemCheckbox checkbox = (IvyRepositoryItemCheckbox) component;
            IvyRepositoryItem ivyRepositoryItem = checkbox.getItem();

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

        String ivyLine = newItem.getIvyLine();
        StringSelection text = new StringSelection(ivyLine);

        getDefaultToolkit().getSystemClipboard().setContents(text, null);
        JOptionPane.showMessageDialog(null, "Line to paste into your ivy file (already copied into the paste buffer):\n" + ivyLine);
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

    @SuppressWarnings({"OverlyComplexBooleanExpression"})
    private boolean isOkToSave()
    {
        String org = orgField.getText();
        String module = moduleField.getText();
        String rev = revField.getText();

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

        if (returnVal == APPROVE_OPTION)
        {  // add any selected files to the map of files - this eliminates dupes

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

    // --------------------------- main() method ---------------------------
    @SuppressWarnings({"CallToSystemExit"})
    public static void main(String[] args)
    {
        NewComponentDialog dialog = new NewComponentDialog(new ArrayList<IvyRepositoryItem>(), new File("dibble"), null);

        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
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
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        contentPane.add(panel1, BorderLayout.CENTER);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Component Properties"));
        final JLabel label1 = new JLabel();
        label1.setText("Org:");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        panel1.add(label1, gbc);
        orgField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(orgField, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Module:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel1.add(label2, gbc);
        moduleField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(moduleField, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Rev:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel1.add(label3, gbc);
        revField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(revField, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel2, gbc);
        addFilesToComponentButton = new JButton();
        addFilesToComponentButton.setEnabled(false);
        addFilesToComponentButton.setText("Add files to component");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(addFilesToComponentButton, gbc);
        addDependenciesToComponentButton = new JButton();
        addDependenciesToComponentButton.setEnabled(false);
        addDependenciesToComponentButton.setText("Add dependencies to component");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(addDependenciesToComponentButton, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(scrollPane1, gbc);
        filesPanel = new JPanel();
        filesPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        scrollPane1.setViewportView(filesPanel);
        filesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Files to be added"));
        final JScrollPane scrollPane2 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(scrollPane2, gbc);
        dependenciesPanel = new JPanel();
        dependenciesPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        scrollPane2.setViewportView(dependenciesPanel);
        dependenciesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Dependencies"));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout(0, 0));
        contentPane.add(panel3, BorderLayout.SOUTH);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new BorderLayout(0, 0));
        panel3.add(panel4, BorderLayout.EAST);
        buttonOK = new JButton();
        buttonOK.setEnabled(false);
        buttonOK.setText("Save");
        panel4.add(buttonOK, BorderLayout.WEST);
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel4.add(buttonCancel, BorderLayout.CENTER);
    }

    /** @noinspection ALL */
    public JComponent $$$getRootComponent$$$()
    {
        return contentPane;
    }
}
