package com.nurflugel.ivybrowser.ui;

import ca.odell.glazedlists.gui.TableFormat;

import com.nurflugel.ivybrowser.domain.IvyRepositoryItem;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jan 25, 2008 Time: 7:12:02 PM To change this template use File | Settings | File Templates.
 */
public class IvyRepositoryItemTableFormat implements TableFormat
{
  // ------------------------ INTERFACE METHODS ------------------------

  // --------------------- Interface TableFormat ---------------------
  @Override
  public int getColumnCount()
  {
    return 3;
  }

  @Override
  public String getColumnName(int i)
  {
    switch (i)
    {
      case 0:
        return "Org";

      case 1:
        return "Module";

      case 2:
        return "Rev";

      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public Object getColumnValue(Object o, int i)
  {
    IvyRepositoryItem dependency = (IvyRepositoryItem) o;

    switch (i)
    {
      case 0:
        return dependency.getOrg();

      case 1:
        return dependency.getModule();

      case 2:
        return dependency.getRev();

      default:
        throw new IllegalStateException();
    }
  }
}
