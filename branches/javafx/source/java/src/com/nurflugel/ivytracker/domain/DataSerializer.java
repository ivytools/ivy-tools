package com.nurflugel.ivytracker.domain;

import ca.odell.glazedlists.EventList;

import com.nurflugel.ivybrowser.domain.IvyPackage;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.showInputDialog;

/** Serializer object for the app. Because XStream has an adverse reaction to the Glazed lists collections. */
public class DataSerializer
{
  private List<IvyPackage>               ivyPackages;
  private Map<Project, List<IvyPackage>> projectIvyFilesMap;
  private List<Project>                  projectUrls;

  public DataSerializer(Map<Project, List<IvyPackage>> ivyFilesMap, EventList<Project> projectUrls, EventList<IvyPackage> ivyRepositoryList)
  {
    projectIvyFilesMap = new HashMap<Project, List<IvyPackage>>(ivyFilesMap.size());

    for (Project project : ivyFilesMap.keySet())
    {
      projectIvyFilesMap.put(project, ivyFilesMap.get(project));
    }

    this.projectUrls = new ArrayList<Project>(projectUrls.size());

    for (Project projectUrl : projectUrls)
    {
      this.projectUrls.add(projectUrl);
    }

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
      String input = (String) showInputDialog(null, "Enter a base name", "Save data to XML", JOptionPane.PLAIN_MESSAGE, null, null, "");

      if (!isEmpty(input))
      {
        XStream    xstream    = new XStream(new DomDriver());
        FileWriter fileWriter = new FileWriter(new File(input + "_data.xml"));

        xstream.toXML(this, fileWriter);
      }
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
      String input = (String) showInputDialog(null, "Enter a base name", "Retrieve data from XML", JOptionPane.PLAIN_MESSAGE, null, null, "trunks");

      if (!isEmpty(input))
      {
        XStream        xstream    = new XStream(new DomDriver());
        FileReader     fileReader = new FileReader(new File(input + "_data.xml"));
        Object         o          = xstream.fromXML(fileReader);
        DataSerializer serializer = (DataSerializer) o;

        projectUrls        = serializer.getProjectUrls();
        projectIvyFilesMap = serializer.getProjectIvyFilesMap();
        ivyPackages        = serializer.getIvyRepositoryList();
      }
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
  }

  public Map<Project, List<IvyPackage>> getProjectIvyFilesMap()
  {
    return projectIvyFilesMap;
  }

  public List<Project> getProjectUrls()
  {
    return projectUrls;
  }

  public List<IvyPackage> getIvyRepositoryList()
  {
    return ivyPackages;
  }
}
