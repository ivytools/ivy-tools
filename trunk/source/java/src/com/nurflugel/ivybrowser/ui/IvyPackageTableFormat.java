package com.nurflugel.ivybrowser.ui;

import ca.odell.glazedlists.gui.TableFormat;

import com.nurflugel.ivybrowser.domain.IvyPackage;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jan 25, 2008 Time: 7:12:02 PM To change this template use File | Settings | File Templates.
 */
public class IvyPackageTableFormat implements TableFormat
{
  static final int ORG      = 0;
  static final int MODULE   = ORG + 1;
  static final int REVISION = MODULE + 1;

  // static final int FILE        = 3;
  static final int SOURCE      = REVISION + 1;
  static final int JAVADOCS    = SOURCE + 1;
  private String[] columnNames = { "Org", "Module", "Revision", "Source?", "Javadocs?" };

  // ------------------------ INTERFACE METHODS ------------------------
  // --------------------- Interface TableFormat ---------------------
  @Override
  public int getColumnCount()
  {
    return 5;
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
    IvyPackage ivyPackage = (IvyPackage) o;

    switch (i)
    {
      case ORG:
        return ivyPackage.getOrgName();

      case MODULE:
        return ivyPackage.getModuleName();

      case REVISION:
        return ivyPackage.getVersion();

      case SOURCE:
        return ivyPackage.hasSourceCode();

      case JAVADOCS:
        return new Boolean(ivyPackage.hasJavaDocs());

      default:
        throw new IllegalStateException();
    }
  }
}
