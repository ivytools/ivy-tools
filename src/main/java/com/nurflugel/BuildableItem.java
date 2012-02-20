package com.nurflugel;

import com.nurflugel.common.ui.tree.CheckableItem;

/** Representation of a buildable item - a target of a branch of a project. */
public class BuildableItem implements CheckableItem
{
  private BuildableProjects project;
  private Targets           buildTarget;
  private boolean           selected;
  private Branch            branch;
  private String            projectAbr;

  // --------------------------- CONSTRUCTORS ---------------------------
  public BuildableItem(BuildableProjects project, Targets buildTarget, Branch branch)
  {
    this.project     = project;
    this.projectAbr  = project.getProjectAbr();
    this.buildTarget = buildTarget;
    this.branch      = branch;
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
  public String toString()
  {
    return buildTarget.toString();
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public Targets getBuildTarget()
  {
    return buildTarget;
  }

  public String getProjectAbr()
  {
    return this.projectAbr;
  }
}
