package com.nurflugel;

import com.nurflugel.common.ui.Util;
import com.nurflugel.ivygrapher.OutputFormat;
import static com.nurflugel.ivygrapher.OutputFormat.PDF;
import static com.nurflugel.ivygrapher.OutputFormat.PNG;
import org.apache.commons.lang.SystemUtils;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/** Enum of operating systems, and methods to deal with differenes between them. */
@SuppressWarnings({ "EnumeratedClassNamingConvention", "EnumeratedConstantNamingConvention" })
public enum Os
{
  OS_X   ("Mac OS X", "build.sh", new String[] {}, "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel",
          "/Applications/Graphviz_old.app/Contents/MacOS/Graphviz", PDF),
  WINDOWS("Windows", "build.cmd", new String[] { "cmd.exe", "/C" }, "com.sun.java.swing.plaf.windows.WindowsLookAndFeel",
          "\"C:\\Program Files\\Graphviz2.24\\bin\\dot.exe\"", PNG);

  private String       name;
  private String       buildCommand;
  private String[]     baseCommandArgs;
  private String       lookAndFeel;
  private String       defaultDotPath;
  private OutputFormat outputFormat;

  // -------------------------- STATIC METHODS --------------------------
  public static Os findOs()
  {
    return SystemUtils.IS_OS_WINDOWS ? WINDOWS
                                     : OS_X;
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

      System.out.println("Opening URL " + fileUrl);
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
