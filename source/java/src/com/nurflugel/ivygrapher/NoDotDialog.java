package com.nurflugel.ivygrapher;

import com.nurflugel.common.ui.Util;
import javax.swing.*;
import static javax.swing.JComponent.*;
import java.awt.event.*;
import java.awt.*;
import static java.awt.event.KeyEvent.*;
import java.io.File;

public class NoDotDialog extends JDialog
{
  private JButton    buttonCancel;
  private JButton    useTextBoxButton;
  private JPanel     contentPane;
  private JButton    openFileChooserButton;
  private JTextField pathTextField;
  private File       file;

  public NoDotDialog(String dotExecutablePath)
  {
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(useTextBoxButton);
    pathTextField.setText(dotExecutablePath);

    useTextBoxButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          file = new File(pathTextField.getText());
          dispose();
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
      }, KeyStroke.getKeyStroke(VK_ESCAPE, 0), WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    openFileChooserButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent actionEvent)
        {
          onOK();
        }
      });
    pack();
    Util.center(this);
    setVisible(true);
  }

  private void onOK()
  {
    JFileChooser fileChooser = new JFileChooser();
    int          result      = fileChooser.showOpenDialog(this);

    if (result == JFileChooser.APPROVE_OPTION)
    {
      file = fileChooser.getSelectedFile();
    }

    dispose();
  }

  private void onCancel()
  {
    // add your code here if necessary
    dispose();
  }

  public static void main(String[] args)
  {
    NoDotDialog dialog = new NoDotDialog("Test message for not finding path");

    dialog.pack();
    dialog.setVisible(true);
    System.exit(0);
  }

  public File getFile()
  {
    return file;
  }
}
