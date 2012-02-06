package com.nurflugel.versionfinder;

import com.nurflugel.common.ui.Util;
import javax.swing.*;
import java.awt.event.*;

public class UsernamePasswordDialog extends JDialog
{
  private JPanel         contentPane;
  private JButton        buttonOK;
  private JButton        buttonCancel;
  private JTextField     usernameField;
  private JPasswordField passwordField;

  public UsernamePasswordDialog(String userName, String password)
  {
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);
    usernameField.setText(userName);
    passwordField.setText(password);
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
      }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    setTitle("Enter username/password for Subversion");
    pack();
    setSize(400, (int) getSize().getHeight());
    Util.center(this);
    setVisible(true);
  }

  private void onOK()
  {
    // add your code here
    dispose();
  }

  private void onCancel()
  {
    // add your code here if necessary
    dispose();
  }

  public String getUsername()
  {
    return usernameField.getText();
  }

  public String getPassword()
  {
    return passwordField.getText();
  }

  public static void main(String[] args)
  {
    UsernamePasswordDialog dialog = new UsernamePasswordDialog("dibble", "");

    System.exit(0);
  }
}
