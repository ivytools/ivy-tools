package com.nurflugel.common.ui;

import static com.nurflugel.common.ui.Util.centerApp;
import com.nurflugel.ivybrowser.AppPreferences;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import static javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
import javax.swing.JDialog;
import javax.swing.JPanel;
import static javax.swing.KeyStroke.getKeyStroke;

public class FindMultiplePreferencesItemsDialog extends JDialog
{
  public static final String EMPTY_STRING     = "";
  private JPanel             contentPane;
  private JButton            buttonOK;
  private JButton            buttonCancel;
  private JComboBox          comboBox;
  private JPanel             borderTitlePanel;
  private AppPreferences     appPreferences;
  private String             keyBase;
  private List<String>       locations;
  private boolean            isOK;

  public FindMultiplePreferencesItemsDialog(AppPreferences appPreferences, String borderTitle, String keyBase)
  {
    this.appPreferences = appPreferences;
    this.keyBase        = keyBase;

    // EtchedBorder border = (EtchedBorder) borderTitlePanel.getBorder();
    // TitledBorder newBorder = BorderFactory.createTitledBorder(border, borderTitle);
    //
    // borderTitlePanel.setBorder(newBorder);
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);
    addListeners();
    locations = new ArrayList<String>();

    for (int i = 0; i < 10; i++)
    {
      String value = appPreferences.getIndexedProperty(keyBase, i);

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
      }, getKeyStroke(VK_ESCAPE, 0), WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
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
    appPreferences.saveIndexedProperties(keyBase, locations);
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
