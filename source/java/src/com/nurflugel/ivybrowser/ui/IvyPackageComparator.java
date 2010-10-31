package com.nurflugel.ivybrowser.ui;

import com.nurflugel.ivybrowser.domain.IvyPackage;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jan 27, 2008 Time: 3:18:31 PM To change this template use File | Settings | File Templates.
 */
public class IvyPackageComparator implements Comparator<IvyPackage>, Serializable
{
  private static final long serialVersionUID = -1887579627945133474L;

  // ------------------------ INTERFACE METHODS ------------------------
  // --------------------- Interface Comparator ---------------------
  @Override
  public int compare(IvyPackage itemA, IvyPackage itemB)
  {
    String moduleA = itemA.getOrgName() + itemA.getModuleName() + itemA.getVersion();
    String moduleB = itemB.getOrgName() + itemA.getModuleName() + itemA.getVersion();

    return moduleA.compareTo(moduleB);
  }
}
