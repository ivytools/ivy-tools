package com.nurflugel.ivybrowser.domain;

/** Nice key for hash maps, etc. */
public class IvyKey implements Comparable<IvyKey>
{
  private String org;
  private String module;
  private String version;

  public IvyKey() {}

  public IvyKey(String org, String module, String version)
  {
    this.org     = org;
    this.module  = module;
    this.version = version;
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
    return org;
  }

  public String getModule()
  {
    return module;
  }

  public String getVersion()
  {
    return version;
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
