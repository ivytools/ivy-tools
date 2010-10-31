package com.nurflugel.externalsreporter.ui.tree;

import com.nurflugel.Branch;
import com.nurflugel.BuildableProjects;
import java.util.ArrayList;
import java.util.List;

/** Representation of a branch, which can contain targets. */
public class BranchNode extends CheckableNode
{
  private BuildableProjects project;
  private boolean           showTargets;
  private List<TargetNode>  targets = new ArrayList<TargetNode>();
  private Branch            branch;

  public BranchNode(BuildableProjects project, Branch branch, boolean showTargets)
  {
    super(branch.getName());
    this.branch      = branch;
    this.project     = project;
    this.showTargets = showTargets;
  }

  // -------------------------- OTHER METHODS --------------------------
  @Override
  public void add(CheckableNode targetNode)
  {
    if (targetNode instanceof TargetNode)
    {
      targets.add(((TargetNode) targetNode));
    }
  }

  public String getBranchUrl()
  {
    String url = project.getProjectBaseUrl();

    url = url + "/" + getName();

    return url;
  }

  @Override
  public List<? extends CheckableNode> getChildren()
  {
    return targets;
  }

  @Override
  public int indexOf(Object child)
  {
    int index = 0;

    for (TargetNode target : targets)
    {
      if (target.equals(child))
      {
        return index;
      }

      index++;
    }

    return -1;
  }

  @Override
  public boolean isLeaf()
  {
    return !showTargets;
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public Branch getBranch()
  {
    return branch;
  }

  public BuildableProjects getProject()
  {
    return project;
  }

  public List<TargetNode> getTargets()
  {
    return targets;
  }
}
