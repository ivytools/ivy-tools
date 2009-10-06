package com.nurflugel.ivybrowser.ui;

import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivybrowser.handlers.BaseWebHandler;
import static com.nurflugel.ivybrowser.ui.BuilderMainFrame.centerApp;
import java.awt.*;
import static java.awt.Cursor.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import static java.awt.event.KeyEvent.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import static javax.swing.BoxLayout.*;
import static javax.swing.JComponent.*;

public class IvyLineDialog extends JDialog
{
    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 4553280884847762492L;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JCheckBox forceThisVersionCheckBox;
    private JPanel dependenciesPanel;
    private JPanel ivyTextPanel;
    private JPanel includedFilesPanel;
    private IvyPackage ivyPackage;
    private String ivyRepositoryPath;
    private IvyBrowserMainFrame mainFrame;

    public IvyLineDialog(IvyPackage ivyPackage, String ivyRepositoryPath, IvyBrowserMainFrame mainFrame)
    {
        this.ivyPackage = ivyPackage;
        this.ivyRepositoryPath = ivyRepositoryPath;
        this.mainFrame = mainFrame;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        LayoutManager textboxLayout = new BoxLayout(ivyTextPanel, Y_AXIS);
        LayoutManager boxLayout = new BoxLayout(dependenciesPanel, Y_AXIS);
        LayoutManager filesLayout = new BoxLayout(includedFilesPanel, Y_AXIS);

        ivyTextPanel.setLayout(textboxLayout);
        dependenciesPanel.setLayout(boxLayout);
        includedFilesPanel.setLayout(filesLayout);
        addListeners();
        createText();
        pack();

        Dimension requestedSize = getSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        if (screenSize.height < requestedSize.height)
        {
            setSize(requestedSize.width, screenSize.height);
        }

        centerApp(this);
    }

