package com.nurflugel.ivytracker;

import com.nurflugel.externalsreporter.ui.UserConfig;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/** Class to handle configuration persistence for the app. */
public class Config implements UserConfig
{
  private static final String PASSWORD                   = "password";
  private static final String USER_NAME                  = "userName";
  static final String         LAST_IVY_REPOSITORY        = "lastIvyRepository";
  static final String         LAST_SUBVERSION_REPOSITORY = "lastSubversionRepository";
  private static final String SHOW_TRUNKS                = "showTrunks";
  private static final String SHOW_BRANCHES              = "showBranches";
  private static final String SHOW_TAGS                  = "showTags";
  private static final String RECURSE_DIRS               = "recurseDirs";
  private Preferences         preferences;
  private String              password;
  private String              userName;
  private List<String>        ivyRepositories            = new ArrayList<String>();
  private List<String>        subversionRepositories     = new ArrayList<String>();
  private boolean             showBranches;
  private boolean             showTrunks;
  private boolean             showTags;
  private boolean             recurseDirs;
  private static final String EMPTY_STRING               = "";

  /** Todo how to deal with changed or wrong passwords? */
  public Config()
  {
    preferences = Preferences.userNodeForPackage(IvyTrackerMainFrame.class);
    getRepositories(LAST_IVY_REPOSITORY, ivyRepositories);
    getRepositories(LAST_SUBVERSION_REPOSITORY, subversionRepositories);
    userName     = preferences.get(USER_NAME, EMPTY_STRING);
    password     = preferences.get(PASSWORD, EMPTY_STRING);
    showTrunks   = preferences.getBoolean(SHOW_TRUNKS, true);
    showBranches = preferences.getBoolean(SHOW_BRANCHES, true);
    showTags     = preferences.getBoolean(SHOW_TAGS, true);
    recurseDirs  = preferences.getBoolean(RECURSE_DIRS, false);
  }

  private void getRepositories(String repository, List<String> repositoryList)
  {
    for (int i = 0; i < 10; i++)
    {
      String key   = repository + i;
      String value = preferences.get(key, EMPTY_STRING);

      if (isEmpty(value))
      {
        break;
      }
      else
      {
        repositoryList.add(value);
      }
    }
  }

  // -------------------------- OTHER METHODS --------------------------
  public String getLastIvyRepository()
  {
    return ivyRepositories.isEmpty() ? EMPTY_STRING
                                     : ivyRepositories.get(ivyRepositories.size() - 1);
  }

  public String getLastSubversionRepository()
  {
    return subversionRepositories.isEmpty() ? EMPTY_STRING
                                            : subversionRepositories.get(subversionRepositories.size() - 1);
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

  public boolean showBranches()
  {
    return showBranches;
  }

  public boolean showTags()
  {
    return showTags;
  }

  public boolean showTrunks()
  {
    return showTrunks;
  }

  public boolean recurseDirs()
  {
    return recurseDirs;
  }

  public void setLastIvyRepository(String lastRepository)
  {
    if (ivyRepositories.contains(lastRepository))
    {
      ivyRepositories.remove(lastRepository);
    }

    ivyRepositories.add(lastRepository);
    saveSettings();
  }

  public void setLastSubversionRepository(String lastRepository)
  {
    if (subversionRepositories.contains(lastRepository))
    {
      subversionRepositories.remove(lastRepository);
    }

    subversionRepositories.add(lastRepository);
    saveSettings();
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
    preferences.putBoolean(SHOW_BRANCHES, showBranches);
    preferences.putBoolean(SHOW_TRUNKS, showTrunks);
    preferences.putBoolean(SHOW_TAGS, showTags);
    preferences.putBoolean(RECURSE_DIRS, recurseDirs);
    saveNonNullValue(password, PASSWORD);
    saveNonNullValue(userName, USER_NAME);
    saveRepositories(LAST_IVY_REPOSITORY, ivyRepositories);
    saveRepositories(LAST_SUBVERSION_REPOSITORY, subversionRepositories);
  }

  /** Save only the 10 elements in the list. */
  private void saveRepositories(String repository, List<String> repositoryList)
  {
    int beginIndex = Math.max(0, repositoryList.size() - 10);  // get either the 0th element or the length -10th

    if (beginIndex >= 0)
    {
      for (int i = 0; i < repositoryList.size(); i++)
      {
        String key   = repository + i;
        String value = repositoryList.get(i + beginIndex);

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

  public void setRecurseDirs(boolean recurseDirs)
  {
    this.recurseDirs = recurseDirs;
    saveSettings();
  }

  public void setShowTrunks(boolean showTrunks)
  {
    this.showTrunks = showTrunks;
    saveSettings();
  }

  @Override
  public void setUserName(String userName)
  {
    this.userName = userName;
    saveSettings();
  }

  public Preferences getPreferences()
  {
    return preferences;
  }
}
