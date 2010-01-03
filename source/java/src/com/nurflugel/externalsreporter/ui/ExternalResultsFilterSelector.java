package com.nurflugel.externalsreporter.ui;

import com.nurflugel.common.ui.Util;
import com.nurflugel.ivybrowser.ui.CheckboxCellRenderer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ExternalResultsFilterSelector extends JDialog
{
  // todo use glazed lists to be able to filter results for manipulation
  private JPanel     contentPane;
  private JButton    buttonOK;
  private JButton    buttonCancel;
  private JSplitPane splitPane;
  private JPanel     projectPanel;
  private JPanel     externalsPanel;
  private JCheckBox  projectSelectAllCheckBox;
  private JCheckBox  externalsSelectAllCheckbox;
  private JCheckBox  noTagsCheckBox;

  public ExternalResultsFilterSelector(List<External> externalList)
  {
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);

    addListeners();
    setTitle("Select externals and/or projects to filter results");
    addExternals(externalList);

    BoxLayout externalsLayout = new BoxLayout(externalsPanel, BoxLayout.Y_AXIS);

    externalsPanel.setLayout(externalsLayout);

    BoxLayout projectsLayout = new BoxLayout(projectPanel, BoxLayout.Y_AXIS);

    projectPanel.setLayout(projectsLayout);

    setSize(1200, 500);
    Util.center(this);
    splitPane.setDividerLocation(0.5d);
    setVisible(true);
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
      }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    externalsSelectAllCheckbox.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          selectAllCheckboxes(externalsSelectAllCheckbox.isSelected(), externalsPanel);
        }
      });
    projectSelectAllCheckBox.addActionListener(new ActionListener()
      {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          selectAllCheckboxes(projectSelectAllCheckBox.isSelected(), projectPanel);
        }
      });
  }

  /** Go through all the checkboxes in the specified panel and set them to the desired state. */
  private void selectAllCheckboxes(boolean selected, JPanel panel)
  {
    Component[] components = panel.getComponents();

    for (Component component : components)
    {
      if (component instanceof JCheckBox)
      {
        ((AbstractButton) component).setSelected(selected);
      }
    }
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

  private void addExternals(List<External> externalList)
  {
    Set<String> externalsSet = new TreeSet<String>();
    Set<String> projectsSet  = new TreeSet<String>();

    for (External external : externalList)
    {
      externalsSet.add(external.getUrl());
      projectsSet.add(external.getProjectBaseUrl());
    }

    for (String external : externalsSet)
    {
      externalsPanel.add(new JCheckBox(external));
    }

    for (String project : projectsSet)
    {
      projectPanel.add(new JCheckBox(project));
    }
  }

  // public static void main(String[] args)
  // {
  // ExternalResultsFilterSelector dialog = new ExternalResultsFilterSelector(externalList);
  //
  // System.exit(0);
  // }

    public List<External> getExternalsList() {
        return null;
//        return externals
    }
}
