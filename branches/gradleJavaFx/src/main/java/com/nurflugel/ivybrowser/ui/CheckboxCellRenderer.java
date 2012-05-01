package com.nurflugel.ivybrowser.ui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import static java.awt.Color.WHITE;

/** Table renderer for Booleans to show as checkbox. */
public class CheckboxCellRenderer extends JCheckBox implements TableCellRenderer
{
  private boolean background;

  public CheckboxCellRenderer(boolean isBackground)
  {
    background = isBackground;
  }

  @Override
  public Component getTableCellRendererComponent(JTable jTable, Object theObject, boolean isSelected, boolean hasFocus, int row, int column)
  {
    // System.out.println("theObject = " + theObject);
    if (theObject instanceof Boolean)
    {
      Boolean dibble = (Boolean) theObject;

      setSelected(dibble);
      setHorizontalAlignment(CENTER);

      if (!background)
      {
        setOpaque(true);
        setBackground(WHITE);
      }

      return this;
    }

    return new JLabel(theObject.toString());
  }
}
