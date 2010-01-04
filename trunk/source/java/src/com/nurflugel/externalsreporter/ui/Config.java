package com.nurflugel.externalsreporter.ui;

import java.io.File;
import java.util.prefs.Preferences;

/** Class to handle configuration persistence for the app. */
public class Config
{
  public static final String  DOT_EXECUTABLE    = "dotExecutable";
  public static final String  IMAGE_DIR         = "imageDir";
  private static final String PASSWORD          = "password";
  private static final String USER_NAME         = "userName";
  private Preferences         preferences;
  private String              dotExecutablePath;
  private String              imageDir;
  private String              password;
  private String              userName;
  private String              lastRepository;
  private static final String LAST_REPOSITORY   = "lastRepository";

  /** Todo how to deal with changed or wrong passwords? */
  public Config()
  {
    preferences       = Preferences.userNodeForPackage(MainFrame.class);
    dotExecutablePath = preferences.get(DOT_EXECUTABLE, "");
    imageDir          = preferences.get(IMAGE_DIR, "");
    lastRepository    = preferences.get(LAST_REPOSITORY, "");
    userName          = preferences.get(USER_NAME, "");
    password          = preferences.get(PASSWORD, "");
  }

  // -------------------------- OTHER METHODS --------------------------

  public File getDotExecutablePath()
  {
    return new File(dotExecutablePath);
  }

  public File getImageDir()
  {
    return new File(imageDir);
  }

  public void saveSettings()
  {
    preferences.put(DOT_EXECUTABLE, dotExecutablePath);
    preferences.put(IMAGE_DIR, imageDir);

    saveNonNullValue(password, PASSWORD);
    saveNonNullValue(userName, USER_NAME);
    saveNonNullValue(lastRepository, LAST_REPOSITORY);
  }

  /** Only save the value to preferences if it's not null. */
  private void saveNonNullValue(String value, String key)
  {
    if (value != null)
    {
      System.out.println("Saving key " + key + "with value = " + value);
      preferences.put(key, value);
    }
  }

  // --------------------- GETTER / SETTER METHODS ---------------------

  public void setDotExecutablePath(String dotExecutablePath)
  {
    this.dotExecutablePath = dotExecutablePath;
    saveSettings();
  }

  public void setImageDir(File imageDir)
  {
    if (imageDir != null)
    {
      this.imageDir = imageDir.getAbsolutePath();
      saveSettings();
    }
  }

  public void setPassword(String password)
  {
    this.password = password;
    saveSettings();
  }

  public void setUserName(String userName)
  {
    this.userName = userName;
    saveSettings();
  }

  public String getPassword()
  {
    return password;
  }

  public String getUserName()
  {
    return userName;
  }

  public String getLastRepository()
  {
    return lastRepository;
  }

  public void setLastRepository(String lastRepository)
  {
    this.lastRepository = lastRepository;
    saveSettings();
  }
}
