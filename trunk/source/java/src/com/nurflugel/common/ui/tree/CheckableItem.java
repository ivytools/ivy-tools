package com.nurflugel.common.ui.tree;

import com.nurflugel.Branch;
import com.nurflugel.BuildableProjects;

/** Abstraction of an item that can be checked. */
public interface CheckableItem
{
  Branch getBranch();
  BuildableProjects getProject();
  boolean isSelected();
  void setSelected(boolean selected);
}
