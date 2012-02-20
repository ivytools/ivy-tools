package com.nurflugel.externalsreporter.ui.tree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jun 20, 2008 Time: 8:59:07 AM To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({ "ChainOfInstanceofChecks", "UseOfSystemOutOrSystemErr" })
public class ExternalsTreeModel implements TreeModel
{
  private TopNode                 root;
  private List<TreeModelListener> treeModelListeners = new ArrayList<TreeModelListener>();

  public ExternalsTreeModel()
  {
    root = new TopNode("Available Projects");
  }
  // ------------------------ INTERFACE METHODS ------------------------
  // --------------------- Interface TreeModel ---------------------

  /** Returns the root of the tree. */
  @Override
  public Object getRoot()
  {
    return root;
  }

  /** Returns the child of parent at index index in the parent's child array. */
  @Override
  public Object getChild(Object parent, int index)
  {
    return ((CheckableNode) parent).getChildren().get(index);
  }

  /** Returns the number of children of parent. */
  @Override
  public int getChildCount(Object parent)
  {
    return ((CheckableNode) parent).getChildren().size();
  }

  /** Returns true if node is a leaf. */
  @Override
  public boolean isLeaf(Object node)
  {
    return ((CheckableNode) node).isLeaf();
  }

  /** Messaged when the user has altered the value for the item identified by path to newValue. */
  @Override
  public void valueForPathChanged(TreePath treePath, Object o) {}

  /** Returns the index of child in parent. */
  @Override
  public int getIndexOfChild(Object parent, Object child)
  {
    return ((CheckableNode) parent).indexOf(child);
  }

  /** Adds a listener for the TreeModelEvent posted after the tree changes. */
  @Override
  public void addTreeModelListener(TreeModelListener treeModelListener)
  {
    treeModelListeners.add(treeModelListener);
  }

  /** Removes a listener previously added with addTreeModelListener. */
  @Override
  public void removeTreeModelListener(TreeModelListener treeModelListener)
  {
    treeModelListeners.remove(treeModelListener);
  }

  // -------------------------- OTHER METHODS --------------------------
  public void addItem(ProjectNode item)
  {
    root.add(item);
    fireTreeStructureChanged(item);
  }

  /** The only event raised by this model is TreeStructureChanged with the root as path, i.e. the whole tree has changed. */
  protected void fireTreeStructureChanged(Object node)
  {
    TreeModelEvent e = new TreeModelEvent(this, new Object[] { node });

    for (TreeModelListener tml : treeModelListeners)
    {
      tml.treeStructureChanged(e);
    }
  }

  public void addItem(CheckableNode currentNode, CheckableNode node)
  {
    currentNode.add(node);
    fireTreeStructureChanged(node);
  }

  public void addItems(List<ProjectNode> list)
  {
    for (ProjectNode projectNode : list)
    {
      root.add(projectNode);
    }
  }
}
