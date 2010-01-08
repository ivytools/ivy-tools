package com.nurflugel.ivygrapher;

import com.nurflugel.common.ui.Util;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileDialog extends JDialog
{
  private JPanel             contentPane;
  private JButton            buttonOK;
  private JButton            buttonCancel;
  private JList              fileList;
  private boolean            wasOk;
  private File               currentDir;
  public static final String UP = ".. (up one level)";

  public FileDialog(final File currentDir)
  {
    this.currentDir = currentDir;
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

    FileDialogListModel listModel = new FileDialogListModel(currentDir, this);

    fileList.setCellRenderer(new FileCellRenderer());
    fileList.setModel(listModel);
    setTitle("Select Ivy report(s)...");

    fileList.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mouseClicked(MouseEvent e)
        {
          Object value = fileList.getSelectedValue();

          System.out.println("value = " + value);

          if (value instanceof FileWrapper)
          {
            handleSelection(e, value);
          }
        }
      });
  }

  private void handleSelection(MouseEvent e, Object value)
  {
    File file = ((FileWrapper) value).getFile();

    if (file.getName().equals(UP))
    {
      file = currentDir.getParentFile();
    }

    if (file.isDirectory())
    {
      System.out.println("file = " + file.getName());

      fileList.clearSelection();
      currentDir = file;
      ((FileDialogListModel) fileList.getModel()).setCurrentDir(file);
    }
    else if (e.getClickCount() == 2)
    {
      onOK();
    }
  }

  private void onCancel()
  {
    dispose();
  }

  private void onOK()
  {
    wasOk = true;
    dispose();
  }

  // -------------------------- OTHER METHODS --------------------------

  public List<File> getFiles()
  {
    Object[]   objects = fileList.getSelectedValues();
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

  public boolean isWasOk()
  {
    return wasOk;
  }

  // --------------------------- main() method ---------------------------

  public static void main(String[] args)
  {
    FileDialog dialog = new FileDialog(new File("."));

    dialog.setVisible(true);
    System.exit(0);
  }
}
