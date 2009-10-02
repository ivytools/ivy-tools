package com.nurflugel.ivytracker;

import ca.odell.glazedlists.gui.TableFormat;
import com.nurflugel.ivytracker.domain.IvyFile;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jan 25, 2008 Time: 7:12:02 PM To change this template use File | Settings | File Templates.
 */
public class IvyFileTableFormat implements TableFormat
{
  static final int ORG      = 0;
  static final int MODULE   = 1;
  static final int REVISION = 2;
  static final int COUNT    = 3;

  // static final int FILE        = 3;
  // static final int SOURCE      = 4;
  // static final int JAVADOCS    = 5;
  private String[] columnNames = { "Org", "Module", "Revision", "Usage Count" };

  public int getColumnCount()
  {
    return 4;
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
    IvyFile ivyFile = (IvyFile) o;

    switch (i)
    {
      case ORG:
        return ivyFile.getOrg();

      case MODULE:
        return ivyFile.getModule();

      case REVISION:
        return ivyFile.getVersion();

      case COUNT:
        return ivyFile.getCount();

      // case FILE:
      // return ivyPackage.getLibrary();
      //
      // case SOURCE:
      // return ivyPackage.hasSourceCode();
      //
      // case JAVADOCS:
      // return ivyPackage.hasJavaDocs();
      default:
        throw new IllegalStateException();
    }
  }
}
