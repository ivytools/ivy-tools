package com.nike.buildmaster.ui;

import com.nike.externalsreporter.ui.MainFrame;

import java.io.File;
import java.util.prefs.Preferences;


/** Wrapper for preferences. */
public class Config
{
    public static final String TAG_SNAPSHOTS_DIR = "tagSnapshotsDir";
    private Preferences preferences;
    private String tagSnapshotsDir;

    public Config()
    {
        preferences = Preferences.userNodeForPackage(MainFrame.class);
        tagSnapshotsDir = preferences.get(TAG_SNAPSHOTS_DIR, "");
    }

    public void saveSettings()
    {
        preferences.put(TAG_SNAPSHOTS_DIR, tagSnapshotsDir);
    }


    public File getTagSnapshotsDir()
    {
        return new File(tagSnapshotsDir);
    }

    public void setTagSnapshotsDir(File tagSnapshotsDir)
    {

        if (tagSnapshotsDir != null)
        {
            this.tagSnapshotsDir = tagSnapshotsDir.getAbsolutePath();
        }
    }
}
