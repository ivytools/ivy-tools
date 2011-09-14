package com.nurflugel.externalsreporter.ui;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/** Created by IntelliJ IDEA. User: douglasbullard Date: Jan 1, 2010 Time: 7:42:34 PM To change this template use File | Settings | File Templates. */
public class CheckboxListRenderer extends JCheckBox implements ListCellRenderer
{
  private boolean background;

  // public CheckboxCellRenderer(boolean isBackground)
  // {
  // background = isBackground;
  // }
  @Override
  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus)
  {
    // System.out.println("theObject = " + theObject);
    if (value instanceof JCheckBox)
    {
      JCheckBox dibble = (JCheckBox) value;

      //
      // setSelected(dibble);
      // setHorizontalAlignment(CENTER);
      //
      // if (!background)
      // {
      // setOpaque(true);
      // setBackground(Color.WHITE);
      // }
      return dibble;
    }

    return new JLabel((String) value);
  }
}
