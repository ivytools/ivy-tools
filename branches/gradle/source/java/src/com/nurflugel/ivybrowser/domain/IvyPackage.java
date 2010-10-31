package com.nurflugel.ivybrowser.domain;

import java.util.*;
import static java.util.Collections.unmodifiableSet;

/** Representation of an Ivy library's file. */
public class IvyPackage implements Comparable<IvyPackage>
{
  private boolean hasJavaDocs;
  private boolean hasSourceCode;

  /** If true, this item is to be included in the GUI activity - this is set by the user by clicking on the checkbox which is rendered. */
  private boolean isIncluded;

  /**
   * this is the .ivy.xml file which is associated with the library. Most of the time it'll be the same as the library, but there are cases where more
   * than one jar or zip file is in the ivy repository, represented by this ivy file.
   */
  private String          moduleName;
  private String          orgName;
  private String          version;
  private Set<IvyPackage> dependencies = new TreeSet<IvyPackage>();
  private Set<String>     publications = new TreeSet<String>();
  private int             count;

  // -------------------------- STATIC METHODS --------------------------

  public static String getKey(String org, String module, String version)
  {
    return org + " " + module + " " + version;
  }

  // --------------------------- CONSTRUCTORS ---------------------------
  public IvyPackage(String orgName, String moduleName, String version)
  {
    this.orgName    = orgName;
    this.moduleName = moduleName;
    this.version    = version;
  }

  // ------------------------ INTERFACE METHODS ------------------------

  // --------------------- Interface Comparable ---------------------

  @Override
  public int compareTo(IvyPackage ivyPackage)
  {
    String moduleA = getOrgName() + getModuleName() + getVersion();
    String moduleB = ivyPackage.getOrgName() + ivyPackage.getModuleName() + ivyPackage.getVersion();

    return moduleA.compareTo(moduleB);
  }

  // -------------------------- OTHER METHODS --------------------------

  public void addDependency(IvyPackage dependencyPackage)
  {
    dependencies.add(dependencyPackage);
  }

  public void addPublication(String publication)
  {
    publications.add(publication);
  }

  public int getCount()
  {
    return count;
  }

  public List<IvyPackage> getDependencies()
  {
    return new ArrayList<IvyPackage>(dependencies);
  }

  public String getModuleName()
  {
    return moduleName;
  }

  public String getOrgName()
  {
    return orgName;
  }

  public String getPrettyText()
  {
    return orgName + " " + moduleName + " " + version;
  }

  public Collection<String> getPublications()
  {
    return unmodifiableSet(publications);
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

  public void setDependencies(Collection<IvyPackage> dependencies)
  {
    this.dependencies.addAll(dependencies);
  }

  public void setHasJavaDocs(boolean hasJavaDocs)
  {
    this.hasJavaDocs = hasJavaDocs;
  }

  public void setHasSourceCode(boolean hasSourceCode)
  {
    this.hasSourceCode = hasSourceCode;
  }

  public void setPublications(Collection<String> publications)
  {
    this.publications.addAll(publications);
  }

  @Override
  public String toString()
  {
    return orgName + " " + moduleName + " " + version;
  }

  @SuppressWarnings({ "UseOfSystemOutOrSystemErr" })
  public void touch()
  {
    String text = "Touching " + getKey();

    System.out.println(text);

    // mainFrame.setStatusLabel(text);
    count++;
  }

  public String getKey()
  {
    return orgName + " " + moduleName + " " + version;
  }

  public boolean isIncluded()
  {
    return isIncluded;
  }

  public void setIncluded(boolean included)
  {
    isIncluded = included;
  }
}
