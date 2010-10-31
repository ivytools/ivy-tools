package com.nurflugel.ivytracker;

import ca.odell.glazedlists.gui.TableFormat;
import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivytracker.domain.IvyFile;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jan 25, 2008 Time: 7:12:02 PM To change this template use File | Settings | File Templates.
 */
public class IvyFileTableFormat implements TableFormat
{
  static final int ORG         = 0;
  static final int MODULE      = 1;
  static final int REVISION    = 2;
  static final int COUNT       = 3;
  private String[] columnNames = { "Org", "Module", "Revision", "Usage Count" };

  // ------------------------ INTERFACE METHODS ------------------------

  // --------------------- Interface TableFormat ---------------------

  @Override
  public int getColumnCount()
  {
    return 4;
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
