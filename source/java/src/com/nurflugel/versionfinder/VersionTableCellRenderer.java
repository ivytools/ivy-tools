package com.nurflugel.versionfinder;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import static java.awt.Color.*;
import java.util.List;

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
      setForeground(RED);
    }
    else
    {
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
