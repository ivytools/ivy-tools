package com.nurflugel.ivytracker.domain;

import java.io.Serializable;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jan 27, 2008 Time: 3:18:31 PM To change this template use File | Settings | File Templates.
 */
public class ProjectComparator implements Comparator, Serializable
{
  private static final long serialVersionUID = -1887579627945133474L;

  // ------------------------ INTERFACE METHODS ------------------------
  // --------------------- Interface Comparator ---------------------
  @Override
  public int compare(Object o1, Object o2)
  {
    Project itemA = (Project) o1;
    Project itemB = (Project) o2;

    return itemA.getProjectUrl().compareTo(itemB.getProjectUrl());
  }
}
