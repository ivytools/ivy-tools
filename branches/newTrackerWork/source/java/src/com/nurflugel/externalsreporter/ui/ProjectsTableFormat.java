package com.nurflugel.externalsreporter.ui;

import ca.odell.glazedlists.gui.TableFormat;
import com.nurflugel.common.ui.Util;
import javax.swing.*;

/** Tells the table what to display in which column for this object. */
public class ProjectsTableFormat implements TableFormat
{
  static final int  INCLUDE          = 0;
  static final int  PROJECT_BASE_URL = INCLUDE + 1;
  static final int  DIR              = PROJECT_BASE_URL + 1;
  private String[]  columnNames      = { "Include?", "Project URL", "Project dir" };
  private JCheckBox checkbox;

  public ProjectsTableFormat(JCheckBox checkbox)
  {
    this.checkbox = checkbox;
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
  public Object getColumnValue(Object o, int i)
  {
    ProjectExternalReference reference = (ProjectExternalReference) o;

    switch (i)
    {
      case INCLUDE:
        return reference.isSelected();

      case PROJECT_BASE_URL:
        if (checkbox.isSelected())
        {
          return Util.filterHttp(reference.getProjectBaseUrl());
        }
        else
        {
          return reference.getProjectBaseUrl();
        }

      case DIR:
        return reference.getExternalDir();

      default:
        throw new IllegalStateException();
    }
  }
}
