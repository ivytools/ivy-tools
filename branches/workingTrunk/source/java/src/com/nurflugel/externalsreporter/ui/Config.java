package com.nurflugel.externalsreporter.ui;

import java.io.File;
import java.util.prefs.Preferences;

/** Class to handle configuration persistence for the app. */
public class Config
{
  public static final String  DOT_EXECUTABLE      = "dotExecutable";
  public static final String  IMAGE_DIR           = "imageDir";
  private static final String PASSWORD            = "password";
  private static final String USER_NAME           = "userName";
  private static final String LAST_REPOSITORY     = "lastRepository";
  private static final String TRIM_HTTP_FROM_URLS = "trimHttpFromUrls";
  private static final String SHOW_TRUNKS         = "showTrunks";
  private static final String SHOW_BRANCHES       = "showBranches";
  private static final String SHOW_TAGS           = "showTags";
  private Preferences         preferences;
  private String              dotExecutablePath;
  private String              imageDir;
  private String              password;
  private String              userName;
  private String              lastRepository;
  private boolean             trimHttpFromUrls;
  private boolean             showBranches;
  private boolean             showTrunks;
  private boolean             showTags;

  /** Todo how to deal with changed or wrong passwords? */
  public Config()
  {
    preferences       = Preferences.userNodeForPackage(MainFrame.class);
    dotExecutablePath = preferences.get(DOT_EXECUTABLE, "");
    imageDir          = preferences.get(IMAGE_DIR, "");
    lastRepository    = preferences.get(LAST_REPOSITORY, "");
    userName          = preferences.get(USER_NAME, "");
    password          = preferences.get(PASSWORD, "");
    trimHttpFromUrls  = preferences.getBoolean(TRIM_HTTP_FROM_URLS, true);
    showTrunks        = preferences.getBoolean(SHOW_TRUNKS, true);
    showBranches      = preferences.getBoolean(SHOW_BRANCHES, false);
    showTags          = preferences.getBoolean(SHOW_TAGS, false);
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

  public String getLastRepository()
  {
    return lastRepository;
  }

  public String getPassword()
  {
    return password;
  }

  public String getUserName()
  {
    return userName;
  }

  public boolean isShowBranches()
  {
    return showBranches;
  }

  public boolean isShowTags()
  {
    return showTags;
  }

  public boolean isShowTrunks()
  {
    return showTrunks;
  }

  public boolean isTrimHttpFromUrls()
  {
    return trimHttpFromUrls;
  }

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

  public void setLastRepository(String lastRepository)
  {
    this.lastRepository = lastRepository;
    saveSettings();
  }

  public void setPassword(String password)
  {
    this.password = password;
    saveSettings();
  }

  public void setShowBranches(boolean showBranches)
  {
    this.showBranches = showBranches;
    saveSettings();
  }

  public void saveSettings()
  {
    preferences.put(DOT_EXECUTABLE, dotExecutablePath);
    preferences.put(IMAGE_DIR, imageDir);
    preferences.putBoolean(TRIM_HTTP_FROM_URLS, trimHttpFromUrls);
    preferences.putBoolean(SHOW_BRANCHES, showBranches);
    preferences.putBoolean(SHOW_TRUNKS, showTrunks);
    preferences.putBoolean(SHOW_TAGS, showTags);

    saveNonNullValue(password, PASSWORD);
    saveNonNullValue(userName, USER_NAME);
    saveNonNullValue(lastRepository, LAST_REPOSITORY);
  }

  /** Only save the value to preferences if it's not null. */
  private void saveNonNullValue(String value, String key)
  {
    if (value != null)
    {
      preferences.put(key, value);
    }
  }

  public void setShowTags(boolean showTags)
  {
    this.showTags = showTags;
    saveSettings();
  }

  public void setShowTrunks(boolean showTrunks)
  {
    this.showTrunks = showTrunks;
    saveSettings();
  }

  public void setTrimHttpFromUrls(boolean trimHttpFromUrls)
  {
    this.trimHttpFromUrls = trimHttpFromUrls;
    saveSettings();
  }

  public void setUserName(String userName)
  {
    this.userName = userName;
    saveSettings();
  }
}
