package com.nurflugel;

import com.nurflugel.externalsreporter.ui.OutputFormat;
import com.nurflugel.common.ui.Util;
import static com.nurflugel.externalsreporter.ui.OutputFormat.*;
import javax.swing.*;
import java.io.File;
import java.awt.*;

/** Enum of operating systems, and methods to deal with differenes between them. */
@SuppressWarnings({ "EnumeratedClassNamingConvention", "EnumeratedConstantNamingConvention" })
public enum Os
{
  OS_X   ("Mac OS X", "build.sh", new String[] {}, "javax.swing.plaf.mac.MacLookAndFeel", "/Applications/Graphviz.app/Contents/MacOS/dot", PDF),
  WINDOWS("Windows", "build.cmd", new String[] { "cmd.exe", "/C" }, "com.sun.java.swing.plaf.windows.WindowsLookAndFeel",
          "\"C:\\Program Files\\Graphviz2.24\\bin\\dot.exe\"", PNG);

  private String       name;
  private String       buildCommand;
  private String[]     baseCommandArgs;
  private String       lookAndFeel;
  private String       defaultDotPath;
  private OutputFormat outputFormat;

  // -------------------------- STATIC METHODS --------------------------
  public static Os findOs(String osName)
  {
    Os[] oses = values();

    for (Os ose : oses)
    {
      if (osName.toLowerCase().startsWith(ose.getName().toLowerCase()))
      {
        return ose;
      }
    }

    return WINDOWS;
  }

  // --------------------------- CONSTRUCTORS ---------------------------
  Os(String name, String buildCommand, String[] baseCommandArgs, String lookAndFeel, String defaultDotPath, OutputFormat outputFormat)
  {
    this.name            = name;
    this.buildCommand    = buildCommand;
    this.baseCommandArgs = baseCommandArgs;
    this.lookAndFeel     = lookAndFeel;
    this.defaultDotPath  = defaultDotPath;
    this.outputFormat    = outputFormat;
  }
  // -------------------------- OTHER METHODS --------------------------

  // public String[] getCommandArgs(String path, BuildableItem buildableItem)
  // {
  // BuildableProjects project = buildableItem.getProject();
  // BuildScripts buildScript = project.getBuildScript();
  // List<String> commands = new ArrayList<String>();
  //
  // commands.addAll(Arrays.asList(baseCommandArgs));
  // commands.add(getBuildCommandPath(path));
  // commands.add("-f");
  // commands.add(buildScript.getScriptName());
  // commands.add(buildableItem.getBuildTarget().toString());
  //
  // String[] output = commands.toArray(new String[commands.size()]);
  //
  // return output;
  // }
  public OutputFormat getOutputFormat()
  {
    return outputFormat;
  }

  public String getBuildCommandPath(String basePath)
  {
    return basePath + File.separator + buildCommand;
  }

  public String getName()
  {
    return name;
  }

  @SuppressWarnings({ "CallToPrintStackTrace", "OverlyBroadCatchBlock" })
  public void setLookAndFeel(Component component)
  {
    if (lookAndFeel.length() > 0)
    {
      Util.setLookAndFeel(lookAndFeel, component);
    }
  }

  public String getDefaultDotPath()
  {
    return defaultDotPath;
  }
}
