package com.nurflugel.ivytracker.domain;

import ca.odell.glazedlists.TextFilterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jan 25, 2008 Time: 10:36:57 PM To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({ "unchecked" })
public class ProjectFileFilterator implements TextFilterator
{
  // ------------------------ INTERFACE METHODS ------------------------

  // --------------------- Interface TextFilterator ---------------------
  @Override
  public void getFilterStrings(List list, Object o)
  {
    Project item = (Project) o;

    list.add(item.getProjectUrl());
  }
}
