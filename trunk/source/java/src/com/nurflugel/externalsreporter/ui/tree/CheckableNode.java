package com.nurflugel.externalsreporter.ui.tree;

import java.util.List;


/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jun 20, 2008 Time: 4:55:12 PM To change this template use File | Settings | File
 * Templates.
 */
public abstract class CheckableNode
{
    private boolean isSelected = false;
    private String name;

    protected CheckableNode(String name)
    {
        this.name = name;
    }

    public abstract List<? extends CheckableNode> getChildren();

    public int indexOf(Object child)
    {
        return -1;
    }

    public String getName()
    {
        return name;
    }

    public boolean isLeaf()
    {
        return false;
    }

    public boolean isSelected()
    {
        return isSelected;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public abstract void add(CheckableNode node);

    public void setSelected(boolean selected)
    {
        isSelected = selected;
    }
}
