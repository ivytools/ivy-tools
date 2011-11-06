package com.nurflugel.externalsreporter.ui;

import com.nurflugel.WebAuthenticator;
import static javax.swing.JOptionPane.showInputDialog;
import static org.apache.commons.lang.StringUtils.isEmpty;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;

/** Class to handle configuration persistence for the app. */
public class Config implements UserConfig
{
  public static final String  DOT_EXECUTABLE      = "dotExecutable";
  public static final String  IMAGE_DIR           = "imageDir";
  private static final String PASSWORD            = "password";
  private static final String USER_NAME           = "userName";
  static final String         REPOSITORY          = "lastRepository";
  private static final String TRIM_HTTP_FROM_URLS = "trimHttpFromUrls";
  private static final String SHOW_TRUNKS         = "showTrunks";
  private static final String SHOW_BRANCHES       = "showBranches";
  private static final String SHOW_TAGS           = "showTags";
  private static final String SHALLOW_SCAN        = "shallowScan";
  private Preferences         preferences;
  private String              dotExecutablePath;
  private String              imageDir;
  private String              password;
  private String              userName;
  private List<String>        repositories        = new ArrayList<String>();
  private boolean             trimHttpFromUrls;
  private boolean             showBranches;
  private boolean             showTrunks;
  private boolean             showTags;
  private boolean             shallowScan;
  private static final String EMPTY_STRING        = "";

  /** Todo how to deal with changed or wrong passwords? */
  public Config()
  {
    preferences       = Preferences.userNodeForPackage(ExternalsFinderMainFrame.class);
    dotExecutablePath = preferences.get(DOT_EXECUTABLE, EMPTY_STRING);
    imageDir          = preferences.get(IMAGE_DIR, EMPTY_STRING);
    getRepositories();
    userName         = preferences.get(USER_NAME, EMPTY_STRING);
    password         = showInputDialog("Enter the password for " + userName
                                         + " (it's in clear text\n because if you get it wrong, it'll lock your account)");
    trimHttpFromUrls = preferences.getBoolean(TRIM_HTTP_FROM_URLS, true);
    showTrunks       = preferences.getBoolean(SHOW_TRUNKS, true);
    showBranches     = preferences.getBoolean(SHOW_BRANCHES, false);
    showTags         = preferences.getBoolean(SHOW_TAGS, false);
    shallowScan      = preferences.getBoolean(SHALLOW_SCAN, true);
  }

  private void getRepositories()
  {
    for (int i = 0; i < 10; i++)
    {
      String key   = REPOSITORY + i;
      String value = preferences.get(key, EMPTY_STRING);

      if (isEmpty(value))
      {
        break;
      }
      else
      {
        repositories.add(value);
      }
    }
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
    return repositories.isEmpty() ? EMPTY_STRING
                                  : repositories.get(repositories.size() - 1);
  }

  @Override
  public String getPassword()
  {
    return password;
  }

  @Override
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
    if (!repositories.contains(lastRepository))
    {
      repositories.add(lastRepository);
      saveSettings();
    }
  }

  @Override
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
    preferences.putBoolean(SHALLOW_SCAN, shallowScan);
    saveNonNullValue(password, PASSWORD);
    saveNonNullValue(userName, USER_NAME);
    saveRepositories();
  }

  /** Save only the 10 elements in the list. */
  private void saveRepositories()
  {
    int beginIndex = Math.max(0, repositories.size() - 10);  // get either the 0th element or the length -10th

    if (beginIndex >= 0)
    {
      for (int i = 0; i < repositories.size(); i++)
      {
        String key   = REPOSITORY + i;
        String value = repositories.get(i + beginIndex);

        if (!isEmpty(value))
        {
          preferences.put(key, value);
        }
      }
    }
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

  @Override
  public void setUserName(String userName)
  {
    this.userName = userName;
    saveSettings();
  }

  public void setShallowScan(boolean selected)
  {
    shallowScan = selected;
  }

  public boolean isShallowScan()
  {
    return shallowScan;
  }

  public Preferences getPreferences()
  {
    return preferences;
  }
}
