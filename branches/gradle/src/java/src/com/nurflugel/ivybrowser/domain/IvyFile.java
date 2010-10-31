package com.nurflugel.ivybrowser.domain;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/** A representation of an item in the Ivy repository. todo delete this file */
public class IvyFile
{
  private List<IvyPackage> dependencies = new ArrayList<IvyPackage>();

  // --------------------------- CONSTRUCTORS ---------------------------
  public IvyFile(String ivyFile)
  {
    try
    {
      SAXBuilder builder = new SAXBuilder();
      URL        url     = new URL(ivyFile);
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

  // --------------------- GETTER / SETTER METHODS ---------------------
  public List<IvyPackage> getDependencies()
  {
    return dependencies;
  }
}
