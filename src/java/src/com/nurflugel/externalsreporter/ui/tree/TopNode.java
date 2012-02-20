package com.nurflugel.externalsreporter.ui.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jun 20, 2008 Time: 6:17:21 PM To change this template use File | Settings | File Templates.
 */
public class TopNode extends CheckableNode
{
  private List<ProjectNode> projects = new ArrayList<ProjectNode>();

  public TopNode(String s)
  {
    super(s);
  }

  // -------------------------- OTHER METHODS --------------------------
  @Override
  public void add(CheckableNode node)
  {
    if (node instanceof ProjectNode)
    {
      projects.add(((ProjectNode) node));
    }
  }

  @Override
  public List<? extends CheckableNode> getChildren()
  {
    return projects;
  }

  @Override
  public int indexOf(Object child)
  {
    if (child instanceof ProjectNode)
    {
      return projects.indexOf(child);
    }

    return -1;
  }
}
