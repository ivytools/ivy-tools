package com.nurflugel;

import com.nurflugel.externalsreporter.ui.OutputFormat;
import java.io.File;

/** Enum of operating systems, and methods to deal with differenes between them. */
@SuppressWarnings({ "EnumeratedClassNamingConvention", "EnumeratedConstantNamingConvention" })
public enum Os
{
  OS_X   ("Mac OS X", "build.sh", new String[] {}, "javax.swing.plaf.mac.MacLookAndFeel", "/Applications/Graphviz.app/Contents/MacOS/dot",
          OutputFormat.PDF),
  WINDOWS("Windows", "build.cmd", new String[] { "cmd.exe", "/C" }, "com.sun.java.swing.plaf.windows.WindowsLookAndFeel",
          "\"C:\\Program Files\\ATT\\Graphviz\\bin\\dot.exe\"", OutputFormat.PNG);

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
  public void setLookAndFeel()
  {
    // if (lookAndFeel.length() > 0)
    // {
    // try
    // {
    // UIManager.setLookAndFeel(lookAndFeel);
    // }
    // catch (Exception e)
    // {
    // e.printStackTrace();
    // }
    // }
  }

  public String getDefaultDotPath()
  {
    return defaultDotPath;
  }
}
