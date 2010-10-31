package com.nurflugel.ivybrowser.ui;

import javax.swing.*;
import java.io.File;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jan 25, 2008 Time: 1:31:10 PM To change this template use File | Settings | File Templates.
 */
public class IvyFileCheckbox extends JCheckBox
{
  /** Use serialVersionUID for interoperability. */
  private static final long serialVersionUID = -5785848516434157524L;
  private File              file;

  // --------------------------- CONSTRUCTORS ---------------------------
  public IvyFileCheckbox(File file)
  {
    super(file.getAbsolutePath());
    this.file = file;
    setSelected(true);
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public File getFile()
  {
    return file;
  }
}
