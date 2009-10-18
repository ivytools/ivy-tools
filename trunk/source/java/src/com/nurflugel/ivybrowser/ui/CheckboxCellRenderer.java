package com.nurflugel.ivybrowser.ui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Oct 16, 2009 Time: 1:33:34 PM To change this template use File | Settings | File Templates.
 */
public class CheckboxCellRenderer extends JCheckBox implements TableCellRenderer
{
  private boolean background;

  public CheckboxCellRenderer(boolean isBackground)
  {
    background = isBackground;
  }

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
        setBackground(Color.WHITE);
      }

      return this;
    }

    return new JLabel((String) theObject);
  }
}
