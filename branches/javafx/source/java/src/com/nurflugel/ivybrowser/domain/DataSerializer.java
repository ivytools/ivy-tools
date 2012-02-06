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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.apache.commons.lang.StringUtils.replace;

/** Serializer object for the app. Because XStream has an adverse reaction to the Glazed lists collections. */
public class DataSerializer
{
  private List<IvyPackage>                                  ivyPackages;
  private String                                            ivyRepositoryPath;
  private Map<String, Map<String, Map<String, IvyPackage>>> packageMap;
  public static final String                                IVYBROWSER_DATA_XML = "ivybrowser_data.xml";

  public DataSerializer(String ivyRepositoryPath, List<IvyPackage> ivyRepositoryList,
                        Map<String, Map<String, Map<String, IvyPackage>>> packageMapToSave)
  {
    this.ivyRepositoryPath = ivyRepositoryPath;
    ivyPackages            = new ArrayList<IvyPackage>(ivyRepositoryList.size());
    packageMap             = new HashMap<String, Map<String, Map<String, IvyPackage>>>();
    ivyPackages.addAll(ivyRepositoryList);
    packageMap.putAll(packageMapToSave);
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
      packageMap  = serializer.getPackageMap();
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

  public Map<String, Map<String, Map<String, IvyPackage>>> getPackageMap()
  {
    return packageMap;
  }
}
