package com.nike.common.ui;

import com.nike.buildmaster.Os;

/** Interface so several projects can share some functionality. */
public interface UiMainFrame
{

    /**
     * Takes the list of build items, and shows the list for confirmation, and lets the user change the suggested tag names.
     *
     * @return the list of confirmed items to build
     */
//    private List<BuildableItem> confirmBuildInformation(List<BuildableItem> checkedTargets)
//    {
//        BuildConfirmationDialog confirmationDialog = new BuildConfirmationDialog(checkedTargets);
//
//
//        if (confirmationDialog.cancelBuild())
//        {
//            System.out.println("Build is being cancelled");
//            System.exit(0);
//        }
//        else
//        {
//            return confirmationDialog.getCheckedTargets();
//        }
//
//        return null;
//    }

    void addStatus(String statusLine);

    /**  */
    Os getOs();

    void initializeStatusBar(int minimum, int maximum, int initialValue, boolean visible);

    boolean isTest();

    boolean getTestDataFromFile();

    void setBusyCursor();

    void setNormalCursor();

    void setReady(boolean isReady);

    void showSevereError(String message, Exception e);
}
