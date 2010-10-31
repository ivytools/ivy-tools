package com.nurflugel.externalsreporter.ui.tree;

import com.nurflugel.common.ui.tree.CheckRenderer;
import com.nurflugel.common.ui.tree.NodeSelectionListener;
import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Handler to go through all the projects and find externals for them. */
@SuppressWarnings({ "UseOfSystemOutOrSystemErr" })
public class ExternalTreeHandler
{
  @SuppressWarnings({ "CollectionDeclaredAsConcreteClass", "UseOfObsoleteCollectionType" })
  private JTree              tree;
  private ExternalsTreeModel treeModel;

  public ExternalTreeHandler(boolean showProjectCheckboxes)
  {
    treeModel = new ExternalsTreeModel();
    tree      = new JTree(treeModel);
    tree.setBackground(new Color(230, 230, 230));
    tree.setCellRenderer(new CheckRenderer(showProjectCheckboxes));
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.putClientProperty("JTree.lineStyle", "Angled");
    tree.addMouseListener(new NodeSelectionListener(tree));
  }

  // -------------------------- OTHER METHODS --------------------------
  public void addItem(ProjectNode item)
  {
    treeModel.addItem(item);
  }

  public void addItem(CheckableNode currentNode, CheckableNode node)
  {
    treeModel.addItem(currentNode, node);
  }

  /**
   * If expand is true, expands all nodes in the tree. Otherwise, collapses all nodes in the tree. In all cases, leave the first node (root) expanded
   */
  public void expandAll(boolean expand)
  {
    Object   root     = treeModel.getRoot();
    TreePath treePath = new TreePath(root);

    // Traverse tree from rootNode
    expandAll(treePath, expand);
    tree.expandRow(0);
  }

  private void expandAll(TreePath parent, boolean expand)
  {
    // Traverse children
    Object                        lastComponent = parent.getLastPathComponent();
    CheckableNode                 node          = (CheckableNode) lastComponent;
    List<? extends CheckableNode> childern      = node.getChildren();

    if (childern.size() >= 0)
    {
      for (CheckableNode child : childern)
      {
        TreePath path = parent.pathByAddingChild(child);

        expandAll(path, expand);
      }
    }

    // Expansion or collapse must be done bottom-up
    if (expand)
    {
      tree.expandPath(parent);
    }
    else
    {
      tree.collapsePath(parent);
    }
  }

  public List<BranchNode> getCheckedBranches()
  {
    return getBranches(true);
  }

  private List<BranchNode> getBranches(boolean isChecked)
  {
    List<BranchNode>  branchNodes = new ArrayList<BranchNode>();
    List<ProjectNode> projects    = getProjects(false);

    for (ProjectNode project : projects)
    {
      List<BranchNode> list = project.getBranches();

      for (BranchNode branchNode : list)
      {
        if (!isChecked || branchNode.isSelected())
        {
          branchNodes.add(branchNode);
        }
      }
    }

    return branchNodes;
  }

  public List<ProjectNode> getCheckedProjects()
  {
    return getProjects(true);
  }

  private List<ProjectNode> getProjects(boolean isChecked)
  {
    List<ProjectNode> projects = new ArrayList<ProjectNode>();
    TopNode           root     = (TopNode) treeModel.getRoot();

    recursiveGetCheckedProjects(projects, root, isChecked);

    return projects;
  }

  private void recursiveGetCheckedProjects(List<ProjectNode> projects, CheckableNode root, boolean isChecked)
  {
    if ((!isChecked || root.isSelected()) && (root instanceof ProjectNode))
    {
      projects.add(((ProjectNode) root));
    }

    List<? extends CheckableNode> children = root.getChildren();

    for (CheckableNode child : children)
    {
      recursiveGetCheckedProjects(projects, child, isChecked);
    }
  }

  public List<TargetNode> getCheckedTargets()
  {
    return getTargets(true);
  }

  private List<TargetNode> getTargets(boolean isChecked)
  {
    List<TargetNode> targetNodes = new ArrayList<TargetNode>();
    List<BranchNode> branchNodes = getBranches(false);

    for (BranchNode branchNode : branchNodes)
    {
      List<TargetNode> targets = branchNode.getTargets();

      for (TargetNode target : targets)
      {
        if (!isChecked || target.isSelected())
        {
          targetNodes.add(target);
        }
      }
    }

    return targetNodes;
  }

  public CheckableNode getTopNode()
  {
    return (CheckableNode) treeModel.getRoot();
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public JTree getTree()
  {
    return tree;
  }
}
