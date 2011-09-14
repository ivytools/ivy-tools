package com.nurflugel.ivybrowser.ui;

import com.nurflugel.ivybrowser.domain.IvyPackage;

import java.util.List;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class IvyRepositoryHtmlTableModel implements TableModel
{
  static final int         FILE        = 3;
  static final int         JAVADOCS    = 5;
  static final int         MODULE      = 1;
  static final int         ORG         = 0;
  static final int         REVISION    = 2;
  static final int         SOURCE      = 4;
  private List<IvyPackage> list;
  private String[]         columnNames = { "Org", "Module", "Revision", "File", "Source?", "Javadocs?" };

  // --------------------------- CONSTRUCTORS ---------------------------
  public IvyRepositoryHtmlTableModel(List<IvyPackage> list)
  {
    this.list = list;
  }

  // ------------------------ INTERFACE METHODS ------------------------
  // --------------------- Interface TableModel ---------------------
  @Override
  public int getRowCount()
  {
    return list.size();
  }

  @Override
  public int getColumnCount()
  {
    return columnNames.length;
  }

  @Override
  public String getColumnName(int columnIndex)
  {
    return columnNames[columnIndex];
  }

  @Override
  public Class getColumnClass(int c)
  {
    return getValueAt(0, c).getClass();
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    return false;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex)
  {
    IvyPackage ivyPackage = list.get(rowIndex);

    switch (columnIndex)
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
        return ivyPackage.hasJavaDocs();

      default:
        return ivyPackage.getOrgName();
    }
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}

  @Override
  public void addTableModelListener(TableModelListener l) {}

  @Override
  public void removeTableModelListener(TableModelListener l) {}

  // -------------------------- OTHER METHODS --------------------------
  public IvyPackage getItemAt(int row)
  {
    return list.get(row);
  }
}
