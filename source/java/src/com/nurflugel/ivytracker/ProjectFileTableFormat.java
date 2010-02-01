package com.nurflugel.ivytracker;

import ca.odell.glazedlists.gui.TableFormat;
import com.nurflugel.ivybrowser.domain.IvyPackage;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jan 25, 2008 Time: 7:12:02 PM To change this template use File | Settings | File Templates.
 */
public class ProjectFileTableFormat implements TableFormat
{
  static final int PROJECT_NAME = 0;
  private String[] columnNames  = { "Project" };

  // ------------------------ INTERFACE METHODS ------------------------

  // --------------------- Interface TableFormat ---------------------

  @Override
  public int getColumnCount()
  {
    return 1;
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
    String project = (String) o;

    switch (i)
    {
      case PROJECT_NAME:
        return project;

      default:
        throw new IllegalStateException();
    }
  }
}
