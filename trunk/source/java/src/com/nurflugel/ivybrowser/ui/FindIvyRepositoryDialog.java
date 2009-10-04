package com.nurflugel.ivybrowser.ui;

import static com.nurflugel.ivybrowser.ui.BuilderMainFrame.centerApp;
import static com.nurflugel.ivybrowser.ui.IvyBrowserMainFrame.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.*;

public class FindIvyRepositoryDialog extends JDialog
{
  private JPanel              contentPane;
  private JButton             buttonOK;
  private JButton             buttonCancel;
  private JComboBox           comboBox;
  private static final String EMPTY_STRING   = "";
  private Preferences         appPreferences;
  private List<String>        locations;

  public FindIvyRepositoryDialog(Preferences appPreferences)
  {
    this.appPreferences = appPreferences;
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);
    addListeners();
    locations = new ArrayList<String>();

    for (int i = 0; i < 10; i++)
    {
      String key   = IVY_REPOSITORY + i;
      String value = appPreferences.get(key, EMPTY_STRING);

      if (value.equalsIgnoreCase(EMPTY_STRING))
      {
        break;
      }
      else
      {
        locations.add(value);
      }
    }

    String[]      locationArray = locations.toArray(new String[locations.size()]);
    ComboBoxModel model         = new DefaultComboBoxModel(locationArray);

    comboBox.setModel(model);
    pack();
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

    // call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
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
  }

  private void onOK()
  {
    Object item     = comboBox.getSelectedItem();
    String location = (String) item;

    if (locations.contains(location))
    {
      locations.remove(location);
    }

    // put this at the top of the list so the most recent value always shows first
    locations.add(0, location);

    int i = 0;

    for (String value : locations)
    {
      appPreferences.put(IVY_REPOSITORY + i++, value);
    }

    dispose();
  }

  private void onCancel()
  {
    // add your code here if necessary
    dispose();
  }

  public String getRepositoryLocation()
  {
    return (String) comboBox.getSelectedItem();
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
    contentPane = new JPanel();
    contentPane.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));

    final JPanel panel1 = new JPanel();

    panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
    contentPane.add(panel1,
                    new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
                                                                     com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH,
                                                                     com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                     | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, 1, null,
                                                                     null, null, 0, false));

    final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();

    panel1.add(spacer1,
               new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
                                                                com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null,
                                                                null, 0, false));

    final JPanel panel2 = new JPanel();

    panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
    panel1.add(panel2,
               new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
                                                                com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null,
                                                                0, false));
    buttonOK = new JButton();
    buttonOK.setText("OK");
    panel2.add(buttonOK,
               new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
                                                                com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                                false));
    buttonCancel = new JButton();
    buttonCancel.setText("Cancel");
    panel2.add(buttonCancel,
               new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
                                                                com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                                false));

    final JPanel panel3 = new JPanel();

    panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
    contentPane.add(panel3,
                    new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
                                                                     com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH,
                                                                     com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                     | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                     com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                     | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null,
                                                                     null, 0, false));
    panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Specify Ivy repository location"));

    final JLabel label1 = new JLabel();

    label1.setHorizontalAlignment(0);
    label1.setHorizontalTextPosition(10);
    label1.setText("Pick an existing location from the dropdown, or ");
    panel3.add(label1,
               new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
                                                                com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                                false));

    final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();

    panel3.add(spacer2,
               new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
                                                                com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null,
                                                                0, false));

    final JLabel label2 = new JLabel();

    label2.setHorizontalAlignment(0);
    label2.setText("enter a new location and click \"OK\"");
    panel3.add(label2,
               new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER,
                                                                com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                                false));
    comboBox = new JComboBox();
    comboBox.setEditable(true);

    final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();

    comboBox.setModel(defaultComboBoxModel1);
    panel3.add(comboBox,
               new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST,
                                                                com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                                false));
  }

  /** @noinspection  ALL */
  public JComponent $$$getRootComponent$$$()
  {
    return contentPane;
  }
}