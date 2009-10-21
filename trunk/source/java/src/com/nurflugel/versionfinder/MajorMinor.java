package com.nurflugel.versionfinder;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Oct 20, 2009 Time: 7:08:53 PM To change this template use File | Settings | File Templates.
 */
public class MajorMinor
{
  private String majorVersion;
  private String minorVersion;

  public MajorMinor(String majorVersion, String minorVersion)
  {
    this.majorVersion = majorVersion;
    this.minorVersion = minorVersion;
  }

  public String getMajorVersion()
  {
    return majorVersion;
  }

  public String getMinorVersion()
  {
    return minorVersion;
  }
}
