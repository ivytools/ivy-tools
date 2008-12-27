package com.nurflugel.externalsreporter.ui.tree;


import com.nurflugel.BuildableProjects;
import com.nurflugel.Branch;

import java.util.ArrayList;
import java.util.List;


/** Representation of a branch, which can contain targets. */
public class BranchNode extends CheckableNode
{
    private BuildableProjects project;
    private boolean showTargets;
    private List<TargetNode> targets = new ArrayList<TargetNode>();
    private Branch branch;

    public BranchNode(BuildableProjects project, Branch branch, boolean showTargets)
    {
        super(branch.getName());
        this.branch = branch;
        this.project = project;
        this.showTargets = showTargets;
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

    @Override
    public void add(CheckableNode targetNode)
    {

        if (targetNode instanceof TargetNode)
        {
            targets.add(((TargetNode) targetNode));
        }
    }

    @Override
    public List<? extends CheckableNode> getChildren()
    {
        return targets;
    }

    public List<TargetNode> getTargets()
    {
        return targets;
    }

    public String getBranchUrl()
    {
        String url = project.getProjectBaseUrl();
        url = url + "/" + getName();

        return url;

    }

    public Branch getBranch()
    {
        return branch;
    }

    public BuildableProjects getProject()
    {
        return project;
    }
}
