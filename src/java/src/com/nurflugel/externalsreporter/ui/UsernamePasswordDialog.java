package com.nurflugel.externalsreporter.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import static com.nurflugel.common.ui.Util.center;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.apache.commons.lang.StringUtils.isEmpty;

public class UsernamePasswordDialog extends JDialog
{
  private JPanel         contentPane;
  private JButton        buttonOK;
  private JButton        buttonCancel;
  private JTextField     userNameField;
  private JPasswordField passwordField2;
  private JPasswordField passwordField;
  private Config         config;

  public UsernamePasswordDialog(Config config)
  {
    this.config = config;
    setTitle("Enter user name and password");
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);
    addListeners();

    String userName = config.getUserName();
    String password = config.getPassword();

    if (!isEmpty(userName))
    {
      userNameField.setText(userName);
    }

    if (!isEmpty(password))
    {
      passwordField.setText(password);
    }

    pack();
    center(this);
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
      }, getKeyStroke(VK_ESCAPE, 0), WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }

  private void onOK()
  {
    String userName  = userNameField.getText();
    String password  = passwordField.getText();
    String password2 = passwordField2.getText();

    if (!isEmpty(userName) && !isEmpty(password))
    {
      if (password.equals(password2))
      {
        config.setUserName(userName);
        config.setPassword(password);
        dispose();
      }
      else
      {
        showMessageDialog(null, "Passwords don't match!");
      }
    }
    else
    {
      showMessageDialog(null, "User name and password can't be blank!");
    }
  }

  private void onCancel()
  {
    // Quit
    System.exit(0);
  }

  public static void main(String[] args)
  {
    UsernamePasswordDialog dialog = new UsernamePasswordDialog(new Config());

    dialog.pack();
    dialog.setVisible(true);
    System.exit(0);
  }
}
