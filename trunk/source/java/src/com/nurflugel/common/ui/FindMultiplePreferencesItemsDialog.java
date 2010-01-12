package com.nurflugel.common.ui;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import static com.nurflugel.common.ui.Util.centerApp;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static javax.swing.KeyStroke.getKeyStroke;

public class FindMultiplePreferencesItemsDialog extends JDialog
{
  private static final String EMPTY_STRING     = "";
  private JPanel              contentPane;
  private JButton             buttonOK;
  private JButton             buttonCancel;
  private JComboBox           comboBox;
  private JPanel              borderTitlePanel;
  private Preferences         appPreferences;
  private String              keyBase;
  private List<String>        locations;
  private boolean             isOK;

  public FindMultiplePreferencesItemsDialog(Preferences appPreferences, String borderTitle, String keyBase)
  {
    this.appPreferences = appPreferences;
    this.keyBase        = keyBase;

//    EtchedBorder border = (EtchedBorder) borderTitlePanel.getBorder();
//    TitledBorder newBorder = BorderFactory.createTitledBorder(border, borderTitle);
//
//    borderTitlePanel.setBorder(newBorder);
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);
    addListeners();
    locations = new ArrayList<String>();

    for (int i = 0; i < 10; i++)
    {
      String key   = keyBase + i;
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
      }, getKeyStroke(VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }

  private void onOK()
  {
    isOK = true;

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
      appPreferences.put(keyBase + i++, value);
    }

    dispose();
  }

  private void onCancel()
  {
    // add your code here if necessary
    isOK = false;
    dispose();
  }

  // -------------------------- OTHER METHODS --------------------------

  public String getRepositoryLocation()
  {
    return (String) comboBox.getSelectedItem();
  }

  public boolean isOk()
  {
    return isOK;
  }
}
