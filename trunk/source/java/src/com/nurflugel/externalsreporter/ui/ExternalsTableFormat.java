package com.nurflugel.externalsreporter.ui;

import ca.odell.glazedlists.gui.TableFormat;
import com.nurflugel.common.ui.Util;
import javax.swing.*;

/** Tells the table what to display in which column for this object. */
public class ExternalsTableFormat implements TableFormat
{
  static final int  INCLUDE      = 0;
  static final int  EXTERNAL_URL = INCLUDE + 1;
  private String[]  columnNames  = { "Include?", "External URL" };
  private JCheckBox checkbox;

  public ExternalsTableFormat(JCheckBox checkbox)
  {
    this.checkbox = checkbox;
  }

  // ------------------------ INTERFACE METHODS ------------------------
  // --------------------- Interface TableFormat ---------------------
  @Override
  public int getColumnCount()
  {
    return 2;
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
    External external = (External) o;

    switch (i)
    {
      case INCLUDE:
        return external.isSelected();

      case EXTERNAL_URL:
        if (checkbox.isSelected())
        {
          return Util.filterHttp(external.getUrl());
        }

        return external.getUrl();

      default:
        throw new IllegalStateException();
    }
  }
}
