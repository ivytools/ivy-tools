package com.nurflugel.versionfinder;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;
import static java.awt.Color.BLACK;
import static java.awt.Color.RED;

/** Renderer to highlight rows which exceed the threshold. */
public class VersionTableCellRenderer extends JLabel implements TableCellRenderer
{
  private VersionFinderUi ui;

  public VersionTableCellRenderer(VersionFinderUi ui)
  {
    this.ui = ui;
  }

  // ------------------------ INTERFACE METHODS ------------------------
  // --------------------- Interface TableCellRenderer ---------------------
  @Override
  public Component getTableCellRendererComponent(JTable jTable, Object whatToRender, boolean isSelected, boolean b1, int row, int col)
  {
    setText(whatToRender.toString());

    List<ResultRow> list      = ui.getResults();
    ResultRow       resultRow = list.get(row);
    String          text      = resultRow.getVersion().getMajorVersion();
    int             level     = Integer.parseInt(text.trim());
    Jdk             threshold = ui.getJdkThreshold();

    if (level >= threshold.getVersion())
    {
      if (col == 0)
      {
        System.out.println("Row " + row + " Showing " + resultRow + " as red");
      }

      setForeground(RED);
    }
    else
    {
      if (col == 0)
      {
        System.out.println("Row " + row + " Showing " + resultRow + " as black");
      }

      setForeground(BLACK);
    }

    if (col > 0)
    {
      setHorizontalAlignment(CENTER);
    }
    else
    {
      setHorizontalAlignment(LEFT);
    }

    return this;
  }
}
