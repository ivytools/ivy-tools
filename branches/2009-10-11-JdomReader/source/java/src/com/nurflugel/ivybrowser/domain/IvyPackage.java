package com.nurflugel.ivybrowser.domain;

import java.net.URL;
import java.util.*;
import static java.util.Collections.*;

/** Representation of . */
public class IvyPackage implements Comparable<IvyPackage>
{
  private boolean hasJavaDocs;
  private boolean hasSourceCode;

  /**
   * this is the .ivy.xml file which is associated with the library. Most of the time it'll be the same as the library, but there are cases where more
   * than one jar or zip file is in the ivy repository, represented by this ivy file.
   */
  private String          moduleName;
  private String          orgName;
  private String          version;
  private Set<IvyPackage> dependencies = new TreeSet<IvyPackage>();
  private Set<String>     publications = new TreeSet<String>();

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

  public Collection<IvyPackage> getDependencies()
  {
    return unmodifiableSet(dependencies);
  }

  public String getPrettyText()
  {
    return orgName + " " + moduleName + " " + version;
  }

  public Collection<String> getPublications()
  {
    return unmodifiableSet(publications);
  }

  public boolean hasJavaDocs()
  {
    return hasJavaDocs;
  }

  public boolean hasSourceCode()
  {
    return hasSourceCode;
  }

  // ------------------------ CANONICAL METHODS ------------------------

  @Override
  public String toString()
  {
    return orgName + " " + moduleName + " " + version;
  }

  // --------------------- GETTER / SETTER METHODS ---------------------

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

  public void setDependencies(Collection<IvyPackage> dependencies)
  {
    this.dependencies.addAll(dependencies);
  }
}
