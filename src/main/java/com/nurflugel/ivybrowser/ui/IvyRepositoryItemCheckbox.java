package com.nurflugel.ivybrowser.ui;

import com.nurflugel.ivybrowser.domain.IvyRepositoryItem;
import javax.swing.*;

/** Created by IntelliJ IDEA. User: dbulla Date: Jan 23, 2008 Time: 2:55:54 PM To change this template use File | Settings | File Templates. */
public class IvyRepositoryItemCheckbox extends JCheckBox
{
  /** Use serialVersionUID for interoperability. */
  private static final long serialVersionUID = 582307514204306711L;
  private IvyRepositoryItem item;

  // --------------------------- CONSTRUCTORS ---------------------------
  public IvyRepositoryItemCheckbox(IvyRepositoryItem item)
  {
    super(item.toString());
    this.item = item;
    setSelected(true);
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public IvyRepositoryItem getItem()
  {
    return item;
  }
}
