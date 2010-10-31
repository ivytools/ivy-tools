package com.nurflugel.common.ui;

import com.nurflugel.Os;

/** Interface so several projects can share some functionality. */
public interface UiMainFrame
{
  // -------------------------- OTHER METHODS --------------------------
  void addStatus(String statusLine);
  Os getOs();
  boolean getTestDataFromFile();
  void initializeStatusBar(int minimum, int maximum, int initialValue, boolean visible);
  boolean isTest();
  void setBusyCursor();
  void setNormalCursor();

  /** everthing is ready for user input. */
  void setReady(boolean isReady);
  void showSevereError(String message, Exception e);
  void stopThreads();
  void setStatusLabel(String text);
  void stopProgressPanel();

  /** Resize any table columns needed. */
  void resizeTableColumns();
}
