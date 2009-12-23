package com.nurflugel.ivygrapher;

import com.nurflugel.common.ui.Util;
import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileDialog extends JDialog
{
  private JPanel  contentPane;
  private JButton buttonOK;
  private JButton buttonCancel;
  private JList   list1;
  private boolean wasOk;

  public FileDialog(File currentDir)
  {
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);

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
    pack();
    setSize(500, 1000);
    Util.center(this);

    FileDialogListModel listModel = new FileDialogListModel(currentDir);

    list1.setCellRenderer(new FileCellRenderer());
    list1.setModel(listModel);
    setTitle("Select Ivy report(s)...");

    list1.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mouseClicked(MouseEvent e)
        {
          Object value = list1.getSelectedValue();

          if (value instanceof FileWrapper)
          {
            File file = ((FileWrapper) value).getFile();

            if (file.isDirectory())
            {
              ((FileDialogListModel) list1.getModel()).setCurrentDir(file);
            }
          }
        }
      });
  }

  private void onOK()
  {
    wasOk = true;
    dispose();
  }

  private void onCancel()
  {
    dispose();
  }

  public boolean isWasOk()
  {
    return wasOk;
  }

  public List<File> getFiles()
  {
    Object[]   objects = list1.getSelectedValues();
    List<File> results = new ArrayList<File>();

    for (Object object : objects)
    {
      File file = ((FileWrapper) (object)).getFile();

      if (!file.isDirectory())
      {
        results.add(file);
      }
    }

    return results;
  }

  public static void main(String[] args)
  {
    FileDialog dialog = new FileDialog(new File("."));

    dialog.setVisible(true);
    System.exit(0);
  }
}
