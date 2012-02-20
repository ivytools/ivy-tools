package com.nurflugel.ivytracker;

import ca.odell.glazedlists.gui.TableFormat;
import com.nurflugel.ivybrowser.domain.IvyPackage;

/** Format for the Ivy table. */
public class IvyFileTableFormat implements TableFormat
{
  static final int IS_INCLUDED = 0;
  static final int ORG         = 1;
  static final int MODULE      = 2;
  static final int REVISION    = 3;
  static final int COUNT       = 4;
  private String[] columnNames = { "Include?", "Org", "Module", "Revision", "Usage Count" };

  // ------------------------ INTERFACE METHODS ------------------------
  // --------------------- Interface TableFormat ---------------------
  @Override
  public int getColumnCount()
  {
    return columnNames.length;
  }

  @Override
  public String getColumnName(int i)
  {
    if ((i >= 0) && (i < columnNames.length))
    {
      return columnNames[i];
    }

    throw new IllegalStateException();
  }

  @Override
  public Object getColumnValue(Object o, int i)
  {
    IvyPackage ivyFile = (IvyPackage) o;

    switch (i)
    {
      case IS_INCLUDED:
        return ivyFile.isIncluded();

      case ORG:
        return ivyFile.getOrgName();

      case MODULE:
        return ivyFile.getModuleName();

      case REVISION:
        return ivyFile.getVersion();

      case COUNT:
        return ivyFile.getCount();

      default:
        throw new IllegalStateException();
    }
  }
}
