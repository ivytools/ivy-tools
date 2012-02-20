package com.nurflugel.ivybrowser.ui;

import ca.odell.glazedlists.TextFilterator;
import com.nurflugel.ivybrowser.domain.IvyPackage;
import java.util.List;

/** This tells GlazedLists what fields to filter by when the user types into the text box. */
@SuppressWarnings({ "unchecked" })
public class IvyPackageFilterator implements TextFilterator
{
  // ------------------------ INTERFACE METHODS ------------------------

  // --------------------- Interface TextFilterator ---------------------
  @Override
  public void getFilterStrings(List list, Object o)
  {
    IvyPackage item = (IvyPackage) o;

    list.add(item.getModuleName());
    list.add(item.getVersion());

    // list.add(item.getLibrary());
    list.add(item.getOrgName());
  }
}
