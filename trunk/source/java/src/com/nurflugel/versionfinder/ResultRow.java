package com.nurflugel.versionfinder;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import java.io.File;
import java.util.List;

/** This represents a row in teh display. */
class ResultRow implements Comparable
{
  private File            file;
  private MajorMinor      version;
  private VersionFinderUi versionFinderUi;

  ResultRow(File file, MajorMinor version, VersionFinderUi versionFinderUi)
  {
    this.file            = file;
    this.version         = version;
    this.versionFinderUi = versionFinderUi;
  }

  // ------------------------ INTERFACE METHODS ------------------------
  // --------------------- Interface Comparable ---------------------
  @Override
  public int compareTo(Object o)
  {
    ResultRow other = (ResultRow) o;

    return getPath(null).compareTo(other.getPath(null));
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public String getPath(List<ResultRow> results)
  {
    PathLength pathLength = versionFinderUi.getPathLength();
    String     fileName   = file.getName();

    if (pathLength == PathLength.FILE_NAME)
    {
      return fileName;
    }

    String filePath = file.getAbsolutePath();

    if (pathLength == PathLength.FULL)
    {
      return filePath;
    }

    if (results == null)
    {
      return filePath;
    }

    if (results.size() == 1)
    {
      return fileName;
    }

    String commonText = versionFinderUi.getCommonText();

    filePath = StringUtils.removeStart(filePath, commonText);

    return (filePath.length() < fileName.length()) ? fileName
                                                   : filePath;
  }

  public MajorMinor getVersion()
  {
    return version;
  }

  public File getFile()
  {
    return file;
  }

  @Override
  public String toString()
  {
    return new ToStringBuilder(this).append("file", file.getName()).append("version", version).toString();
  }
}
