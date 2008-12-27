package com.nike.common.ui.tree;

import com.nike.Branch;
import com.nike.BuildableProjects;


/** Abstraction of an item that can be checked. */
public interface CheckableItem
{
    Branch getBranch();

    BuildableProjects getProject();

    boolean isSelected();

    void setSelected(boolean selected);
}
