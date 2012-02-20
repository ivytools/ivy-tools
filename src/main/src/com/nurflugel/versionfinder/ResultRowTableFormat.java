package com.nurflugel.versionfinder;

import ca.odell.glazedlists.gui.TableFormat;
import java.util.List;

/** This figures out how to take the object and what gets displayed in which column. */
public class ResultRowTableFormat implements TableFormat
{
  static final int        JAR           = 0;
  static final int        MAJOR_VERSION = 1;
  static final int        MINOR_VERSION = 2;
  private String[]        columnNames   = { "Jar", "Java Major Version", "Java Minor Version" };
  private List<ResultRow> results;

  public ResultRowTableFormat(List<ResultRow> results)
  {
    this.results = results;
  }

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
    if ((i >= 0) && (i < columnNames.length))
    {
      return columnNames[i];
    }

    throw new IllegalStateException();
  }

  @Override
  public Object getColumnValue(Object o, int column)
  {
    ResultRow  resultRow = (ResultRow) o;
    MajorMinor version   = resultRow.getVersion();

    switch (column)
    {
      case JAR:
        return resultRow.getPath(results);

      case MAJOR_VERSION:
        return version.getMajorVersion();

      case MINOR_VERSION:
        return version.getMinorVersion();

      default:
        throw new IllegalStateException();
    }
  }
}
