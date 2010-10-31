package com.nurflugel.externalsreporter.ui.tree;

import com.nurflugel.BuildableItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jun 20, 2008 Time: 4:53:51 PM To change this template use File | Settings | File Templates.
 */
public class TargetNode extends CheckableNode
{
  private BuildableItem item;

  public TargetNode(BuildableItem item)
  {
    super(item.getBuildTarget().getTargetName());
    this.item = item;
  }

  // -------------------------- OTHER METHODS --------------------------
  @Override
  public void add(CheckableNode node)
  {
    throw new IllegalArgumentException("Cant add a child to a target");
  }

  public BuildableItem getBuildableItem()
  {
    return item;
  }

  @Override
  public List<? extends CheckableNode> getChildren()
  {
    return new ArrayList<TargetNode>();
  }

  @Override
  public boolean isLeaf()
  {
    return true;
  }
}
