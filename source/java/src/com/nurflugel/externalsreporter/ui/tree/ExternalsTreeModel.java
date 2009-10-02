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

  /** Adds a listener for the TreeModelEvent posted after the tree changes. */
  public void addTreeModelListener(TreeModelListener treeModelListener)
  {
    treeModelListeners.add(treeModelListener);
  }

  /** Returns the child of parent at index index in the parent's child array. */
  public Object getChild(Object parent, int index)
  {
    return ((CheckableNode) parent).getChildren().get(index);
  }

  /** Returns the number of children of parent. */
  public int getChildCount(Object parent)
  {
    return ((CheckableNode) parent).getChildren().size();
  }

  /** Returns the index of child in parent. */
  public int getIndexOfChild(Object parent, Object child)
  {
    return ((CheckableNode) parent).indexOf(child);
  }

  /** Returns true if node is a leaf. */
  public boolean isLeaf(Object node)
  {
    return ((CheckableNode) node).isLeaf();
  }

  /** Removes a listener previously added with addTreeModelListener. */
  public void removeTreeModelListener(TreeModelListener treeModelListener)
  {
    treeModelListeners.remove(treeModelListener);
  }

  /** Messaged when the user has altered the value for the item identified by path to newValue. */
  public void valueForPathChanged(TreePath treePath, Object o) {}

  public void addItem(ProjectNode item)
  {
    root.add(item);
    fireTreeStructureChanged(item);
  }

  public void addItems(List<ProjectNode> list)
  {
    for (ProjectNode projectNode : list)
    {
      root.add(projectNode);
    }
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

  /** Returns the root of the tree. */
  public Object getRoot()
  {
    return root;
  }

  public void addItem(CheckableNode currentNode, CheckableNode node)
  {
    currentNode.add(node);
    fireTreeStructureChanged(node);
  }
}
