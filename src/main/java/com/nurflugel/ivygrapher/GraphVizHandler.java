package com.nurflugel.ivygrapher;

import com.nurflugel.Os;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import static org.apache.commons.io.FileUtils.writeLines;

/** Superclass for all the common GraphViz stuff. */
public abstract class GraphVizHandler
{
  protected Os           os;
  protected OutputFormat outputFormat;
  protected String       dotExecutablePath;
  protected boolean      deleteDotFileOnExit;
  protected NodeOrder    nodeOrder;
  protected boolean      concentrateEdges;

  public GraphVizHandler(Os os, OutputFormat outputFormat, boolean deleteDotFileOnExit, String dotExecutablePath, boolean concentrateEdges,
                         NodeOrder nodeOrder)
  {
    this.os                  = os;
    this.outputFormat        = outputFormat;
    this.deleteDotFileOnExit = deleteDotFileOnExit;
    this.dotExecutablePath   = dotExecutablePath;
    this.concentrateEdges    = concentrateEdges;
    this.nodeOrder           = nodeOrder;
  }

  protected abstract void writeDotFileTargetDeclarations(Module ivyModule, Map<String, Module> moduleMap, List<String> lines);
  protected abstract void writeDotDependencies(Map<String, Module> moduleMap, List<String> lines, Module ivyModule);

  /** Convert the .dot file into png, pdf, svg, whatever. */
  @SuppressWarnings({ "OverlyLongMethod", "CallToPrintStackTrace" })
  public void processDotFile(File dotFile)
  {
    try
    {
      String outputFileName = getOutputFileName(dotFile, outputFormat.getExtension());
      File   outputFile     = new File(dotFile.getParent(), outputFileName);
      File   parentFile     = dotFile.getParentFile();
      String dotFilePath    = dotFile.getAbsolutePath();
      String outputFilePath = outputFile.getAbsolutePath();

      if (outputFile.exists())
      {
        // logger.debug("Deleting existing version of " + outputFilePath);
        outputFile.delete();  // delete the file before generating it if it exists
      }

      String outputFormatName = outputFormat.getType();

      // this is to deal with different versions of Graphviz on OS X - if dot is in applications (old version), preface with an e for epdf.  If it's
      // in /usr/local/bin, leave as pdf
      if ((os == Os.OS_X) && dotExecutablePath.startsWith("/Applications"))
      {
        outputFormatName = "e" + outputFormatName;
      }

      String[] command = { dotExecutablePath, "-T" + outputFormatName, dotFilePath, "-o" + outputFilePath };

      System.out.println("Command to run: " + concatenate(command));  // +" parent file is " + parentFile.getPath());

      Runtime runtime = Runtime.getRuntime();
      long    start   = new Date().getTime();

      runtime.exec(command).waitFor();

      long end = new Date().getTime();

      // logger.debug("Took " + (end - start) + " milliseconds to generate graphic");
      viewFile(outputFilePath);

      if (deleteDotFileOnExit)
      {
        dotFile.deleteOnExit();
      }
    }
    catch (Exception e)
    {  // todo handle error

      // logger.error(e);
      e.printStackTrace();
      System.err.println("exception = " + e);
    }
  }

  public void viewFile(String outputFilePath) throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException,
                                                     ClassNotFoundException
  {
    // mainFrame.setStatus("Opening file "+outputFilePath);
    os.openFile(outputFilePath);
  }

  private String concatenate(String[] command)
  {
    StringBuilder sb = new StringBuilder();

    for (String s : command)
    {
      sb.append(s).append(" ");
    }

    return sb.toString();
  }

  /** Takes someting like build.dot and returns build.png. */
  private String getOutputFileName(File dotFile, String outputExtension)
  {
    String results = dotFile.getName();
    int    index   = results.indexOf(".dot");

    results = results.substring(0, index) + outputExtension;

    return results;
  }

  public File generateDotFile(File xmlFile, Module ivyModule, Map<String, Module> moduleMap) throws IOException
  {
    String       fileName = xmlFile.getAbsolutePath().replace(".xml", "");
    File         dotFile  = new File(fileName + ".dot");
    List<String> lines    = new ArrayList<String>();

    // open a new .dot file
    lines.add("digraph G {\nnode [shape=ellipse,fontname=\"Arial\",fontsize=\"10\"];");
    lines.add("edge [fontname=\"Arial\",fontsize=\"8\"];\nrankdir=" + nodeOrder.getOrder() + ";\n");
    lines.add("concentrate=" + concentrateEdges + ";");
    writeDotFileTargetDeclarations(ivyModule, moduleMap, lines);
    writeDotDependencies(moduleMap, lines, ivyModule);
    lines.add("}");

    // write the lines to the file
    writeLines(dotFile, lines);

    return dotFile;
  }
}
