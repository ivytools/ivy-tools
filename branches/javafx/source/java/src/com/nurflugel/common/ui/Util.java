package com.nurflugel.common.ui;

import org.apache.commons.lang.StringUtils;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;

import java.io.File;

import java.net.URL;

import java.util.Date;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Util class. */
public class Util
{
  // public static final String  OSX_DOT_LOCATION     = "/Applications/Graphviz.app/Contents/MacOS/dot";
  // public static final String  PREVIEW_LOCATION     = "/Applications/Preview.app/Contents/MacOS/Preview";
  // public static final String  WINDOWS_DOT_LOCATION = "\"\\Program Files\\ATT\\Graphviz\\bin\\dot.exe\"";
  public static final Cursor  busyCursor   = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
  public static final Cursor  normalCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
  private static final String SLASH        = "/";
  // -------------------------- STATIC METHODS --------------------------

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

      timeRemainingString = ""                        //
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
    branch = branch.replace("//", SLASH);

    return branch;
  }

  /** Sets the look and feel. */
  @SuppressWarnings({ "UseOfSystemOutOrSystemErr" })
  public static LookAndFeel setLookAndFeel(String feelName, Component component)
  {
    LookAndFeel currentLAF = UIManager.getLookAndFeel();

    try
    {
      UIManager.setLookAndFeel(feelName);
      SwingUtilities.updateComponentTreeUI(component);
    }
    catch (Exception e)
    {
      System.out.println("Error setting native LAF: " + feelName + e.getMessage());
    }

    return currentLAF;
  }

  public static LookAndFeel setLookAndFeel(LookAndFeel lookAndFeel, Component component)
  {
    return setLookAndFeel(lookAndFeel.getName(), component);
  }

  public static void centerApp(Object object)
  {
    if (object instanceof Component)
    {
      Component comp           = (Component) object;
      Toolkit   defaultToolkit = Toolkit.getDefaultToolkit();
      Dimension screenSize     = defaultToolkit.getScreenSize();
      int       x              = (int) ((screenSize.getWidth() - comp.getWidth()) / 2);
      int       y              = (int) ((screenSize.getHeight() - comp.getHeight()) / 2);

      comp.setBounds(x, y, comp.getWidth(), comp.getHeight());
    }
  }

  /** Add the help listener - link to the help files. */
  @SuppressWarnings({ "UseOfSystemOutOrSystemErr" })
  public static void addHelpListener(String helpSetName, JButton helpButton, JFrame theFrame)
  {
    ClassLoader classLoader = theFrame.getClass().getClassLoader();

    try
    {
      URL                       hsURL                 = HelpSet.findHelpSet(classLoader, helpSetName);
      HelpSet                   helpSet               = new HelpSet(null, hsURL);
      HelpBroker                helpBroker            = helpSet.createHelpBroker();
      CSH.DisplayHelpFromSource displayHelpFromSource = new CSH.DisplayHelpFromSource(helpBroker);

      helpButton.addActionListener(displayHelpFromSource);
    }
    catch (HelpSetException ee)
    {  // Say what the exception really is
      System.out.println("Exception! " + ee.getMessage());
      // LOGGER.error("HelpSet " + ee.getMessage());
      // LOGGER.error("HelpSet " + HELP_HS + " not found");
    }
  }

  /** True recursive file deletes - will get rid of this file or dir, and everything in it. */
  @SuppressWarnings({ "ResultOfMethodCallIgnored" })
  public static void rmDirs(File theFile)
  {
    if (theFile.isFile())
    {
      theFile.delete();
    }
    else
    {
      File[] files = theFile.listFiles();

      if (files != null)
      {
        for (File file : files)
        {
          rmDirs(file);
        }
      }

      theFile.delete();
    }
  }

  /** Filters out the http:// in a URL if it exists... also the /svn/ if that's there, too. */
  public static String filterHttp(String url)
  {
    String trimmedUrl = StringUtils.substringAfter(url, "http://");

    trimmedUrl = StringUtils.substringAfter(trimmedUrl, SLASH);  // get text after URL closing /

    if (trimmedUrl.startsWith("svn"))
    {
      trimmedUrl = StringUtils.substringAfter(trimmedUrl, SLASH);
    }

    return trimmedUrl;
  }

  /** Returns the text with no trailing "/". */
  public static String getUrlNoTrailingSlash(String text)
  {
    if (text.endsWith(SLASH))
    {
      return StringUtils.substringBeforeLast(text, SLASH);
    }
    else
    {
      return text;
    }
  }

  private Util() {}
}
