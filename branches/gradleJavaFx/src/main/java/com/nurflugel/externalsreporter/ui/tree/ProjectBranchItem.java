package com.nurflugel.externalsreporter.ui.tree;

import com.nurflugel.Branch;
import com.nurflugel.BuildableProjects;
import com.nurflugel.common.ui.tree.CheckableItem;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: May 31, 2008 Time: 3:20:22 PM To change this template use File | Settings | File Templates.
 */
public class ProjectBranchItem implements CheckableItem
{
  private BuildableProjects project;
  private boolean           selected;
  private Branch            branch;

  // --------------------------- CONSTRUCTORS ---------------------------
  public ProjectBranchItem(BuildableProjects project, Branch branch)
  {
    this.project = project;
    this.branch  = branch;
  }

  // ------------------------ INTERFACE METHODS ------------------------
  // --------------------- Interface CheckableItem ---------------------
  @Override
  public Branch getBranch()
  {
    return branch;
  }

  @Override
  public BuildableProjects getProject()
  {
    return project;
  }

  @Override
  public boolean isSelected()
  {
    return selected;
  }

  @Override
  public void setSelected(boolean selected)
  {
    this.selected = selected;
  }

  // ------------------------ CANONICAL METHODS ------------------------
  @Override
  @SuppressWarnings({ "AccessingNonPublicFieldOfAnotherObject" })
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }

    if ((o == null) || (getClass() != o.getClass()))
    {
      return false;
    }

    ProjectBranchItem that = (ProjectBranchItem) o;

    if ((branch != null) ? (!branch.equals(that.branch))
                         : (that.branch != null))
    {
      return false;
    }

    return project == that.project;
  }

  @Override
  public int hashCode()
  {
    int result = ((project != null) ? project.hashCode()
                                    : 0);

    result = (31 * result) + ((branch != null) ? branch.hashCode()
                                               : 0);

    return result;
  }

  @Override
  public String toString()
  {
    return branch.getName();
  }
}
