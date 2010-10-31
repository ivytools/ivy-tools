package com.nurflugel.ivytracker.domain;

import com.nurflugel.ivybrowser.domain.IvyPackage;
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
  @Override
  public int compare(Object o1, Object o2)
  {
    IvyPackage itemA   = (IvyPackage) o1;
    IvyPackage itemB   = (IvyPackage) o2;
    String     moduleA = itemA.getOrgName() + itemA.getModuleName() + itemA.getVersion();
    String     moduleB = itemB.getOrgName() + itemA.getModuleName() + itemA.getVersion();

    return moduleA.compareTo(moduleB);
  }
}
