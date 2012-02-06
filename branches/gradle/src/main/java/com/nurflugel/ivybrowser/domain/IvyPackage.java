package com.nurflugel.ivybrowser.domain;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import static java.util.Collections.unmodifiableSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** Representation of an Ivy library's file. */
@SuppressWarnings({ "CallToPrintStackTrace" })
public class IvyPackage implements Comparable<IvyPackage>
{
  private boolean hasJavaDocs;
  private boolean hasSourceCode;

  /** If true, this item is to be included in the GUI activity - this is set by the user by clicking on the checkbox which is rendered. */
  private boolean         isIncluded;
  private IvyKey          key;
  private Set<IvyPackage> dependencies   = new TreeSet<IvyPackage>();
  private Set<IvyPackage> excludes       = new TreeSet<IvyPackage>();
  private Set<IvyPackage> globalExcludes = new TreeSet<IvyPackage>();
  private Set<String>     publications   = new TreeSet<String>();
  private int             count;
  private String          ivyFileUrl;

  // -------------------------- STATIC METHODS --------------------------
  public static IvyKey getKey(String org, String module, String version)
  {
    return new IvyKey(org, module, version);
  }

  // --------------------------- CONSTRUCTORS ---------------------------
  public IvyPackage(String orgName, String moduleName, String version)
  {
    key = new IvyKey(orgName, moduleName, version);
  }

  public IvyPackage(String ivyFileUrl)
  {
    this.ivyFileUrl = ivyFileUrl;

    try
    {
      SAXBuilder builder = new SAXBuilder();
      URL        url     = new URL(ivyFileUrl);
      Document   doc     = builder.build(url);
      Element    root    = doc.getRootElement();
      Element    child1  = root.getChild("dependencies");

      if (child1 != null)
      {
        List children = child1.getChildren("dependency");

        for (Object child : children)
        {
          Element    childElement = (Element) child;
          String     org          = childElement.getAttribute("org").getValue();
          String     name         = childElement.getAttribute("name").getValue();
          String     rev          = childElement.getAttribute("rev").getValue();
          IvyPackage ivyPackage   = new IvyPackage(org, name, rev);

          dependencies.add(ivyPackage);
        }
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
    }
    catch (JDOMException e)
    {
      e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
    }
  }

  /**
   * Although it seems like we could just query the set of dependencies to see if we have a matching item as a dependency, that's not quite so simple
   * when we start considering exludes and global excludes. I'm not doing that yet, but when I do, this method will make much more sense.
   *
   * @param   ivyKey  The key of the dependency we're checking
   *
   * @return  true if it's a dependency of this Ivy file, false if not.
   */
  public boolean containsDependency(IvyKey ivyKey)
  {
    for (IvyPackage dependency : dependencies)
    {
      if (dependency.getKey().equals(ivyKey))
      {
        return true;
      }
    }

    return false;
  }

  // ------------------------ INTERFACE METHODS ------------------------
  // --------------------- Interface Comparable ---------------------
  @Override
  public int compareTo(IvyPackage other)
  {
    return key.compareTo(other.getKey());
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

  public IvyKey getKey()
  {
    return key;
  }

  public String getModuleName()
  {
    return key.getModule();
  }

  public String getOrgName()
  {
    return key.getOrg();
  }

  public Collection<String> getPublications()
  {
    return unmodifiableSet(publications);
  }

  public String getVersion()
  {
    return key.getVersion();
  }

  public boolean hasJavaDocs()
  {
    return hasJavaDocs;
  }

  public boolean hasSourceCode()
  {
    return hasSourceCode;
  }

  public boolean isIncluded()
  {
    return isIncluded;
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

  public void setIncluded(boolean included)
  {
    isIncluded = included;
  }

  public void setPublications(Collection<String> publications)
  {
    this.publications.addAll(publications);
  }

  @Override
  public String toString()
  {
    return getPrettyText();
  }

  public String getPrettyText()
  {
    return key.getOrg() + " " + key.getModule() + " " + key.getVersion();
  }

  @SuppressWarnings({ "UseOfSystemOutOrSystemErr" })
  public void touch()
  {
    String text = "Touching " + key;

    System.out.println(text);

    // mainFrame.setStatusLabel(text);
    count++;
  }

  public String getIvyFileUrl()
  {
    return ivyFileUrl;
  }
}
