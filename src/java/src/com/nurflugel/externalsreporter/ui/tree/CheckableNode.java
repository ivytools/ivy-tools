package com.nurflugel.externalsreporter.ui.tree;

import java.util.List;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jun 20, 2008 Time: 4:55:12 PM To change this template use File | Settings | File Templates.
 */
public abstract class CheckableNode
{
  private boolean isSelected = false;
  private String  name;

  protected CheckableNode(String name)
  {
    this.name = name;
  }

  // -------------------------- OTHER METHODS --------------------------
  public abstract void add(CheckableNode node);
  public abstract List<? extends CheckableNode> getChildren();

  public int indexOf(Object child)
  {
    return -1;
  }

  public boolean isLeaf()
  {
    return false;
  }

  public boolean isSelected()
  {
    return isSelected;
  }

  // ------------------------ CANONICAL METHODS ------------------------
  @Override
  public String toString()
  {
    return name;
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public String getName()
  {
    return name;
  }

  public void setSelected(boolean selected)
  {
    isSelected = selected;
  }
}
