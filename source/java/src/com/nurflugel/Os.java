package com.nurflugel;

import com.nurflugel.ivygrapher.OutputFormat;
import com.nurflugel.common.ui.Util;
import static com.nurflugel.ivygrapher.OutputFormat.*;
import java.io.File;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

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

  public String getBuildCommandPath(String basePath)
  {
    return basePath + File.separator + buildCommand;
  }

  public String getDefaultDotPath()
  {
    return defaultDotPath;
  }

  public String getName()
  {
    return name;
  }

  public OutputFormat getOutputFormat()
  {
    return outputFormat;
  }

  public void openFile(String filePath) throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException,
                                               ClassNotFoundException
  {
    if (this == OS_X)
    {
      // calling FileManager to open the URL works, if we replace spaces with %20
      String   outputFilePath = filePath.replace(" ", "%20");

      String   fileUrl        = "file://" + outputFilePath;

      Class<?> aClass         = Class.forName("com.apple.eio.FileManager");
      Method   method         = aClass.getMethod("openURL", String.class);

      method.invoke(null, fileUrl);
    }
    else
    {
      List<String> commandList = new ArrayList<String>();

      if (this == WINDOWS)
      {
        commandList.add("cmd.exe");
        commandList.add("/c");
      }

      commandList.add(filePath);

      String[] command = commandList.toArray(new String[commandList.size()]);

      // logger.debug("Command to run: " + concatenate(command));
      Runtime runtime = Runtime.getRuntime();

      runtime.exec(command);
    }
  }

  @SuppressWarnings({ "CallToPrintStackTrace", "OverlyBroadCatchBlock" })
  public void setLookAndFeel(Component component)
  {
    if (lookAndFeel.length() > 0)
    {
      Util.setLookAndFeel(lookAndFeel, component);
    }
  }
}
