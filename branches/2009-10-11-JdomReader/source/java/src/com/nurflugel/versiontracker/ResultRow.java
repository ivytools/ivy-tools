package com.nurflugel.versiontracker;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Oct 15, 2009 Time: 10:04:24 PM To change this template use File | Settings | File Templates.
 */
class ResultRow implements Comparable
{
  private String path;
  private String version;

  ResultRow(String path, String version)
  {
    this.path    = path;
    this.version = version;
  }

  public String getPath()
  {
    return path;
  }

  public String getVersion()
  {
    return version;
  }

  public int compareTo(Object o)
  {ResultRow other = (ResultRow) o;
    return path.compareTo(other.getPath());
  }
}
