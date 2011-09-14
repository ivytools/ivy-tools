package com.nurflugel.ivybrowser.domain;

import ca.odell.glazedlists.EventList;

import static com.nurflugel.ivybrowser.ui.IvyBrowserMainFrame.IVYBROWSER_DATA_XML;

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
      FileWriter fileWriter = new FileWriter(getDataFile());

      xstream.toXML(this, fileWriter);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  public static File getDataFile()
  {
    String userDir  = System.getProperty("user.dir");
    File   dataFile = new File(userDir, IVYBROWSER_DATA_XML);

    return dataFile;
  }

  public void retrieveFromXml()
  {
    // Properties properties = System.getProperties();
    // for (Map.Entry<Object, Object> objectObjectEntry : properties.entrySet())
    // {
    // System.out.println(objectObjectEntry.getKey()+"   "+ objectObjectEntry.getValue());
    //
    // }
    try
    {
      XStream        xstream    = new XStream(new DomDriver());
      FileReader     fileReader = new FileReader(getDataFile());
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
