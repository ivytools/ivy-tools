package com.nurflugel.ivybrowser.domain;

import ca.odell.glazedlists.EventList;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.commons.lang.StringUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static com.nurflugel.ivybrowser.Preferences.IVYBROWSER_DATA_XML;
import static org.apache.commons.lang.StringUtils.replace;

/** Serializer object for the app. Because XStream has an adverse reaction to the Glazed lists collections. */
public class DataSerializer
{
  private List<IvyPackage> ivyPackages;
  private String           ivyRepositoryPath;

  public DataSerializer(String ivyRepositoryPath, EventList<IvyPackage> ivyRepositoryList)
  {
    this.ivyRepositoryPath = ivyRepositoryPath;
    ivyPackages            = new ArrayList<IvyPackage>(ivyRepositoryList.size());

    for (IvyPackage ivyPackage : ivyRepositoryList)
    {
      ivyPackages.add(ivyPackage);
    }
  }

  public void saveToXml()
  {
    try
    {
      XStream    xstream    = new XStream(new DomDriver());
      File       dataFile   = getDataFile(ivyRepositoryPath);
      FileWriter fileWriter = new FileWriter(dataFile);

      System.out.println("Saving dataFile = " + dataFile);
      xstream.toXML(this, fileWriter);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  public static File getDataFile(String ivyRepositoryPath)
  {
    String userDir = System.getProperty("user.home");
    File   dataDir = new File(userDir, ".ivyBrowser");

    if (!dataDir.exists())
    {
      dataDir.mkdir();
    }

    String path     = cleanCharacters(ivyRepositoryPath);
    File   dataFile = new File(dataDir, path + '_' + IVYBROWSER_DATA_XML);

    return dataFile;
  }

  /**
   * Convert something like this:
   *
   * <p>http://subversion/svn/javaexternals/trunk/repository</p>
   *
   * <p>to this:</p>
   *
   * <p>http___subversion_svn_javaexternals_trunk_repository</p>
   *
   * <p>So that the file names won't be wonky</p>
   */
  private static String cleanCharacters(String ivyRepositoryUrl)
  {
    String newText = replace(ivyRepositoryUrl, ":", "_");

    newText = replace(newText, "/", "_");

    return newText;
  }

  public void retrieveFromXml()
  {
    try
    {
      XStream        xstream    = new XStream(new DomDriver());
      File           dataFile   = getDataFile(ivyRepositoryPath);
      FileReader     fileReader = new FileReader(dataFile);
      Object         o          = xstream.fromXML(fileReader);
      DataSerializer serializer = (DataSerializer) o;

      System.out.println("Reading dataFile = " + dataFile);
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
