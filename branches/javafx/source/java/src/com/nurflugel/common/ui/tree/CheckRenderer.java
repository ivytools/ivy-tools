package com.nurflugel.common.ui.tree;

import com.nurflugel.externalsreporter.ui.tree.CheckableNode;
import com.nurflugel.externalsreporter.ui.tree.ProjectNode;
import com.nurflugel.externalsreporter.ui.tree.TopNode;
import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/** Renderer for the tree nodes. */
public class CheckRenderer extends JPanel implements TreeCellRenderer
{
  private static final long serialVersionUID      = 4777763047200461024L;
  private JCheckBox         check                 = new JCheckBox();
  private TreeLabel         label                 = new TreeLabel();
  private boolean           showProjectCheckboxes;

  public CheckRenderer(boolean showProjectCheckboxes)
  {
    this.showProjectCheckboxes = showProjectCheckboxes;
    setLayout(null);
    add(check);
    add(label);
    // check.setBackground(UIManager.getColor("Tree.textBackground"));
    // label.setForeground(UIManager.getColor("Tree.textForeground"));
  }

  // ------------------------ INTERFACE METHODS ------------------------
  // --------------------- Interface TreeCellRenderer ---------------------
  @Override
  @SuppressWarnings({ "ReturnOfThis" })
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row,
                                                boolean hasFocus)
  {
    String stringValue = tree.convertValueToText(value, isSelected, expanded, leaf, row, hasFocus);

    setEnabled(tree.isEnabled());
    check.setSelected(((CheckableNode) value).isSelected());
    label.setFont(tree.getFont());
    label.setText(stringValue);
    label.setSelected(isSelected);
    label.setFocus(hasFocus);

    if (leaf)
    {
      check.setVisible(true);
      label.setIcon(null);
    }
    else if (expanded)
    {
      check.setVisible(true);
      label.setIcon(UIManager.getIcon("Tree.openIcon"));
    }
    else
    {
      check.setVisible(true);
      label.setIcon(UIManager.getIcon("Tree.closedIcon"));
    }

    if ((value instanceof TopNode) || ((value instanceof ProjectNode) && !showProjectCheckboxes))
    {
      check.setVisible(false);
    }

    return this;
  }

  // -------------------------- OTHER METHODS --------------------------
  @Override
  public void doLayout()
  {
    Dimension checkboxDimension = getCheckboxDimension();
    Dimension labelDimension    = label.getPreferredSize();
    int       checkboxHeight    = 0;
    int       labelHeight       = 0;

    if (checkboxDimension.height < labelDimension.height)
    {
      checkboxHeight = (labelDimension.height - checkboxDimension.height) / 2;
    }
    else
    {
      labelHeight = (checkboxDimension.height - labelDimension.height) / 2;
    }

    check.setLocation(0, checkboxHeight);
    check.setBounds(0, checkboxHeight, checkboxDimension.width, checkboxDimension.height);
    label.setLocation(checkboxDimension.width, labelHeight);
    label.setBounds(checkboxDimension.width, labelHeight, labelDimension.width, labelDimension.height);
    invalidate();
  }

  @Override
  public Dimension getPreferredSize()
  {
    Dimension checkboxDimension = getCheckboxDimension();
    Dimension labelDimension    = label.getPreferredSize();

    return new Dimension(checkboxDimension.width + labelDimension.width,
                         ((checkboxDimension.height < labelDimension.height) ? labelDimension.height
                                                                             : checkboxDimension.height));
  }

  private Dimension getCheckboxDimension()
  {
    return check.isVisible() ? check.getPreferredSize()
                             : new Dimension(0, 0);
  }

  // @Override
  // public void setBackground(Color color)
  // {
  //
  // if (color instanceof ColorUIResource) {
  // color = null;
  // }
  //
  // super.setBackground(color);
  // }
}
