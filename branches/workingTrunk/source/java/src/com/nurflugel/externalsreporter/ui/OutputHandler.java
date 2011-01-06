package com.nurflugel.externalsreporter.ui;

import com.nurflugel.Os;
import com.nurflugel.ivygrapher.OutputFormat;

import java.io.*;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jun 17, 2008 Time: 10:10:22 PM To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({ "UseOfSystemOutOrSystemErr", "CallToPrintStackTrace" })
public class OutputHandler
{
  public static final String    NEW_LINE                = "\n";
  protected static final String CLOSING_LINE_DOTGRAPH   = "}";
  protected static final String OPENING_LINE_DOTGRAPH   = "digraph G {\nnode [shape=box,fontname=\"Arial\",fontsize=\"10\"];\nedge [fontname=\"Arial\",fontsize=\"8\"];\nrankdir=RL;\n\n";
  protected static final String OPENING_LINE_SUBGRAPH   = "subgraph ";
  private static final String   QUOTE                   = "\"";
  private boolean               shouldGroupByBuildfiles = false;
  private MainFrame             mainFrame;
  private Os                    os;

  public OutputHandler(MainFrame mainFrame)
  {
    this.mainFrame = mainFrame;
    os             = mainFrame.getOs();
  }

  // -------------------------- OTHER METHODS --------------------------

  public File launchDot(File dotFile, File dotExecutable) throws IOException, InterruptedException
  {
    String outputFileName    = "externals" + mainFrame.getOs().getOutputFormat().getExtension();
    File   outputFile        = new File(dotFile.getParent(), outputFileName);
    File   parentFile        = outputFile.getParentFile();
    String dotExecutablePath = dotFile.getAbsolutePath();
    String outputFilePath    = outputFile.getAbsolutePath();

    if (outputFile.exists())
    {                       // logger.debug("Deleting existing version of " + outputFilePath);
      outputFile.delete();  // delete the file before generating it if it exists
    }

    OutputFormat outputFormat     = os.getOutputFormat();
    String       outputFormatName = outputFormat.getType();

    // this is to deal with different versions of Graphviz on OS X - if dot is in applications (old version), preface with an e for epdf.  If it's
    // in /usr/local/bin, leave as pdf
    if ((os == Os.OS_X) && dotExecutablePath.startsWith("/Applications"))
    {
      outputFormatName = "e" + outputFormatName;
    }

    String[] command = { dotExecutable.getAbsolutePath(), "-T" + outputFormatName, dotExecutablePath, "-o" + outputFilePath };

    // String[] command = { dotExecutable.getAbsolutePath(), "-T" + outputFormatName, dotExecutablePath, "-o" + outputFilePath };

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
    String outputFilePath = imageFile.getAbsolutePath();

    try
    {
      os.openFile(outputFilePath);
    }
    catch (Exception e)
    {
      throw new IOException(e);
    }
  }

  public File writeDotFile(List<External> externals, List<ProjectExternalReference> projectsList) throws IOException
  {
    File         imageDir    = mainFrame.getConfig().getImageDir();
//    JFileChooser fileChooser = new JFileChooser(imageDir);
//
//    fileChooser.setDialogTitle("Choose a location for your image");
//    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//    fileChooser.setMultiSelectionEnabled(false);
//    fileChooser.ensureFileIsVisible(imageDir);
//
//    int returnVal = fileChooser.showDialog(mainFrame, "Save image here");
//
//    if (returnVal == JFileChooser.APPROVE_OPTION)
//    {
//      imageDir = fileChooser.getSelectedFile();

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

      writeDotFileTargetDeclarations(out, externals, projectsList);
      writeDotFileDependencies(projectsList, out);

      out.writeBytes(CLOSING_LINE_DOTGRAPH);
      outputStream.close();

      return dotFile;
//    }
//
//    return null;
  }

    private void writeDotFileDependencies(List<ProjectExternalReference> projectsList, DataOutputStream out) throws IOException {
        Set<String> lines = new HashSet<String>();

        // we do this becasue we seem to have duplicates - todo figure out why "uniquelist" isn't
        for (ProjectExternalReference reference : projectsList)
        {
          if (reference.isSelected() || reference.getExternal().isSelected())
          {
            String line = QUOTE + reference.getKey() + QUOTE + " -> " + QUOTE + reference.getExternal().getKey() + QUOTE + NEW_LINE;

            lines.add(line);
          }
        }

        for (String line : lines)
        {
          out.writeBytes(line);
        }
    }

    private void writeDotFileTargetDeclarations(DataOutputStream out, List<External> externals, List<ProjectExternalReference> projectsList)
                                       throws IOException
  {
    int clusterIndex = 0;

    out.writeBytes("\t" + OPENING_LINE_SUBGRAPH + "cluster_" + clusterIndex + " {" + NEW_LINE);

    Set<String> lines = new HashSet<String>();

    for (External external : externals)
    {
      if (external.isSelected())
      {
        String line = "\t\t" + QUOTE + external.getKey() + QUOTE + " [label=" + QUOTE + external.getLabel() + QUOTE + " shape=box color=black ]; "
                      + NEW_LINE;

        lines.add(line);
      }
    }

    for (String line : lines)
    {
      out.writeBytes(line);
    }

    lines.clear();

    for (ProjectExternalReference project : projectsList)
    {
      if (project.isSelected())
      {
        String line = "\t\t" + QUOTE + project.getKey() + QUOTE + " [label=" + QUOTE + project.getLabel() + QUOTE + " shape=box color=black ]; ";

        lines.add(line);
      }
    }

    for (String line : lines)
    {
      out.writeBytes(line + NEW_LINE);
    }

    out.writeBytes("\t}" + NEW_LINE);
    clusterIndex++;
  }  // end for
}
