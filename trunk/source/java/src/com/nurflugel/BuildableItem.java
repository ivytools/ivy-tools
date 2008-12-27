package com.nurflugel;

import com.nurflugel.common.ui.tree.CheckableItem;


/** Representation of a buildable item - a target of a branch of a project. */
public class BuildableItem implements CheckableItem
{
    private BuildableProjects project;
    private Targets buildTarget;
    private boolean selected;
    private Branch branch;
    private String projectAbr;

    // --------------------------- CONSTRUCTORS ---------------------------

    public BuildableItem(BuildableProjects project, Targets buildTarget, Branch branch)
    {
        this.project = project;
        this.projectAbr = project.getProjectAbr();
        this.buildTarget = buildTarget;
        this.branch = branch;
    }

    // -------------------------- OTHER METHODS --------------------------

    public Branch getBranch()
    {
        return branch;
    }


    public String getProjectAbr()
    {
        return this.projectAbr;
    }

    public Targets getBuildTarget()
    {
        return buildTarget;
    }

    public BuildableProjects getProject()
    {
        return project;
    }

    public boolean isSelected()
    {
        return selected;
    }

    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

    @Override
    public String toString()
    {
        return buildTarget.toString();
    }

}
