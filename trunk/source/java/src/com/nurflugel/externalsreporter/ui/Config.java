package com.nurflugel.externalsreporter.ui;

import java.io.File;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jun 21, 2008 Time: 6:50:22 PM To change this template use File | Settings | File Templates.
 */
public class Config
{
  public static final String  DOT_EXECUTABLE    = "dotExecutable";
  public static final String  IMAGE_DIR         = "imageDir";
  private static final String VERSION           = "version";
  private Preferences         preferences;
  private String              dotExecutablePath;
  private String              imageDir;

  public Config()
  {
    preferences       = Preferences.userNodeForPackage(MainFrame.class);
    dotExecutablePath = preferences.get(DOT_EXECUTABLE, "");
    imageDir          = preferences.get(IMAGE_DIR, "");
  }

  public void saveSettings()
  {
    preferences.put(DOT_EXECUTABLE, dotExecutablePath);
    preferences.put(IMAGE_DIR, imageDir);
  }

  public File getDotExecutablePath()
  {
    return new File(dotExecutablePath);
  }

  public void setDotExecutablePath(String dotExecutablePath)
  {
    this.dotExecutablePath = dotExecutablePath;
  }

  public File getImageDir()
  {
    return new File(imageDir);
  }

  public void setImageDir(File imageDir)
  {
    if (imageDir != null)
    {
      this.imageDir = imageDir.getAbsolutePath();
    }
  }
}
