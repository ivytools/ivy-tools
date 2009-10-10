package com.nurflugel.ivytracker;

import com.nurflugel.ivybrowser.ui.BuilderMainFrame;
import com.nurflugel.ivytracker.domain.IvyFile;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.tree.TreeModel;

public class ShowProjectsDialog extends JDialog
{
  /** Use serialVersionUID for interoperability. */
  private static final long serialVersionUID = -4630003093188980293L;
  private JPanel            contentPane;
  private JButton           buttonOK;
  private JTree             projectsTree;

  public ShowProjectsDialog(List<IvyFile> projectIvyFiles, Map<String, IvyFile> ivyFilesMap)
  {
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);
    addListeners();
    populateTree(projectIvyFiles, ivyFilesMap);
    pack();
    setSize(1000, 800);
    BuilderMainFrame.centerApp(this);
  }

  private void populateTree(List<IvyFile> projectIvyFiles, Map<String, IvyFile> ivyFilesMap)
  {
    TreeModel treeModel = new IvyProjectTreeModel(projectIvyFiles, ivyFilesMap);

    projectsTree.setModel(treeModel);
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

  public static void main(String[] args)
  {
    ShowProjectsDialog dialog = new ShowProjectsDialog(new ArrayList<IvyFile>(), new HashMap<String, IvyFile>());

    dialog.pack();
    dialog.setVisible(true);
    System.exit(0);
  }
}
