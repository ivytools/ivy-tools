package com.nurflugel.ivygrapher;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/** Renderer so we can see different colors for directories, etc. */
// public class FileCellRenderer extends JLabel implements ListCellRenderer
public class FileCellRenderer extends JLabel implements ListCellRenderer
{
  public FileCellRenderer()
  {
    setOpaque(true);
  }

  @Override
  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
  {
    FileWrapper wrapper = (FileWrapper) value;

    setText(wrapper.toString());

    File file = wrapper.getFile();

    if (value instanceof FileWrapper)
    {
      if (file.isDirectory())
      {
        setForeground(Color.BLUE);
      }
      else
      {
        setForeground(Color.BLACK);
      }

      if (isSelected)
      {
        setBackground(Color.YELLOW);
      }
      else
      {
        setBackground(Color.WHITE);
      }
    }

    return this;
  }
}
