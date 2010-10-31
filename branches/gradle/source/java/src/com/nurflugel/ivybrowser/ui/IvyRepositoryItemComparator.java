package com.nurflugel.ivybrowser.ui;

import com.nurflugel.ivybrowser.domain.IvyRepositoryItem;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jan 25, 2008 Time: 7:19:58 PM To change this template use File | Settings | File Templates.
 */
public class IvyRepositoryItemComparator implements Comparator, Serializable
{
  private static final long serialVersionUID = -8090786516649397033L;

  // ------------------------ INTERFACE METHODS ------------------------
  // --------------------- Interface Comparator ---------------------
  @Override
  public int compare(Object o1, Object o2)
  {
    IvyRepositoryItem itemA   = (IvyRepositoryItem) o1;
    IvyRepositoryItem itemB   = (IvyRepositoryItem) o2;
    String            moduleA = itemA.getModule();
    String            moduleB = itemB.getModule();

    return moduleA.compareTo(moduleB);
  }
}
