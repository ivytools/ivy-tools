package com.nurflugel;

/** Representation of a branch.  Note that a branch can only have one tag made from it. */
public class Branch
{
    private String name;
    private String tagName;

    public Branch(String branchName)
    {
        name = branchName;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getTagName()
    {
        return tagName;
    }

    public void setTagName(String tagName)
    {
        this.tagName = tagName;
    }

    public String toString()
    {
        return name;
    }

    /** Gets the path of the branch relative to the reposlitory root. */
    public String getPath()
    {

        if (name.equals("trunk"))
        {
            return name;
        }
        else
        {
            return "branches/" + name;
        }
    }
}