    private void createText()
    {
        List<IvyPackage> dependencies = ivyPackage.getDependencies();

        dependenciesPanel.removeAll();

        if (dependencies.isEmpty())
        {
            dependenciesPanel.add(new JLabel("No dependencies found"));
        }
        else
        {
            List<IvyPackageCheckbox> sortedCheckboxes = new ArrayList<IvyPackageCheckbox>();

            for (final IvyPackage dependency : dependencies)
            {
                IvyPackageCheckbox checkBox = new IvyPackageCheckbox(dependency);

                checkBox.setToolTipText("Right-click to bring up another window of this item's dependencies");
                checkBox.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        updatePastedText();
                    }
                });
                checkBox.addMouseListener(new MouseAdapter()
                {
                    /** Popup still yet another line dialog box if they click on the label of the checkbox. */
                    @Override
                    public void mouseClicked(MouseEvent mouseEvent)
                    {
                        setCursor(getPredefinedCursor(WAIT_CURSOR));

                        // get package from look up of org module rev in hashtable
                        IvyPackage newIvyPackage = getPackageFromMap(dependency, mainFrame.getPackageMap());
                        IvyLineDialog lineDialog = new IvyLineDialog(newIvyPackage, ivyRepositoryPath, mainFrame);

                        lineDialog.setVisible(true);
                        setCursor(getPredefinedCursor(DEFAULT_CURSOR));
                    }
                });
                sortedCheckboxes.add(checkBox);
            }

            Collections.sort(sortedCheckboxes);

            for (IvyPackageCheckbox sortedCheckbox : sortedCheckboxes)
            {
                dependenciesPanel.add(sortedCheckbox);
            }
        }

        populateIncludedJarsPanel();
        updatePastedText();
    }

    private IvyPackage getPackageFromMap(IvyPackage dependency, Map<String, Map<String, Map<String, IvyPackage>>> packageMap)
    {
        String orgName = dependency.getOrgName();
        String moduleName = dependency.getModuleName();
        String version = dependency.getVersion();
        Map<String, Map<String, IvyPackage>> modules = packageMap.get(orgName);
        Map<String, IvyPackage> versions = modules.get(moduleName);
        IvyPackage aPackage = versions.get(version);

        return aPackage;
    }

    private void populateIncludedJarsPanel()
    {
        final String orgName = ivyPackage.getOrgName();
        final String moduleName = ivyPackage.getModuleName();
        final String version = ivyPackage.getVersion();
        final BaseWebHandler handler = HandlerFactory.getHandler(mainFrame, ivyRepositoryPath, null, mainFrame.getPackageMap());
        List<String> includedFiles = handler.findIncludedFiles(ivyRepositoryPath, orgName, moduleName, version);

        Collections.sort(includedFiles);

        int height = 0;

        for (String includedFile : includedFiles)
        {
            final JLabel fileLabel = new JLabel(includedFile);

            fileLabel.setToolTipText("Click to download this file");
            includedFilesPanel.add(fileLabel);
            height += fileLabel.getPreferredSize().height;
            fileLabel.addMouseListener(new MouseAdapter()
            {
                /** download the file. */
                @Override
                public void mouseClicked(MouseEvent mouseEvent)
                {
                    handler.downloadFile(fileLabel, orgName, moduleName, version);
                }
            });
        }

        height += 15;

        Dimension newSize = new Dimension(includedFilesPanel.getWidth(), height);

        includedFilesPanel.setSize(newSize);
        includedFilesPanel.setMinimumSize(newSize);
        includedFilesPanel.setPreferredSize(newSize);
    }

    @SuppressWarnings({"StringConcatenationInsideStringBufferAppend"})
    private void updatePastedText()
    {
        ivyTextPanel.removeAll();
        getPasteText();
        pack();
        centerApp(this);
    }

    private void getPasteText()
    {
        String sourceTag = ivyPackage.hasSourceCode() ? ",source"
                                                      : "";
        String javadocTag = ivyPackage.hasJavaDocs() ? ",javadoc"
                                                     : "";
        String forceText = forceThisVersionCheckBox.isSelected() ? " force=\"true\" "
                                                                 : "";
        StringBuilder pasteText = new StringBuilder();
        List<IvyPackage> excludedPackages = new ArrayList<IvyPackage>();
        Component[] components = dependenciesPanel.getComponents();

        for (Component component : components)
        {
            if (component instanceof IvyPackageCheckbox)
            {
                IvyPackageCheckbox checkbox = (IvyPackageCheckbox) component;

                if (!checkbox.isSelected())
                {
                    excludedPackages.add(checkbox.getIvyPackage());
                }
            }
        }

        String text = "";

        if (excludedPackages.isEmpty())
        {
            text = "<dependency org=\"" + ivyPackage.getOrgName() + "\"  name=\"" + ivyPackage.getModuleName() + "\"  rev=\"" + ivyPackage.getVersion()
                   + "\"  conf=\"build,dist-war,test" + sourceTag + javadocTag + "\"" + forceText + "/>";
            ivyTextPanel.add(new JLabel(text));
            pasteText.append(text);
        }
        else
        {
            text = "<dependency org=\"" + ivyPackage.getOrgName() + "\"  name=\"" + ivyPackage.getModuleName() + "\"  rev=\"" + ivyPackage.getVersion()
                   + "\"  conf=\"dist-ear" + sourceTag + javadocTag + "\"" + forceText + ">";
            ivyTextPanel.add(new JLabel(text));
            pasteText.append(text);

            for (IvyPackage excludedPackage : excludedPackages)
            {
                text = "    <exclude org=\"" + excludedPackage.getOrgName() + "\" name=\"" + excludedPackage.getModuleName() + "\"/>";
                ivyTextPanel.add(new JLabel(text));
                pasteText.append("\n").append(text);
            }

            text = "</dependency>";
            ivyTextPanel.add(new JLabel(text));
            pasteText.append("\n").append(text);
        }

        StringSelection ivyLine = new StringSelection(pasteText.toString());

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ivyLine, null);
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

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(VK_ESCAPE, 0), WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        forceThisVersionCheckBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                updatePastedText();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    private void onOK()
    {
        getPasteText();
        dispose();
    }

    private void onCancel()
    {
        dispose();
    }

    public static void main(String[] args)
    {
        IvyPackage ivyPackage = new IvyPackage("org.apache", "commons-lang", "2.1");

        ivyPackage.setHasJavaDocs(true);
        ivyPackage.setHasSourceCode(true);

        IvyLineDialog dialog = new IvyLineDialog(ivyPackage, "something", null);

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
        contentPane.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        contentPane.add(panel1,
                        new com.intellij.uiDesigner.core.GridConstraints(3, 0, 2, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH,
                                                                         com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                                                         com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel2, gbc);
        buttonOK = new JButton();
        buttonOK.setText("OK");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(buttonOK, gbc);
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(buttonCancel, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3,
                        new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH,
                                                                         com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                                                         com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                         com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                                                         com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("The following line has been pasted into your cut/copy/paste buffer:");
        panel3.add(label1,
                   new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE,
                                                                    com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null,
                                                                    null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        panel4.setDoubleBuffered(false);
        panel4.setMaximumSize(new Dimension(2147483647, 48));
        panel3.add(panel4,
                   new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH,
                                                                    com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                                                    com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
                                                                    null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Options"));
        forceThisVersionCheckBox = new JCheckBox();
        forceThisVersionCheckBox.setText("Force this version");
        forceThisVersionCheckBox.setToolTipText("If checked, this version will override any others in your Ivy configuration.  Use with caution!");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel4.add(forceThisVersionCheckBox, gbc);
        ivyTextPanel = new JPanel();
        ivyTextPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        panel3.add(ivyTextPanel,
                   new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH,
                                                                    com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                                                    com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                    com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                                                    com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        contentPane.add(scrollPane1,
                        new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH,
                                                                         com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                                                         com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW,
                                                                         com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                                                         com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        dependenciesPanel = new JPanel();
        dependenciesPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        dependenciesPanel.setToolTipText("If any of these are unchecked, that dependency will not be brought in.  Use with caution!");
        scrollPane1.setViewportView(dependenciesPanel);
        dependenciesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Dependencies to include"));
        includedFilesPanel = new JPanel();
        includedFilesPanel.setLayout(new BorderLayout(0, 0));
        includedFilesPanel.setMinimumSize(new Dimension(12, 40));
        includedFilesPanel.setPreferredSize(new Dimension(12, 40));
        includedFilesPanel.setToolTipText("These jars come across from Ivy into your unversioned/lib dirs");
        contentPane.add(includedFilesPanel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
                                                                                             com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL,
                                                                                             com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK |
                                                                                             com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                                             com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 100), null, null, 0,
                                                                                             false));
        includedFilesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Included files:"));
    }

    /** @noinspection ALL */
    public JComponent $$$getRootComponent$$$()
    {
        return contentPane;
    }
}
