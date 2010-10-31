package com.nurflugel.ivytracker;

import com.nurflugel.ivybrowser.domain.IvyKey;
import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivytracker.domain.Project;
import javax.swing.*;
import javax.swing.tree.TreeModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;
import static com.nurflugel.common.ui.Util.center;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
import static javax.swing.KeyStroke.getKeyStroke;

public class ShowProjectsDialog extends JDialog
{
  /** Use serialVersionUID for interoperability. */
  private static final long serialVersionUID = -4630003093188980293L;
  private JPanel            contentPane;
  private JButton           buttonOK;
  private JTree             projectsTree;

  public ShowProjectsDialog(List<IvyPackage> projectIvyFiles, Map<Project, List<IvyPackage>> ivyFilesMap)
  {
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);
    addListeners();

    // populateTree(projectIvyFiles, ivyFilesMap);//todo
    pack();
    setSize(1000, 800);
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
    // add your code here
    dispose();
  }

  private void onCancel()
  {
    // add your code here if necessary
    dispose();
  }

  // todo start from selected Ivy files, show projects
  private void populateTree(List<IvyPackage> projectIvyFiles, Map<IvyKey, IvyPackage> ivyFilesMap)
  {
    TreeModel treeModel = new IvyProjectTreeModel(projectIvyFiles, ivyFilesMap);

    projectsTree.setModel(treeModel);
  }
}
