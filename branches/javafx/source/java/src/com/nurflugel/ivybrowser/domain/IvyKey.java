package com.nurflugel.ivybrowser.domain;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/** Nice key for hash maps, etc. */
public class IvyKey implements Comparable<IvyKey>
{
  private StringProperty org;
  private StringProperty module;
  private StringProperty version;

  public IvyKey(String org, String module, String version)
  {
    orgProperty().set(org);
    moduleProperty().set(module);
    versionProperty().set(version);
  }

  public StringProperty orgProperty()
  {
    if (org == null)
    {
      org = new SimpleStringProperty(this, "org");
    }

    return org;
  }

  public StringProperty moduleProperty()
  {
    if (module == null)
    {
      module = new SimpleStringProperty(this, "module");
    }

    return module;
  }

  public StringProperty versionProperty()
  {
    if (version == null)
    {
      version = new SimpleStringProperty(this, "version");
    }

    return org;
  }

  @Override
  public int compareTo(IvyKey other)
  {
    String moduleA = getOrg() + getModule() + getVersion();
    String moduleB = other.getOrg() + other.getModule() + other.getVersion();

    return moduleA.compareTo(moduleB);
  }

  public String getOrg()
  {
    return org.getValue();
  }

  public String getModule()
  {
    return module.getValue();
  }

  public String getVersion()
  {
    return version.getValue();
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }

    if ((o == null) || (getClass() != o.getClass()))
    {
      return false;
    }

    IvyKey ivyKey = (IvyKey) o;

    if ((module != null) ? (!module.equals(ivyKey.module))
                         : (ivyKey.module != null))
    {
      return false;
    }

    if ((org != null) ? (!org.equals(ivyKey.org))
                      : (ivyKey.org != null))
    {
      return false;
    }

    if ((version != null) ? (!version.equals(ivyKey.version))
                          : (ivyKey.version != null))
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = (org != null) ? org.hashCode()
                               : 0;

    result = (31 * result) + ((module != null) ? module.hashCode()
                                               : 0);
    result = (31 * result) + ((version != null) ? version.hashCode()
                                                : 0);

    return result;
  }

  @Override
  public String toString()
  {
    return org + " " + module + " " + version;
  }
}
