package net.nike.ivybrowser.ui;

import net.nike.ivybrowser.domain.IvyPackage;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;


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
    private IvyPackage ivyPackage;

    public IvyLineDialog(IvyPackage ivyPackage)
    {
        this.ivyPackage = ivyPackage;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        LayoutManager textboxLayout = new BoxLayout(ivyTextPanel, BoxLayout.Y_AXIS);
        ivyTextPanel.setLayout(textboxLayout);

        LayoutManager boxLayout = new BoxLayout(dependenciesPanel, BoxLayout.Y_AXIS);
        dependenciesPanel.setLayout(boxLayout);
        addListeners();
        createText();
        pack();
        Dimension requestedSize = getSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (screenSize.height < requestedSize.height)
        {
            setSize(requestedSize.width, screenSize.height);
        }
        BuilderMainFrame.centerApp(this);
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
            for (IvyPackage dependency : dependencies)
            {
                JCheckBox checkBox = new IvyPackageCheckbox(dependency);
                checkBox.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        updatePastedText();
                    }
                });
                dependenciesPanel.add(checkBox);
            }
        }

        updatePastedText();
    }

    @SuppressWarnings({"StringConcatenationInsideStringBufferAppend"})
    private void updatePastedText()
    {
        String sourceTag = ivyPackage.hasSourceCode()
                           ? ",source"
                           : "";
        String javadocTag = ivyPackage.hasJavaDocs()
                            ? ",javadoc"
                            : "";
        String forceText = forceThisVersionCheckBox.isSelected()
                           ? " force=\"true\" "
                           : "";
        ivyTextPanel.removeAll();

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

        StringBuilder pasteText = new StringBuilder();
        String text = "";

        if (excludedPackages.isEmpty())
        {
            text = "<dependency org=\"" + ivyPackage.getOrgName() + "\"  name=\"" + ivyPackage.getModuleName() + "\"  rev=\"" +
                   ivyPackage.getVersion() + "\"  conf=\"dist-ear" + sourceTag + javadocTag + "\"" + forceText + "/>";
            ivyTextPanel.add(new JLabel(text));
            pasteText.append(text);
        }
        else
        {
            text = "<dependency org=\"" + ivyPackage.getOrgName() + "\"  name=\"" + ivyPackage.getModuleName() + "\"  rev=\"" +
                   ivyPackage.getVersion() + "\"  conf=\"dist-ear" + sourceTag + javadocTag + "\"" + forceText + ">";
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
        pack();
        BuilderMainFrame.centerApp(this);
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
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
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

        // ivyPackage.setDependencies();
        IvyLineDialog dialog = new IvyLineDialog(ivyPackage);
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
        contentPane.setLayout(new GridBagLayout());
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(panel1, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
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
        panel3.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(panel3, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("The following line has been pasted into your cut/copy/paste buffer:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(label1, gbc);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        panel4.setDoubleBuffered(false);
        panel4.setMaximumSize(new Dimension(2147483647, 48));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel3.add(panel4, gbc);
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
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel3.add(ivyTextPanel, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(scrollPane1, gbc);
        dependenciesPanel = new JPanel();
        dependenciesPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        dependenciesPanel.setToolTipText("If any of these are unchecked, that dependency will not be brought in.  Use with caution!");
        scrollPane1.setViewportView(dependenciesPanel);
        dependenciesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Dependencies to include"));
    }

    /** @noinspection ALL */
    public JComponent $$$getRootComponent$$$()
    {
        return contentPane;
    }
}