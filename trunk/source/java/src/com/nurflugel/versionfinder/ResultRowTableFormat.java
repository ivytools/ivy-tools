package com.nurflugel.versionfinder;

import ca.odell.glazedlists.gui.TableFormat;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Oct 15, 2009 Time: 10:03:40 PM To change this template use File | Settings | File Templates.
 */
public class ResultRowTableFormat implements TableFormat
{
  static final int JAR         = 0;
  static final int VERSION     = 1;
  private String[] columnNames = { "Jar", "Java Version" };

  // ------------------------ INTERFACE METHODS ------------------------

  // --------------------- Interface TableFormat ---------------------

  public int getColumnCount()
  {
    return 2;
  }

  public String getColumnName(int i)
  {
    if ((i >= 0) && (i < columnNames.length))
    {
      return columnNames[i];
    }

    throw new IllegalStateException();
  }

  public Object getColumnValue(Object o, int i)
  {
    ResultRow resultRow = (ResultRow) o;

    switch (i)
    {
      case JAR:
        return resultRow.getPath();

      case VERSION:
        return resultRow.getVersion();

      default:
        throw new IllegalStateException();
    }
  }
}
