package com.nurflugel.ivygrapher;

import com.apple.eio.FileManager;
import static com.nurflugel.ivygrapher.OutputFormat.*;
import com.nurflugel.Os;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** Outputs the .dot file to GraphViz's "dot" - this converts it into the desired format (PDF, PNG, etc). */
@SuppressWarnings({ "UseOfSystemOutOrSystemErr" })
public class GraphVizHandler
{
  // public static final Logger logger                = LogFactory.getLogger(GraphVizHandler.class);
  public static final String NEW_LINE            = "\n";

  private NodeOrder          nodeOrder;
  private Os                 os;
  private OutputFormat       outputFormat;
  private String             dotExecutablePath;
  private boolean            deleteDotFileOnExit;

  public GraphVizHandler(NodeOrder nodeOrder, Os os, OutputFormat outputFormat, String dotExecutablePath, boolean deleteDotFileOnExit)
  {
    this.nodeOrder           = nodeOrder;
    this.os                  = os;
    this.outputFormat        = outputFormat;
    this.dotExecutablePath   = dotExecutablePath;
    this.deleteDotFileOnExit = deleteDotFileOnExit;
  }

  public File generateDotFile(File xmlFile, Module ivyModule, Map<String, Module> moduleMap) throws IOException
  {
    String fileName = xmlFile.getAbsolutePath().replace(".xml", "");
    File   dotFile  = new File(fileName + ".dot");

    // logger.debug("Writing output to file " + dotFile.getAbsolutePath());

    OutputStream     outputStream = new FileOutputStream(dotFile);
    DataOutputStream out          = new DataOutputStream(outputStream);

    // open a new .dot file
    String openingLine = "digraph G {\nnode [shape=ellipse,fontname=\"Arial\",fontsize=\"10\"];\n"
                         + "edge [fontname=\"Arial\",fontsize=\"8\"];\nrankdir=" + nodeOrder.getOrder() + ";\n\n"
                         + "concentrate=false;\n";

    write(out, openingLine);

    writeDotFileTargetDeclarations(ivyModule, moduleMap, out);

    writeDotDependencies(moduleMap, out, ivyModule);

    write(out, "}");

    outputStream.close();

    return dotFile;
  }

  private void write(DataOutputStream out, String text) throws IOException
  {
    out.writeBytes(text);

    // System.out.println(text);
  }

  private void writeDotDependencies(Map<String, Module> moduleMap, DataOutputStream out, Module ivyModule) throws IOException
  {
    // iterate through the map for each module
    // see if it's a caller for any other module - if so, add that module as a dependency
    for (Module callingModule : moduleMap.values())
    {
      for (Module calledModule : moduleMap.values())
      {
        Map<Module, String> callersMap = calledModule.getCallers();

        if (callersMap.containsKey(callingModule))
        {
          String callingRev      = callersMap.get(callingModule);
          String calledModuleRev = calledModule.getRevision();
          String color           = "black";

          if (callingModule.equals(ivyModule))
          {
            // if the ivy script is calling out a rev which was evicted, make it red
            int result = callingRev.compareTo(calledModuleRev);

            color = (result < 0) ? "red"
                                 : "black";

            // if there is another caller with the same revision, make it red
            for (Module module : callersMap.keySet())
            {
              if (!module.equals(ivyModule))
              {
                String otherRev = callersMap.get(module);

                if (otherRev.equals(callingRev))
                {
                  color = "red";

                  break;
                }
              }
            }
          }

          String line = "\t\t" + callingModule.getNiceXmlKey() + " -> " + calledModule.getNiceXmlKey() + "[color=" + color + " label=\"" + callingRev
                        + "\"];" + NEW_LINE;

          write(out, line);
        }
      }
    }
  }

  private void writeDotFileTargetDeclarations(Module ivyModule, Map<String, Module> moduleMap, DataOutputStream out) throws IOException
  {
    // out.writeBytes("\t" + OPENING_LINE_SUBGRAPH + "cluster_0"  + " {" + NEW_LINE);

    String line = "\t\t" + ivyModule.getNiceXmlKey() + " [label=\"" + ivyModule.getPrettyLabel() + "\" shape=" + "ellipse" + " color=" + "red"
                  + " ]; ";

    write(out, line + NEW_LINE);

    for (Module module : moduleMap.values())
    {
      if (!module.equals(ivyModule))
      {
        line = "\t\t" + module.getNiceXmlKey() + " [label=\"" + module.getPrettyLabel() + "\" shape=" + "ellipse" + " color=" + "black" + " ]; ";

        write(out, line + NEW_LINE);
      }
    }
    // write(out,"\t}" + NEW_LINE);
  }

  /** Convert the .dot file into png, pdf, svg, whatever. */
  @SuppressWarnings({ "OverlyLongMethod" })
  public void processDotFile(File dotFile)
  {
    try
    {
      String outputFileName = getOutputFileName(dotFile, outputFormat.getExtension());
      File   outputFile     = new File(dotFile.getParent(), outputFileName);
      File   parentFile     = outputFile.getParentFile();
      String dotFilePath    = dotFile.getAbsolutePath();
      String outputFilePath = outputFile.getAbsolutePath();

      if (outputFile.exists())
      {
        // logger.debug("Deleting existing version of " + outputFilePath);
        outputFile.delete();  // delete the file before generating it if it exists
      }

      String   outputFormatName = outputFormat.getType();
      String[] command          = { dotExecutablePath, "-T" + outputFormatName, dotFilePath, "-o" + outputFilePath };

      // logger.debug("Command to run: " + concatenate(command) + " parent file is " + parentFile.getPath());

      Runtime runtime = Runtime.getRuntime();
      long    start   = new Date().getTime();

      runtime.exec(command).waitFor();

      long end = new Date().getTime();

      // logger.debug("Took " + (end - start) + " milliseconds to generate graphic");

      List<String> commandList = new ArrayList<String>();

      if (os == Os.OS_X)
      {
        // This method doesn't work
        // calling FileManager to open the URL works, if we replace spaces with %20
        outputFilePath = outputFilePath.replace(" ", "%20");

        String fileUrl = "file://" + outputFilePath;

        // logger.debug("Trying to open URL: " + fileUrl);
        FileManager.openURL(fileUrl);
      }
      else
      {
        if (os == Os.WINDOWS)
        {
          commandList.add("cmd.exe");
          commandList.add("/c");
        }

        commandList.add(outputFilePath);
        command = commandList.toArray(new String[commandList.size()]);
        // logger.debug("Command to run: " + concatenate(command));

        runtime.exec(command);
      }

      if (deleteDotFileOnExit)
      {
        dotFile.deleteOnExit();
      }
    }
    catch (Exception e)
    {  // todo handle error

      // logger.error(e);
    }
  }

  /** Takes someting like build.dot and returns build.png. */
  private String getOutputFileName(File dotFile, String outputExtension)
  {
    String results = dotFile.getName();
    int    index   = results.indexOf(".dot");

    results = results.substring(0, index) + outputExtension;

    return results;
  }
}
