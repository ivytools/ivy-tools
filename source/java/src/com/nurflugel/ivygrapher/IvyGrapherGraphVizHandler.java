package com.nurflugel.ivygrapher;

import com.nurflugel.Os;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.apache.commons.io.FileUtils.writeLines;

/** Outputs the .dot file to GraphViz's "dot" - this converts it into the desired format (PDF, PNG, etc). */
@SuppressWarnings({ "UseOfSystemOutOrSystemErr" })
public class IvyGrapherGraphVizHandler extends GraphVizHandler
{
  public IvyGrapherGraphVizHandler(NodeOrder nodeOrder, Os os, OutputFormat outputFormat, String dotExecutablePath, boolean deleteDotFileOnExit,
                                   boolean concentrateEdges)
  {
    super(os, outputFormat, deleteDotFileOnExit, dotExecutablePath, concentrateEdges, nodeOrder);
  }

  // -------------------------- OTHER METHODS --------------------------
  @Override
  protected void writeDotFileTargetDeclarations(Module ivyModule, Map<String, Module> moduleMap, List<String> lines)
  {
    // out.writeBytes("\t" + OPENING_LINE_SUBGRAPH + "cluster_0"  + " {" + NEW_LINE);
    String line = "\t\t" + ivyModule.getNiceXmlKey() + " [label=\"" + ivyModule.getPrettyLabel() + "\" shape=" + "ellipse" + " color=" + "red"
                    + " ]; ";

    lines.add(line);

    for (Module module : moduleMap.values())
    {
      if (!module.equals(ivyModule))
      {
        line = "\t\t" + module.getNiceXmlKey() + " [label=\"" + module.getPrettyLabel() + "\" shape=" + "ellipse" + " color=" + "black" + " ]; ";
        lines.add(line);
      }
    }
    // write(out,"\t}" + NEW_LINE);
  }

  @Override
  protected void writeDotDependencies(Map<String, Module> moduleMap, List<String> lines, Module ivyModule)
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

          String niceXmlKey = callingModule.getNiceXmlKey();
          String line       = "\t\t" + niceXmlKey + " -> " + calledModule.getNiceXmlKey() + "[color=" + color + " label=\"" + callingRev + "\"];";

          lines.add(line);
        }
      }
    }
  }
}
