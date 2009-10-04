package com.nurflugel.common.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

/** Util class. */
public class Util
{
  public static final String OSX_DOT_LOCATION     = "/Applications/Graphviz.app/Contents/MacOS/dot";
  public static final String PREVIEW_LOCATION     = "/Applications/Preview.app/Contents/MacOS/Preview";
  public static final String WINDOWS_DOT_LOCATION = "\"\\Program Files\\ATT\\Graphviz\\bin\\dot.exe\"";
  public static final Cursor busyCursor           = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
  public static final Cursor normalCursor         = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

  private Util() {}

  /** Firgures out how much time is remaining in the task. */
  public static String calculateTimeRemaining(long startTime, int currentValue, int maxValue)
  {
    String timeRemainingString = "";
    long   now                 = new Date().getTime();
    long   difference          = now - startTime;

    if ((difference != 0) && (currentValue != 0))
    {
      long deltaTime = ((maxValue - currentValue) * difference) / currentValue;
      long minutes   = deltaTime / 60 / 1000;
      long seconds   = 0;

      if (minutes == 0)
      {
        seconds = deltaTime / 1000;
      }

      timeRemainingString = ""                      //
                            + ((minutes > 0) ? (minutes + " minutes")
                                             : "")  //
                            + ((seconds > 0) ? (seconds + " seconds")
                                             : "");
    }

    return timeRemainingString;
  }

  /** Centers the component on the screen. */
  @SuppressWarnings({ "NumericCastThatLosesPrecision" })
  public static void center(Container container)
  {
    Toolkit   defaultToolkit = Toolkit.getDefaultToolkit();
    Dimension screenSize     = defaultToolkit.getScreenSize();
    int       x              = (int) ((screenSize.getWidth() - container.getWidth()) / 2);
    int       y              = (int) ((screenSize.getHeight() - container.getHeight()) / 2);

    container.setBounds(x, y, container.getWidth(), container.getHeight());
  }

  /** Get's rid of the subversion base URL. */
  public static String filterUrlNames(String theBranch)
  {
    // todo - fix this!!!
    String branch = theBranch;

    // String branch = theBranch.replace("http://----/svn/", "");
    //
    // branch = branch.replace("http://----.nurflugel.com/svn/", "");
    branch = branch.replace("//", "/");

    return branch;
  }

  /** Sets the look and feel. */
  public static void setLookAndFeel(String feelName, Component component)
  {
    try
    {
      UIManager.setLookAndFeel(feelName);
      SwingUtilities.updateComponentTreeUI(component);
    }
    catch (Exception e)
    {
      System.out.println("Error setting native LAF: " + feelName + e.getMessage());
    }
  }
}
