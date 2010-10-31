package com.nurflugel.ivygrapher;

import java.util.HashMap;
import java.util.Map;

public class Module
{
  private String              organization;
  private String              name;
  private String              revision;
  private Map<Module, String> callers = new HashMap<Module, String>();

  public Module(String organization, String name, String revision)
  {
    this.name         = name;
    this.organization = organization;
    this.revision     = revision;
  }

  // -------------------------- OTHER METHODS --------------------------
  public void addCaller(Module caller, String callerPreferredRev)
  {
    callers.put(caller, callerPreferredRev);
  }

  public String getKey()
  {
    String text = organization + " " + name;

    return text;
  }

  public String getPrettyLabel()
  {
    return organization + "\\n" + name + "\\n" + ((revision == null) ? ""
                                                                     : (revision));
  }

  // ------------------------ CANONICAL METHODS ------------------------
  @Override
  public String toString()
  {
    return getNiceXmlKey() + "_" + revision;
  }

  public String getNiceXmlKey()
  {
    return generateKey(organization, name);
  }

  @SuppressWarnings({ "ParameterHidesMemberVariable" })
  public static String generateKey(String organization, String name)
  {
    String key = organization + "_" + name;

    key = key.replace(".", "_");
    key = key.replace("-", "_");

    return key;
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public Map<Module, String> getCallers()
  {
    return callers;
  }

  public String getRevision()
  {
    return revision;
  }

  public void setRevision(String revision)
  {
    this.revision = revision;
  }
}
