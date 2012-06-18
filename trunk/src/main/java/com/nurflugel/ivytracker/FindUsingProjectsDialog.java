package com.nurflugel.ivytracker;

import ca.odell.glazedlists.EventList;
import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivytracker.domain.Project;
import javax.swing.*;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;
import static com.nurflugel.common.ui.Util.centerApp;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
import static javax.swing.KeyStroke.getKeyStroke;

public class FindUsingProjectsDialog extends JDialog
{
  /** Use serialVersionUID for interoperability. */
  private static final long serialVersionUID = -1713437647264279958L;
  private JPanel            contentPane;
  private JButton           buttonOK;
  private JTree             projectsTree;

  public FindUsingProjectsDialog(IvyPackage ivyFile, Map<Project, List<IvyPackage>> ivyFilesMap, EventList<IvyPackage> ivyRepositoryList)
  {
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);
    addListeners();

    // determineDependencies(ivyFile, ivyFilesMap, ivyRepositoryList);
    populateTree(ivyFile, ivyFilesMap, ivyRepositoryList);
    pack();
    setSize(600, 600);
    centerApp(this);
  }

  // private void determineDependencies(IvyPackage ivyFile, Map<Project, List<IvyPackage>> ivyFilesMap, EventList<IvyPackage> ivyRepositoryList) {}
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

  private void populateTree(IvyPackage ivyFile, Map<Project, List<IvyPackage>> ivyFilesMap, List<IvyPackage> ivyRepositoryList)
  {
    TreeModel treeModel = new FindUsingProjectsTreeModel(ivyFile, ivyFilesMap, ivyRepositoryList);

    projectsTree.setModel(treeModel);
  }
}
