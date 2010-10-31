package com.nurflugel.ivybrowser.domain;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/** This is the representation of an Ivy package as found on the file system, NOT in the Subversion repository over HTML. */
@SuppressWarnings({ "AssignmentToCollectionOrArrayFieldFromParameter", "CallToPrintStackTrace" })
public class IvyRepositoryItem
{
  private static final int BYTE_MASK = 0xFF;

  /**
   * this is the .ivy.xml file which is associated with the library. Most of the time it'll be the same as the library, but there are cases where more
   * than one jar or zip file is in the ivy repository, represented by this ivy file.
   */
  private List<File>              libraryFiles;
  private String                  moduleName;
  private String                  orgName;
  private String                  rev;
  private List<IvyRepositoryItem> dependencies;
  private File                    repositoryDir;
  private DateFormat              dateFormat = new SimpleDateFormat("yyyyMMDDHHmmss");

  // --------------------------- CONSTRUCTORS ---------------------------
  public IvyRepositoryItem(String orgName, String moduleName, String rev, File repositoryDir)
  {
    this.repositoryDir = repositoryDir;
    this.orgName       = orgName;
    this.moduleName    = moduleName;
    this.rev           = rev;
  }

  // -------------------------- OTHER METHODS --------------------------
  public List<IvyRepositoryItem> getDependencies()
  {
    return Collections.unmodifiableList(dependencies);
  }

  public String getIvyLine()
  {
    return "maint clean mirror " + orgName + " " + moduleName + " " + rev;
  }

  public List<File> getLibraryFiles()
  {
    return Collections.unmodifiableList(libraryFiles);
  }

  public String getModule()
  {
    return moduleName;
  }

  public String getOrg()
  {
    return orgName;
  }

  /** gets a timestamp of the format yyyymmddhhmmss. */
  private String getTimestamp()
  {
    Date now = new Date();

    return dateFormat.format(now);
  }

