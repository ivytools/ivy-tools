package com.nurflugel.ivybrowser.ui;

import static com.nurflugel.ivybrowser.ui.BuilderMainFrame.centerApp;
import static com.nurflugel.ivybrowser.ui.IvyBrowserMainFrame.*;
import java.awt.*;
import java.awt.event.*;
import static java.awt.event.KeyEvent.*;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.*;
import static javax.swing.KeyStroke.getKeyStroke;

public class FindIvyRepositoryDialog extends JDialog
{
  private static final String EMPTY_STRING   = "";
  private JPanel              contentPane;
  private JButton             buttonOK;
  private JButton             buttonCancel;
  private JComboBox           comboBox;
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

  // -------------------------- OTHER METHODS --------------------------

  public String getRepositoryLocation()
  {
    return (String) comboBox.getSelectedItem();
  }
}
