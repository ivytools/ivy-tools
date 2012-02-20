package com.nurflugel.common.ui.tree;

import javax.swing.*;
import java.awt.*;

/** Extension of label that gives a little border space around it, and has the idea of being selected or not. */
class TreeLabel extends JLabel
{
  /** Use serialVersionUID for interoperability. */
  private static final long serialVersionUID = -5147458747018926490L;
  private boolean           isSelected;
  private boolean           hasFocus;
  // -------------------------- OTHER METHODS --------------------------

  // @Override public void setBackground(Color color)
  // {
  // if (color instanceof ColorUIResource) {
  // color = null;
  // }
  //
  // super.setBackground(color);
  // }
  // @Override public void paint(Graphics g)
  // {
  // String str = getText();
  //
  // if (str != null) {
  //
  // if (0 < str.length()) {
  //
  // if (isSelected) {
  // g.setColor(UIManager.getColor("Tree.selectionBackground"));
  // } else {
  // g.setColor(UIManager.getColor("Tree.textBackground"));
  // }
  //
  // Dimension preferredSize = getPreferredSize();
  // int       imageOffset   = 0;
  // Icon      currentI      = getIcon();
  //
  // if (currentI != null) {
  // imageOffset = currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1);
  // }
  //
  // g.fillRect(imageOffset, 0, preferredSize.width - 1 - imageOffset, preferredSize.height);
  //
  // if (hasFocus) {
  // g.setColor(UIManager.getColor("Tree.selectionBorderColor"));
  // g.drawRect(imageOffset, 0, preferredSize.width - 1 - imageOffset, preferredSize.height - 1);
  // }
  // }
  // } // end if
  //
  // super.paint(g);
  // }
  @Override
  public Dimension getPreferredSize()
  {
    Dimension retDimension = super.getPreferredSize();

    if (retDimension != null)
    {
      retDimension = new Dimension(retDimension.width + 3, retDimension.height);
    }

    return retDimension;
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public void setFocus(boolean hasFocus)
  {
    this.hasFocus = hasFocus;
  }

  public void setSelected(boolean isSelected)
  {
    this.isSelected = isSelected;
  }
}
