package com.nurflugel;

import org.apache.commons.lang.StringUtils;

/** Representation of a branch. Note that a branch can only have one tag made from it. */
public class Branch
{
  private String name;
  private String tagName;

  public static void main(String[] args)
  {
    String name       = "Douglas Gary Bullard";
    String middleName = " Gary ";
    int    index      = name.indexOf(middleName);
    String firstName  = name.substring(0, index);
    String lastName   = name.substring(index + middleName.length(), name.length());

    System.out.println("firstName = " + firstName);
    System.out.println("lastName = " + lastName);
    firstName = StringUtils.substringBefore(name, middleName);
    lastName  = StringUtils.substringAfter(name, middleName);
    System.out.println("firstName = " + firstName);
    System.out.println("lastName = " + lastName);
  }

  public Branch(String branchName)
  {
    name = branchName;
  }
  // -------------------------- OTHER METHODS --------------------------

  /** Gets the path of the branch relative to the reposlitory root. */
  public String getPath()
  {
    return name.equals("trunk") ? name
                                : ("branches/" + name);
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
}
