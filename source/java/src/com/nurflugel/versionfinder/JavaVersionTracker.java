package com.nurflugel.versionfinder;

import org.apache.commons.lang.StringUtils;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Finds all the vesions of classes in jars (i.e., 49, 50, etc) to help with version mismatch problems in jdk 1.4/1.5/1.6. */
@SuppressWarnings({ "UseOfSystemOutOrSystemErr", "CallToPrintStackTrace" })
public class JavaVersionTracker
{
  private File    tempDir = new File("tempDir");
  private boolean useTest = false;

  public static void main(String[] args)
  {
    if (args.length == 0)
    {
      System.out.println("Usage: java -classpath buildtasks.jar net.nike.build.javaversionfinder files_or_dirs");
      System.out.println("     where files_or_dirs is a space delimited list of dirs of jars, or individual jars");
      System.exit(0);
    }

    JavaVersionTracker vf = new JavaVersionTracker();

    if (vf.useTest)
    {
      try
      {
        // Process process = Runtime.getRuntime().exec("javap -v -classpath c:/snapshots/buildtasks/trunk/tempDir
        // org.aopalliance.intercept.MethodInvocation");
        Process process = Runtime.getRuntime().exec("javap -v -classpath /Users/douglasbullard/Documents/JavaStuff/Nike_Subversion_Projects/buildtasks/trunk/tempDir org.aopalliance.intercept.MethodInvocation");

        // vf.printOutput(new File("."), process, jarFile, jarResults);
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
      //
    }
    else
    {
      vf.doIt(args);
    }
  }

  /** @param  args  list of dirs or jars to parse */
  private void doIt(String[] args)
  {
    JFileChooser fileChooser = new JFileChooser("Pick dirs or jars");

    fileChooser.setMultiSelectionEnabled(true);

    int    i             = fileChooser.showDialog(null, "Find versions in these files and/or dirs");
    File[] selectedFiles = fileChooser.getSelectedFiles();

    // if (args.length == 0)
    // {
    // showUsage();
    // }
    // else
    // {
    Map<File, Set<String>> jarResults = new HashMap<File, Set<String>>();  // map of string(jar path, version list)

    for (File file : selectedFiles)
    {
      processArg(file.getAbsolutePath(), jarResults);
    }

    Set<File>  files    = jarResults.keySet();
    List<File> fileList = new ArrayList<File>(files);

    Collections.sort(fileList);

    int maxLength = 0;

    for (File file : fileList)
    {
      maxLength = Math.max(maxLength, file.getAbsolutePath().length());
    }

    for (File file : fileList)
    {
      StringBuilder builder  = new StringBuilder();
      Set<String>   versions = jarResults.get(file);

      for (String version : versions)
      {
        builder.append(version).append(" ");
      }

      System.out.println(file.getAbsolutePath() + getWhiteSpace(file, maxLength) + builder);
    }

    tempDir.deleteOnExit();

    // System.exit(0);
  }

  /** Show suggested usage. */
  private void showUsage()
  {
    System.out.println("\n\n\nVersionFinder will find versions of classes of jars in your path");
    System.out.println("Usage: java -classpath buildtasks.jar;commons-lang.jar net.nike.build.javaversionfinder.VersionFinder jar1 dir1 dir2 etc");
    System.out.println("Arguments are a single jar, multiple jars or directories of jars, delimited by spaces ");
    System.out.println("An example of running this for OC (which doesn't have buildtasks from Ivy, so I'm getting it from another project: ");
    System.out.println("Usage: java -classpath \\snapshots\\buildtasks\\trunk\\unversioned\\lib\\build\\buildtasks.jar;\\snapshots\\buildtasks\\trunk\\unversioned\\lib\\build\\commons-lang.jar net.nike.build.javaversionfinder.VersionFinder build\\lib lib\\build app\\server-config\\atg\\lib\n\n\n");
  }

  private void processArg(String arg, Map<File, Set<String>> jarResults)
  {
    File   file = new File(arg);
    String name = file.getName();

    if (name.endsWith(".jar") || name.endsWith(".zip"))
    {
      processJarFile(file, jarResults);
    }
    else if (file.isDirectory())
    {
      processDir(file, jarResults, file, false);
    }
  }

  private void processJarFile(File jarFile, Map<File, Set<String>> jarResults)
  {
    if (jarFile.getName().endsWith(".jar"))
    {
      deleteFile(tempDir);
      tempDir.mkdirs();
      unzipFile(jarFile);
      processExpandedDir(jarResults, jarFile);

      // Set<String>   list    = jarResults.get(jarFile);
      // StringBuilder builder = new StringBuilder();
      //
      // if (list != null)
      // {
      // for (String version : list)
      // {
      // builder.append(version).append(" ");
      // }
      // }
      // else
      // {
      // System.out.println("Jar " + jarFile + " was skipped");
      // }

      // System.out.println("Jar file: " + jarFile + getWhiteSpace(jarFile) + "versions: " + builder);
      deleteFile(tempDir);
    }
  }

  private String getWhiteSpace(File jarFile, int maxLength)
  {
    int padding = maxLength + 3;

    padding -= jarFile.getAbsolutePath().length();
    // padding -= jarFile.getName().length();

    StringBuilder sb = new StringBuilder(":");

    for (int i = 0; i < padding; i++)
    {
      sb.append(" ");
    }

    return sb.toString();
  }

  private void deleteFile(File file)
  {
    boolean result = file.delete();

    if (!result)
    {
      // System.out.println("Didn't remove file "+file);
    }
  }

  private void processExpandedDir(Map<File, Set<String>> jarResults, File jarFile)
  {
    File[]  files        = tempDir.listFiles();
    boolean wasProcessed = false;

    for (File file : files)
    {
      if (file.isDirectory())
      {
        wasProcessed = processDir(file, jarResults, jarFile, wasProcessed);
      }

      // if(file.getName().endsWith(".jar"))processJarFile(file,jarResults);
      if (file.getName().endsWith(".class"))
      {
        if (!wasProcessed)
        {
          wasProcessed = true;  // todo pass this in as a parameter, so we can force recursion iton all dirs
          processClassFile(file, jarResults, jarFile);
        }
      }

      deleteFile(file);
    }
  }

  private boolean processDir(File dir, Map<File, Set<String>> jarResults, File jarFile, boolean processed)
  {
    File[]  files        = dir.listFiles();
    boolean wasProcessed = processed;

    for (File file : files)
    {
      if (file.isDirectory())
      {
        wasProcessed = processDir(file, jarResults, jarFile, wasProcessed);
      }

      if (file.getName().endsWith(".jar"))
      {
        processJarFile(file, jarResults);
      }
      else if (file.getName().endsWith(".class"))
      {
        if (!wasProcessed)
        {
          processClassFile(file, jarResults, jarFile);
          wasProcessed = true;
        }

        deleteFile(file);
      }
    }

    return wasProcessed;
  }

  private void processClassFile(File file, Map<File, Set<String>> jarResults, File jarFile)
  {
    try
    {
      // call javap with tempdir as classapth
      // remove tempdir from file name, convert file seperators to "."
      // call javap
      // parse output to catch major version
      // put into map for jar file
      String className = getClassName(file);

      className = StringUtils.substringBefore(className, ".class");

      Runtime runtime = Runtime.getRuntime();

      // String[] command = {"javap.exe","-cp", tempDir.getAbsolutePath(), className};
      // String[] command = {"javap.exe -v -classpath "+tempDir.getAbsolutePath()+" "+className};
      Process process = runtime.exec("javap -v -classpath " + tempDir.getAbsolutePath() + " " + className);

      printOutput(file, process, jarFile, jarResults);
      deleteFile(file);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  private void printOutput(File file, Process process, File jarFile, Map<File, Set<String>> jarResults) throws IOException
  {
    InputStream       inputStream  = process.getInputStream();
    InputStreamReader streamReader = new InputStreamReader(inputStream);
    BufferedReader    reader       = new BufferedReader(streamReader);
    String            line         = reader.readLine();
    int               i            = 0;

    while (line != null)
    {
      // System.out.println("line["+ i++ +"] = " + line);
      if (StringUtils.contains(line, "major version: "))
      {
        // System.out.println("line = " + line);
        String version = StringUtils.substringAfter(line, "major version: ");

        // System.out.println("Jar: " + jarFile.getName() + "     File: " + file.getName() + "     version = " + version);
        Set<String> versions = jarResults.get(jarFile);

        if (versions == null)
        {
          versions = new HashSet<String>();
          jarResults.put(jarFile, versions);
        }

        versions.add(version);
        process.destroy();

        break;
      }

      line = reader.readLine();
    }
  }

  private String getClassName(File file)
  {
    String path          = file.getAbsolutePath();
    String leadingPath   = tempDir.getAbsolutePath();
    String classPathName = StringUtils.substringAfter(path, leadingPath);
    String className     = StringUtils.replace(classPathName, File.separator, ".");

    className = StringUtils.substringAfter(className, ".");

    return className;
  }

  @SuppressWarnings({ "ResultOfMethodCallIgnored", "IOResourceOpenedButNotSafelyClosed" })
  private void unzipFile(File file)
  {
    try
    {
      ZipFile     zipFile = new ZipFile(file);
      Enumeration entries = zipFile.entries();

      while (entries.hasMoreElements())
      {
        ZipEntry entry   = (ZipEntry) entries.nextElement();
        String   name    = entry.getName();
        File     newFile = new File(tempDir, name);

        if (entry.isDirectory())
        {
          // Assume directories are stored parents first then children.
          // System.err.println("Extracting directory: " + name);
          // This is not robust, just for demonstration purposes.
          newFile.mkdir();

          continue;
        }

        // System.err.println("Extracting file: " + name);
        if (name.endsWith(".class"))
        {
          File dir = newFile.getParentFile();

          dir.mkdirs();

          FileOutputStream     fileOutputStream = new FileOutputStream(newFile);
          BufferedOutputStream out              = new BufferedOutputStream(fileOutputStream);
          InputStream          inputStream      = zipFile.getInputStream(entry);

          copyInputStream(inputStream, out);
          fileOutputStream.close();

          break;
        }
      }

      zipFile.close();
    }
    catch (IOException ioe)
    {
      System.err.println("Unhandled (but not fatal) exception:");
      ioe.printStackTrace();
    }
  }

  public void copyInputStream(InputStream in, OutputStream out) throws IOException
  {
    byte[] buffer = new byte[1024];
    int    len;

    while ((len = in.read(buffer)) >= 0)
    {
      out.write(buffer, 0, len);
    }

    in.close();
    out.close();
  }
}
