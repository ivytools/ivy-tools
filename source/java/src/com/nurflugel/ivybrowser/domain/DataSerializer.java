package com.nurflugel.ivybrowser.domain;

import ca.odell.glazedlists.EventList;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

/** Serializer object for the app. Because XStream has an adverse reaction to the Glazed lists collections. */
public class DataSerializer
{
  private List<IvyPackage> ivyPackages;

  public DataSerializer(EventList<IvyPackage> ivyRepositoryList)
  {
    ivyPackages = new ArrayList<IvyPackage>(ivyRepositoryList.size());

    for (IvyPackage ivyPackage : ivyRepositoryList)
    {
      ivyPackages.add(ivyPackage);
    }
  }

  public void saveToXml()
  {
    try
    {
      // String input = (String) showInputDialog(null, "Enter a base name", "Save data to XML", JOptionPane.PLAIN_MESSAGE, null, null, "");

      // if (!isEmpty(input))
      XStream    xstream    = new XStream(new DomDriver());
      FileWriter fileWriter = new FileWriter(new File("ivybrowser_data.xml"));

      xstream.toXML(this, fileWriter);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  public void retrieveFromXml()
  {
    try
    {
      XStream        xstream    = new XStream(new DomDriver());
      FileReader     fileReader = new FileReader(new File("ivybrowser_data.xml"));
      Object         o          = xstream.fromXML(fileReader);
      DataSerializer serializer = (DataSerializer) o;

      ivyPackages = serializer.getIvyPackages();
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
  }

  public List<IvyPackage> getIvyPackages()
  {
    return ivyPackages;
  }
}
