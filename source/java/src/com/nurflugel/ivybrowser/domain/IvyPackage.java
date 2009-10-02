package com.nurflugel.ivybrowser.domain;

import java.net.URL;
import java.util.List;

/** Representation of . */
public class IvyPackage implements Comparable<IvyPackage>
{
  private boolean hasJavaDocs;
  private boolean hasSourceCode;

  /**
   * this is the .ivy.xml file which is associated with the library. Most of the time it'll be the same as the library, but there are cases where more
   * than one jar or zip file is in the ivy repository, represented by this ivy file.
   */
  private String ivyFile;
  private String library;
  private String moduleName;
  private String orgName;
  private String version;
  private URL    versionUrl;

  // --------------------------- CONSTRUCTORS ---------------------------
  public IvyPackage(String orgName, String moduleName, String version)
  {
    this.orgName    = orgName;
    this.moduleName = moduleName;
    this.version    = version;
  }

  public IvyPackage(String orgName, String moduleName, String version, String library)
  {
    this.orgName    = orgName;
    this.moduleName = moduleName;
    this.version    = version;
    this.library    = library;
  }

  // -------------------------- OTHER METHODS --------------------------
  public List<IvyPackage> getDependencies()
  {
    IvyFile file = new IvyFile(versionUrl, ivyFile);

    return file.getDependencies();
  }

  public String getIvyFile()
  {
    return ivyFile;
  }

  public void setIvyFile(String ivyFile)
  {
    this.ivyFile = ivyFile;
  }

  public String getLibrary()
  {
    return library;
  }

  public String getModuleName()
  {
    return moduleName;
  }

  public String getOrgName()
  {
    return orgName;
  }

  public String getVersion()
  {
    return version;
  }

  public boolean hasJavaDocs()
  {
    return hasJavaDocs;
  }

  public boolean hasSourceCode()
  {
    return hasSourceCode;
  }

  public void setHasJavaDocs(boolean hasJavaDocs)
  {
    this.hasJavaDocs = hasJavaDocs;
  }

  public void setHasSourceCode(boolean hasSourceCode)
  {
    this.hasSourceCode = hasSourceCode;
  }

  public void setVersionUrl(URL versionUrl)
  {
    this.versionUrl = versionUrl;
  }

  @Override
  public String toString()
  {
    if (library == null)
    {
      return orgName + " " + moduleName + " " + version;
    }
    else
    {
      return orgName + " " + moduleName + " " + version + " " + library;
    }
  }

  public String getPrettyText()
  {
    return orgName + " " + moduleName + " " + version;
  }

  public int compareTo(IvyPackage ivyPackage)
  {
    String moduleA = getOrgName() + getModuleName() + getVersion();
    String moduleB = ivyPackage.getOrgName() + ivyPackage.getModuleName() + ivyPackage.getVersion();

    return moduleA.compareTo(moduleB);
  }
}
