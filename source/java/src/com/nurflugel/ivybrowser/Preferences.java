package com.nurflugel.ivybrowser;

import static com.nurflugel.common.ui.FindMultiplePreferencesItemsDialog.EMPTY_STRING;

import com.nurflugel.ivybrowser.ui.IvyBrowserMainFrame;

/** Created by IntelliJ IDEA. User: douglas_bullard Date: 9/13/11 Time: 19:24 To change this template use File | Settings | File Templates. */
public class Preferences
{
  private static final String         PARSE_ON_OPEN = "parseOnOpen";
  private static final String         SAVE_DIR      = "saveDir";
  private java.util.prefs.Preferences preferences;

  public Preferences(IvyBrowserMainFrame ivyBrowserMainFrame)
  {
    preferences = java.util.prefs.Preferences.userNodeForPackage(ivyBrowserMainFrame.getClass());
  }

  // -------------------------- OTHER METHODS --------------------------
  public String getIndexedProperty(String property, int index)
  {
    return preferences.get(property + index, EMPTY_STRING);
  }

  public boolean getParseOnOpen()
  {
    return preferences.getBoolean(PARSE_ON_OPEN, false);
  }

  public String getPreferredSaveDir()
  {
    return preferences.get(SAVE_DIR, null);
  }

  public void savaeParseOnOpen(boolean isSelected)
  {
    preferences.putBoolean(PARSE_ON_OPEN, isSelected);
  }

  public void setPreferredSaveDir(String dir)
  {
    preferences.put(SAVE_DIR, dir);
  }

  public void saveIndexedProperty(String keyBase, int index, String value)
  {
    preferences.put(keyBase + index, value);
  }
}
