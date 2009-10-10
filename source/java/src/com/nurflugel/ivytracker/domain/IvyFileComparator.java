package com.nurflugel.ivytracker.domain;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jan 27, 2008 Time: 3:18:31 PM To change this template use File | Settings | File Templates.
 */
public class IvyFileComparator implements Comparator, Serializable
{
  private static final long serialVersionUID = -1887579627945133474L;

  // ------------------------ INTERFACE METHODS ------------------------

  // --------------------- Interface Comparator ---------------------

  public int compare(Object o1, Object o2)
  {
    IvyFile itemA   = (IvyFile) o1;
    IvyFile itemB   = (IvyFile) o2;
    String  moduleA = itemA.getOrg() + itemA.getModule() + itemA.getVersion();
    String  moduleB = itemB.getOrg() + itemA.getModule() + itemA.getVersion();

    return moduleA.compareTo(moduleB);
  }
}
