package com.nurflugel.externalsreporter.ui;

import com.nurflugel.Os;
import static com.nurflugel.Os.OS_X;
import static org.apache.commons.io.FileUtils.writeLines;
import com.nurflugel.ivygrapher.OutputFormat;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Output handler for making the dot file and image. */
@SuppressWarnings({ "UseOfSystemOutOrSystemErr", "CallToPrintStackTrace" })
public class OutputHandler
{
  public static final String       NEW_LINE                = "\n";
  protected static final String    CLOSING_LINE_DOTGRAPH   = "}";
  protected static final String    OPENING_LINE_DOTGRAPH   = "digraph G {\nnode [shape=box,fontname=\"Arial\",fontsize=\"10\"];\nedge [fontname=\"Arial\",fontsize=\"8\"];\nrankdir=RL;\n\n";
  protected static final String    OPENING_LINE_SUBGRAPH   = "subgraph ";
  private static final String      QUOTE                   = "\"";
  private boolean                  shouldGroupByBuildfiles = false;
  private ExternalsFinderMainFrame mainFrame;
  private Os                       os;

  public OutputHandler(ExternalsFinderMainFrame mainFrame)
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
    if ((os == OS_X) && dotExecutablePath.startsWith("/Applications"))
    {
      outputFormatName = "e" + outputFormatName;
    }

    String[] command = { dotExecutable.getAbsolutePath(), "-T" + outputFormatName, dotExecutablePath, "-o" + outputFilePath };

    // String[] command = { dotExecutable.getAbsolutePath(), "-T" + outputFormatName, dotExecutablePath, "-o" + outputFilePath };
    System.out.println("Dot file is " + dotFile.getAbsolutePath() + ", output file is " + outputFilePath);

    // logger.debug("Command to run: " + concatenate(command) + " parent file is " + parentFile.getPath());
    Runtime runtime = Runtime.getRuntime();
    long    start   = new Date().getTime();

    runtime.exec(command);

    long end = new Date().getTime();

    System.out.println("Took " + (end - start) + " milliseconds to generate graphic");

    return outputFile;
  }

  public void viewResultingFile(File imageFile) throws IOException
  {
    String outputFilePath = imageFile.getAbsolutePath();

    try
    {
      mainFrame.setStatus("Opening file " + outputFilePath);
      os.openFile(outputFilePath);
    }
    catch (Exception e)
    {
      throw new IOException(e);
    }
  }

  public File writeDotFile(List<External> externals, List<ProjectExternalReference> projectsList) throws IOException
  {
    // todo use FileUtils and just write collections of lines
    File imageDir = mainFrame.getConfig().getImageDir();

    mainFrame.getConfig().setImageDir(imageDir);

    File dotFile = new File(imageDir, "externals.dot");

    System.out.println("Writing output to file " + dotFile.getAbsolutePath());

    List<String> lines = new ArrayList<String>();

    // open a new .dot file
    lines.add(OPENING_LINE_DOTGRAPH);

    if (!shouldGroupByBuildfiles)
    {
      lines.add("clusterrank=none;\n");
    }

    writeDotFileTargetDeclarations(lines, externals, projectsList);
    writeDotFileDependencies(projectsList, lines);
    lines.add(CLOSING_LINE_DOTGRAPH);
    writeLines(dotFile, lines);

    return dotFile;
  }

  private void writeDotFileDependencies(List<ProjectExternalReference> projectsList, List<String> fileLines)
  {
    Set<String> lines = new HashSet<String>();

    // we do this because we seem to have duplicates - todo figure out why "uniquelist" isn't
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
      fileLines.add(line);
    }
  }

  private void writeDotFileTargetDeclarations(List<String> fileLines, List<External> externals, List<ProjectExternalReference> projectsList)
  {
    int clusterIndex = 0;

    fileLines.add("\t" + OPENING_LINE_SUBGRAPH + "cluster_" + clusterIndex + " {" + NEW_LINE);

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
      fileLines.add(line);
    }

    lines.clear();

    for (ProjectExternalReference project : projectsList)
    {
      if (project.isSelected() || project.getExternal().isSelected())
      {
        String line = "\t\t" + QUOTE + project.getKey() + QUOTE + " [label=" + QUOTE + project.getLabel() + QUOTE + " shape=box color=black ]; ";

        lines.add(line);
      }
    }

    for (String line : lines)
    {
      fileLines.add(line + NEW_LINE);
    }

    fileLines.add("\t}" + NEW_LINE);
    clusterIndex++;
  }  // end for
}
