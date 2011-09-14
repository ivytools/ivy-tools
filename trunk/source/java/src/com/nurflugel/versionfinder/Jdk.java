package com.nurflugel.versionfinder;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Oct 16, 2009 Time: 7:48:55 PM To change this template use File | Settings | File Templates.
 */
public enum Jdk
{
  JDK11("JDK 1.1", 46),
  JDK12("JDK 1.2", 46),
  JDK13("JDK 1.3", 47),
  JDK14("JDK 1.4", 48),
  JDK15("JDK 1.5", 49),
  JDK16("JDK 1.6", 50),
  JDK17("JDK 1.7", 51);

  private int    version;
  private String name;

  // -------------------------- STATIC METHODS --------------------------
  public static Jdk findByVersion(int version)
  {
    Jdk[] jdks = values();

    for (Jdk jdk : jdks)
    {
      if (jdk.getVersion() == version)
      {
        return jdk;
      }
    }

    return JDK15;
  }

  Jdk(String name, int version)
  {
    this.name    = name;
    this.version = version;
  }

  // ------------------------ CANONICAL METHODS ------------------------
  @Override
  public String toString()
  {
    return "Jdk{"
             + "name='" + name + '\'' + ", version=" + version + '}';
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public String getName()
  {
    return name;
  }

  public int getVersion()
  {
    return version;
  }
}
