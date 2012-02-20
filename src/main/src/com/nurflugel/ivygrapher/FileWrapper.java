package com.nurflugel.ivygrapher;

import java.io.File;

/** Cheesy little class to provide a nicer "toString" than Java File. */
public class FileWrapper
{
  private File file;

  public FileWrapper(File file)
  {
    this.file = file;
  }

  // -------------------------- OTHER METHODS --------------------------
  public File getFile()
  {
    return file;
  }

  @Override
  public String toString()
  {
    return file.getName();
  }
}
