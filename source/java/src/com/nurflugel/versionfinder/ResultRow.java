package com.nurflugel.versionfinder;

import org.apache.commons.lang.StringUtils;
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
    PathLength pathLength = versionTrackerUi.getPathLength();

    if (pathLength == PathLength.FILE_NAME)
    {
      return file.getName();
    }

    String filePath = file.getAbsolutePath();

    if (pathLength == PathLength.FULL)
    {
      return filePath;
    }

    String commonText = versionTrackerUi.getCommonText();

    filePath = StringUtils.removeStart(filePath, commonText);

    return filePath;
  }

  public String getVersion()
  {
    return version;
  }

  public File getFile()
  {
    return file;
  }
}
