package com.nurflugel.common.ui.tree;

import com.nurflugel.externalsreporter.ui.tree.CheckableNode;
import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/** Listener for the tree nodes. */
public class NodeSelectionListener extends MouseAdapter
{
  private JTree tree;

  public NodeSelectionListener(JTree tree)
  {
    this.tree = tree;
  }
  // ------------------------ INTERFACE METHODS ------------------------
  // --------------------- Interface MouseListener ---------------------

  /** If this node has children, and it's checked, check all the children, too. Or uncheck them, if the node is unchecked. */
  @Override
  public void mouseClicked(MouseEvent e)
  {
    int      x    = e.getX();
    int      y    = e.getY();
    int      row  = tree.getRowForLocation(x, y);
    TreePath path = tree.getPathForRow(row);

    // TreePath  path = tree.getSelectionPath();
    if (path != null)
    {
      CheckableNode node       = (CheckableNode) path.getLastPathComponent();
      boolean       isSelected = !(node.isSelected());

      setAllChildren(node, isSelected);

      // if (node.getSelectionMode() == CheckNode.DIG_IN_SELECTION) {
      //
      // if (isSelected) {
      // tree.expandPath(path);
      // } else {
      // tree.collapsePath(path);
      // }
      // }
      tree.revalidate();
      tree.repaint();
      // ((ExternalsTreeModel) tree.getModel()).nodeChanged(node);
      //
      // // I need revalidate if node is root.  but why?
      // if (row == 0) {
      // tree.revalidate();
      // tree.repaint();
      // }
    }
  }

  // -------------------------- OTHER METHODS --------------------------
  private void setAllChildren(CheckableNode node, boolean selected)
  {
    node.setSelected(selected);

    List<? extends CheckableNode> children = node.getChildren();

    for (CheckableNode child : children)
    {
      setAllChildren(child, selected);
    }
  }
}
