package com.nurflugel.versiontracker;

import java.io.File;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Oct 15, 2009 Time: 10:04:24 PM To change this template use File | Settings | File Templates.
 */
class ResultRow implements Comparable
{
  private File             file;
  private String           version;
  private VersionTrackerUi versionTrackerUi;

  ResultRow(File file, String version, VersionTrackerUi versionTrackerUi)
  {
    this.file             = file;
    this.version          = version;
    this.versionTrackerUi = versionTrackerUi;
  }

  // ------------------------ INTERFACE METHODS ------------------------

  // --------------------- Interface Comparable ---------------------

  public int compareTo(Object o)
  {
    ResultRow other = (ResultRow) o;

    return getPath().compareTo(other.getPath());
  }

  // --------------------- GETTER / SETTER METHODS ---------------------

  public String getPath()
  {
    if (versionTrackerUi.useShortPaths())
    {
      return file.getName();
    }

    return file.getAbsolutePath();
  }

  public String getVersion()
  {
    return version;
  }
}
