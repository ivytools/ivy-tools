package com.nurflugel.ivygrapher;

import com.apple.eio.FileManager;
import static com.nurflugel.ivygrapher.OutputFormat.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Sep 30, 2009 Time: 8:09:58 AM To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({ "UseOfSystemOutOrSystemErr" })
public class GraphVizHandler
{
  // public static final Logger logger                = LogFactory.getLogger(GraphVizHandler.class);
  public static final String NEW_LINE = "\n";
  protected static final String CLOSING_LINE_DOTGRAPH = "}";
  protected static final String OPENING_LINE_DOTGRAPH = "digraph G {\nnode [shape=ellipse,fontname=\"Arial\",fontsize=\"10\"];\n"
                                                        + "edge [fontname=\"Arial\",fontsize=\"8\"];\nrankdir=TB;\n\n"
                                                        + "concentrate=false;\n";
  private static final String   DOT_EXECUTABLE        = "dotExecutable";
  protected static final String OPENING_LINE_SUBGRAPH = "subgraph ";
  private String                os                    = System.getProperty("os.name");
  private Preferences           preferences;

  public GraphVizHandler(Preferences preferences)
  {
    this.preferences = preferences;
  }

  /** @return  true if the OS is OS X */
  private boolean isOsX()
  {
    return os.toLowerCase().startsWith("mac os");
  }

  /** @return  true if the OS is Windoze */
  private boolean isWindows()
  {
    return (os.toLowerCase().startsWith("windows"));
  }

  public File generateDotFile(File xmlFile, Module ivyModule, Map<String, Module> moduleMap) throws IOException
  {
    String fileName = xmlFile.getAbsolutePath().replace(".xml", "");
    File   dotFile  = new File(fileName + ".dot");

    // logger.debug("Writing output to file " + dotFile.getAbsolutePath());

    OutputStream     outputStream = new FileOutputStream(dotFile);
    DataOutputStream out          = new DataOutputStream(outputStream);

    // open a new .dot file
    write(out, OPENING_LINE_DOTGRAPH);

    writeDotFileTargetDeclarations(ivyModule, moduleMap, out);

    writeDotDependencies(moduleMap, out, ivyModule);

    write(out, CLOSING_LINE_DOTGRAPH);

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
    OutputFormat outputFormat;

    if (isOsX())
    {
      outputFormat = PDF;
    }
    else
    {
      outputFormat = PNG;
    }

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
      String[] command          = { findDotExecutablePath(), "-T" + outputFormatName, dotFilePath, "-o" + outputFilePath };

      // logger.debug("Command to run: " + concatenate(command) + " parent file is " + parentFile.getPath());

      Runtime runtime = Runtime.getRuntime();
      long    start   = new Date().getTime();

      runtime.exec(command).waitFor();

      long end = new Date().getTime();

      // logger.debug("Took " + (end - start) + " milliseconds to generate graphic");

      List<String> commandList = new ArrayList<String>();

      if (isOsX())
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
        if (isWindows())
        {
          commandList.add("cmd.exe");
          commandList.add("/c");
        }

        commandList.add(outputFilePath);
        command = commandList.toArray(new String[commandList.size()]);
        // logger.debug("Command to run: " + concatenate(command));

        runtime.exec(command);
      }

      dotFile.deleteOnExit();
    }
    catch (Exception e)
    {  // todo handle error

      // logger.error(e);
    }
  }

  /**
   * @param   commands  dibble
   *
   * @return  dibble
   */
  private String concatenate(String[] commands)
  {
    StringBuilder stringBuffer = new StringBuilder();

    for (String command : commands)
    {
      stringBuffer.append(" ");
      stringBuffer.append(command);
    }

    return stringBuffer.toString();
  }

  /** Takes someting like build.dot and returns build.png. */
  private String getOutputFileName(File dotFile, String outputExtension)
  {
    String results = dotFile.getName();
    int    index   = results.indexOf(".dot");

    results = results.substring(0, index) + outputExtension;

    return results;
  }

  private String findDotExecutablePath()
  {
    String dotExecutablePath = preferences.get(DOT_EXECUTABLE, "");

    if ((dotExecutablePath == null) || (dotExecutablePath.length() == 0))
    {
      if (os.startsWith("Mac OS"))
      {
        dotExecutablePath = "/Applications/Graphviz.app/Contents/MacOS/dot";
      }
      else  // if (os.toLowerCase().startsWith("windows"))
      {
        dotExecutablePath = "\"C:\\Program Files\\ATT\\Graphviz\\bin\\dot.exe\"";
      }
    }

    // Create a file chooser NoDotDialog dialog            = new NoDotDialog(dotExecutablePath); File        dotExecutableFile = dialog.getFile();
    //
    // if (dotExecutableFile != null) { dotExecutablePath = dotExecutableFile.getAbsolutePath(); preferences.put(DOT_EXECUTABLE, dotExecutablePath); }
    // else { JOptionPane.showMessageDialog(frame, "Sorry, this program can't run without the GraphViz installation.\n" + "  Please install that and
    // try again"); System.exit(0); }
    return dotExecutablePath;
  }
}
