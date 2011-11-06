package com.nurflugel.ivybrowser;

import static com.nurflugel.common.ui.FindMultiplePreferencesItemsDialog.EMPTY_STRING;
import static com.nurflugel.ivybrowser.ui.IvyBrowserMainFrame.IVY_REPOSITORY;
import com.nurflugel.ivybrowser.ui.IvyBrowserMainFrame;
import java.util.List;
import java.util.prefs.Preferences;

/** Created by IntelliJ IDEA. User: douglas_bullard Date: 9/13/11 Time: 19:24 To change this template use File | Settings | File Templates. */
public class AppPreferences
{
  private static final String PARSE_ON_OPEN = "parseOnOpen";
  private static final String SAVE_DIR      = "saveDir";
  private Preferences         preferences;

  public AppPreferences(Class theClass)
  {
    preferences = Preferences.userNodeForPackage(theClass);
  }

  public AppPreferences(Preferences preferences)
  {
    this.preferences = preferences;
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

  public void saveParseOnOpen(boolean isSelected)
  {
    preferences.putBoolean(PARSE_ON_OPEN, isSelected);
  }

  public void setPreferredSaveDir(String dir)
  {
    preferences.put(SAVE_DIR, dir);
  }

  public String getDefaultRepository()
  {
    return getIndexedProperty(IVY_REPOSITORY, 0);
  }

  public void saveIndexedProperties(String keyBase, List<String> locations)
  {
    for (int i = 0; i < locations.size(); i++)
    {
      preferences.put(keyBase + i, locations.get(i));
    }
  }
}