  /** Create the new entry - ivy xml file, properly named libraries, dirs, subdirs, etc. */
  public void saveToDisk()
  {
    try
    {
      File   dirPath       = createParentDirs();
      File[] existingFiles = dirPath.listFiles();

      for (File existingFile : existingFiles)
      {
        existingFile.delete();
      }

      writeLibraryFiles(dirPath);
      writeIvyFile(dirPath);
      writeChecksumFiles(dirPath);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (NoSuchAlgorithmException e)
    {
      e.printStackTrace();
    }
  }

  private File createParentDirs()
  {
    File orgNameDir = new File(repositoryDir, orgName);

    orgNameDir.mkdir();

    File moduleDir = new File(orgNameDir, moduleName);

    moduleDir.mkdir();

    File revDir = new File(moduleDir, rev);

    revDir.mkdir();

    return revDir;
  }

  /** copy all the library files to their new location, with the proper version appended. */
  private void writeLibraryFiles(File dirPath) throws IOException
  {
    for (File libraryFile : libraryFiles)
    {
      String fileName      = getVersionedFileName(libraryFile);
      File   versionedFile = new File(dirPath, fileName);

      copyFile(libraryFile, versionedFile);
    }
  }

  private String getVersionedFileName(File libraryFile)
  {
    String fullName     = libraryFile.getName();
    String returnString;
    int    index        = fullName.lastIndexOf(".");

    if (index == -1)
    {
      returnString = fullName + "-" + rev;
    }
    else
    {
      String foreString = fullName.substring(0, index);
      String aftString  = fullName.substring(index);

      returnString = foreString + "-" + rev + aftString;
    }

    return returnString;
  }

  public void copyFile(File in, File out) throws IOException
  {
    FileInputStream  fis = new FileInputStream(in);
    FileOutputStream fos = new FileOutputStream(out);
    byte[]           buf = new byte[1024];
    int              i;

    while ((i = fis.read(buf)) != -1)
    {
      fos.write(buf, 0, i);
    }

    fis.close();
    fos.close();
  }

  /** Create the actual Ivy xml file. */
  @SuppressWarnings({ "OverlyLongMethod" })
  private void writeIvyFile(File dirPath)
  {
    Element root = new Element("ivy-module");

    root.setAttribute("version", "1.0");

    Element infoElement = new Element("info").setAttribute("organisation", orgName).setAttribute("module", moduleName).setAttribute("revision", rev)
                                             .setAttribute("status", "release").setAttribute("publication", getTimestamp()).setAttribute("default",
                                                                                                                                         "true");
    Element configurationsElement = new Element("configurations");
    Element defaultElement        = new Element("conf").setAttribute("name", "default").setAttribute("visibility", "public");
    Element sourceElement         = new Element("conf").setAttribute("name", "source").setAttribute("visibility", "public");
    Element docElement            = new Element("conf").setAttribute("name", "javadoc").setAttribute("visibility", "public");
    Element publicationsElement   = new Element("publications");

    for (File libraryFile : libraryFiles)
    {
      String  fullName        = libraryFile.getName();
      int     index           = fullName.lastIndexOf(".");
      String  extension       = fullName.substring(index + 1);
      String  foreString      = fullName.substring(0, index);
      Element artifactElement = new Element("artifact").setAttribute("name", foreString);

      artifactElement.setAttribute("type", extension).setAttribute("ext", extension);

      if (fullName.contains("src") || fullName.contains("source"))
      {
        artifactElement.setAttribute("conf", "source");
      }
      else if (fullName.contains("doc") || fullName.contains("javadoc"))
      {
        artifactElement.setAttribute("conf", "javadoc");
      }
      else
      {
        artifactElement.setAttribute("conf", "default");
      }

      publicationsElement.addContent(artifactElement);
    }

    Element dependenciesElement = new Element("dependencies");

    for (IvyRepositoryItem item : dependencies)
    {
      Element dependencyElement = new Element("dependency").setAttribute("org", item.getOrg()).setAttribute("name", item.getModule())
                                                           .setAttribute("rev", item.getRev()).setAttribute("conf", "default");

      dependenciesElement.addContent(dependencyElement);
    }

    Document doc = new Document(root);

    root.addContent(infoElement);
    root.addContent(configurationsElement);
    configurationsElement.addContent(defaultElement);
    configurationsElement.addContent(sourceElement);
    configurationsElement.addContent(docElement);
    root.addContent(publicationsElement);
    root.addContent(dependenciesElement);

    String       ivyFileName  = moduleName + "-" + rev + ".ivy.xml";
    File         ivyFile      = new File(dirPath, ivyFileName);
    Format       prettyFormat = Format.getPrettyFormat();
    XMLOutputter outp         = new XMLOutputter(prettyFormat);

    try
    {
      OutputStream fileStream = new FileOutputStream(ivyFile);

      outp.output(doc, fileStream);
      fileStream.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  /** For every entry in the dir, create a checksum (md5 and sha1). */
  private void writeChecksumFiles(File dirPath) throws NoSuchAlgorithmException, IOException
  {
    File[] files = dirPath.listFiles();

    for (File file : files)
    {
      if (!file.isDirectory())
      {
        writeChecksum("MD5", file);
        writeChecksum("SHA1", file);
      }
    }
  }

  private void writeChecksum(String algorythym, File file) throws NoSuchAlgorithmException, IOException
  {
    MessageDigest       md         = MessageDigest.getInstance(algorythym);
    InputStream         is         = new FileInputStream(file);
    BufferedInputStream bis        = new BufferedInputStream(is);
    int                 bufferSize = 512;
    byte[]              bytes      = new byte[bufferSize];

    md.reset();

    DigestInputStream dis = new DigestInputStream(is, md);

    while (dis.read(bytes, 0, bufferSize) != -1) {}

    dis.close();
    is.close();

    byte[] digest  = md.digest();
    String hexHash = createDigestString(digest);

    System.out.println(algorythym + " " + file + " digest = " + hexHash);

    FileWriter fw = new FileWriter(file.getAbsolutePath() + "." + algorythym.toLowerCase());

    fw.write(hexHash);
    fw.close();
  }

  private String createDigestString(byte[] fileDigestBytes)
  {
    StringBuilder checksumSb = new StringBuilder();

    for (byte fileDigestByte : fileDigestBytes)
    {
      String hexStr = Integer.toHexString(BYTE_MASK & fileDigestByte);

      if (hexStr.length() < 2)
      {
        checksumSb.append("0");
      }

      checksumSb.append(hexStr);
    }

    return checksumSb.toString();
  }

  // ------------------------ CANONICAL METHODS ------------------------
  @Override
  public String toString()
  {
    return orgName + " " + moduleName + " " + rev;
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public String getRev()
  {
    return rev;
  }

  public void setDependencies(List<IvyRepositoryItem> dependencies)
  {
    this.dependencies = dependencies;
  }

  public void setFiles(List<File> files)
  {
    this.libraryFiles = files;
  }
}
