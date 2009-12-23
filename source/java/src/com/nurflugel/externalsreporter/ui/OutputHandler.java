package com.nurflugel.externalsreporter.ui;

import com.nurflugel.BuildableProjects;
import com.nurflugel.Os;
import com.nurflugel.common.ui.Util;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jun 17, 2008 Time: 10:10:22 PM To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({ "UseOfSystemOutOrSystemErr", "CallToPrintStackTrace" })
public class OutputHandler
{
  public static final String NEW_LINE                 = "\n";
  protected static final String CLOSING_LINE_DOTGRAPH = "}";
  protected static final String OPENING_LINE_DOTGRAPH = "digraph G {\nnode [shape=box,fontname=\"Arial\",fontsize=\"10\"];\nedge [fontname=\"Arial\",fontsize=\"8\"];\nrankdir=RL;\n\n";
  protected static final String OPENING_LINE_SUBGRAPH = "subgraph ";
  private static final String   QUOTE                 = "\"";
  private boolean               shouldGroupByBuildfiles = true;
  private MainFrame             mainFrame;

  public OutputHandler(MainFrame mainFrame)
  {
    this.mainFrame = mainFrame;
  }

  // -------------------------- OTHER METHODS --------------------------

  public File launchDot(File dotFile, File dotExecutable) throws IOException, InterruptedException
  {
    String outputFileName = "externals" + mainFrame.getOs().getOutputFormat().getExtension();
    File   outputFile     = new File(dotFile.getParent(), outputFileName);
    File   parentFile     = outputFile.getParentFile();
    String dotFilePath    = dotFile.getAbsolutePath();
    String outputFilePath = outputFile.getAbsolutePath();

    if (outputFile.exists())
    {                       // logger.debug("Deleting existing version of " + outputFilePath);
      outputFile.delete();  // delete the file before generating it if it exists
    }

    // String   outputFormat = ui.getOutputFormat().getType();
    String[] command = { dotExecutable.getAbsolutePath(), "-T" + mainFrame.getOs().getOutputFormat().getType(), dotFilePath, "-o" + outputFilePath };

    // logger.debug("Command to run: " + concatenate(command) + " parent file is " + parentFile.getPath());
    Runtime runtime = Runtime.getRuntime();
    long    start   = new Date().getTime();

    runtime.exec(command).waitFor();

    long end = new Date().getTime();

    System.out.println("Took " + (end - start) + " milliseconds to generate graphic");

    return outputFile;
  }

  public void viewResultingFile(File imageFile) throws IOException
  {
    List<String> commandList    = new ArrayList<String>();
    Runtime      runtime        = Runtime.getRuntime();
    String       outputFilePath = imageFile.getAbsolutePath();
    Os           os             = mainFrame.getOs();

    if (os == Os.OS_X)
    {
      // calling FileManager to open the URL works, if we replace spaces with %20
      outputFilePath = outputFilePath.replace(" ", "%20");

      String fileUrl = "file://" + outputFilePath;

      // We do this rather than calling
      // FileManager.openURL(fileUrl);
      // so we can compile this from Windoze, rather than alwasy having to compile it from OS X.  But it behaves just the same.
      try
      {
        Class<?> aClass = Class.forName("com.apple.eio.FileManager");
        Method   method = aClass.getMethod("openURL", String.class);

        method.invoke(null, fileUrl);
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    else
    {
      if (os == Os.WINDOWS)
      {
        commandList.add("cmd.exe");
        commandList.add("/c");
      }

      commandList.add(outputFilePath);

      String[] command = commandList.toArray(new String[commandList.size()]);
      // logger.debug("Command to run: " + concatenate(command));

      runtime.exec(command);
    }
  }

  public File writeDotFile(Map<BuildableProjects, Map<String, List<External>>> dependencies) throws IOException
  {
    JFileChooser fileChooser = new JFileChooser(mainFrame.getConfig().getImageDir());

    fileChooser.setDialogTitle("Choose a location for your image");
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setMultiSelectionEnabled(false);
    fileChooser.showDialog(mainFrame, "Save image here");

    File imageDir = fileChooser.getSelectedFile();

    mainFrame.getConfig().setImageDir(imageDir);

    File dotFile = new File(imageDir, "externals.dot");

    System.out.println("Writing output to file " + dotFile.getAbsolutePath());

    OutputStream     outputStream = new FileOutputStream(dotFile);
    DataOutputStream out          = new DataOutputStream(outputStream);

    // open a new .dot file
    out.writeBytes(OPENING_LINE_DOTGRAPH);

    if (!shouldGroupByBuildfiles)
    {
      out.writeBytes("clusterrank=none;\n");
    }

    writeDotFileTargetDeclarations(out, dependencies);

    Set<BuildableProjects> projects = dependencies.keySet();

    for (BuildableProjects project : projects)
    {
      Map<String, List<External>> branchesExternalsMap = dependencies.get(project);
      Set<String>                 branches             = branchesExternalsMap.keySet();

      for (String branch : branches)
      {
        List<External> externals = branchesExternalsMap.get(branch);

        for (External external : externals)
        {
          branch = Util.filterUrlNames(branch);

          String url = external.getUrl();

          url = Util.filterUrlNames(url);
          out.writeBytes(QUOTE + branch + "/" + external.getDir() + QUOTE + " -> " + QUOTE + url + QUOTE + NEW_LINE);
        }
      }
    }

    out.writeBytes(CLOSING_LINE_DOTGRAPH);
    outputStream.close();

    return dotFile;
  }

  private void writeDotFileTargetDeclarations(DataOutputStream out, Map<BuildableProjects, Map<String, List<External>>> dependencies)
                                       throws IOException
  {
    int                    clusterIndex = 0;
    Set<BuildableProjects> projectsSet  = dependencies.keySet();

    for (BuildableProjects project : projectsSet)
    {
      out.writeBytes("\t" + OPENING_LINE_SUBGRAPH + "cluster_" + clusterIndex + " {" + NEW_LINE);

      String fileName = project.getProjectName();

      out.writeBytes("\t\tlabel=" + QUOTE + fileName + QUOTE + NEW_LINE);

      Map<String, List<External>> branchesExternals = dependencies.get(project);

      for (String branchName : branchesExternals.keySet())
      {
        List<External> externals      = branchesExternals.get(branchName);
        String         filteredBranch = Util.filterUrlNames(branchName);

        for (External external : externals)
        {
          String filteredName = filteredBranch + "/" + Util.filterUrlNames(external.getDir());
          String line         = "\t\t" + QUOTE + filteredName + QUOTE + " [label=" + QUOTE + filteredName + QUOTE + " shape=" + "box" + " color="
                                + "black"
                                + " ]; ";

          out.writeBytes(line + NEW_LINE);
        }
      }

      out.writeBytes("\t}" + NEW_LINE);
      clusterIndex++;
    }  // end for
  }
}
