package com.nike.buildmaster.handlers;

import com.nike.buildmaster.WebAuthenticator;
import com.nike.buildmaster.projects.Branch;
import com.nike.buildmaster.projects.BuildableProjects;
import com.nike.buildmaster.ui.MainFrame;
import com.nike.buildmaster.ui.buildtree.BuildableItem;
import com.nike.externalsreporter.ui.tree.ProjectBranchItem;
import org.jdesktop.swingworker.SwingWorker;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.wc.*;
import static org.tmatesoft.svn.core.wc.SVNRevision.HEAD;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;


/** Class to do checkouts of Subversion tags in the backgroudn */
@SuppressWarnings({"CallToPrintStackTrace", "UseOfSystemOutOrSystemErr"})
public class SubversionCheckoutTask extends SwingWorker<Object, Object>
{
    private SVNClientManager ourClientManager;
    private File snapshotRoot;
    private MainFrame mainFrame;
    private SVNURL svnurl;
    private List<BuildableItem> buildableItems;
    private ProjectBranchItem projectBranch;
    private Map<ProjectBranchItem, List<BuildableItem>> buildMap;

    public SubversionCheckoutTask(File snapshotRoot,
                                  MainFrame mainFrame,
                                  SVNURL svnurl,
                                  List<BuildableItem> buildableItems, ProjectBranchItem projectBranch,
                                  Map<ProjectBranchItem, List<BuildableItem>> buildMap)
    {
        this.snapshotRoot = snapshotRoot;
        this.mainFrame = mainFrame;
        this.svnurl = svnurl;
        this.buildableItems = buildableItems;
        this.projectBranch = projectBranch;
        this.buildMap = buildMap;
        DAVRepositoryFactory.setup();

        /*
         * Creates a default run-time configuration options driver. Default options created in this way use the Subversion run-time configuration area (for instance, on a Windows platform it can be found in the '%APPDATA%\Subversion' directory).
         *
         * readonly = true - not to save  any configuration changes that can be done during the program run to a config file (config settings will only be read to initialize; to enable changes the readonly flag should be set to false).
         *
         * SVNWCUtil is a utility class that creates a default options driver.
         */
        ISVNOptions options = SVNWCUtil.createDefaultOptions(true);

        /*
         * Creates an instance of SVNClientManager providing authentication information (name, password) and an options driver
         */
        ourClientManager = null;//todo fix for SVN 1.5!!!
//        ourClientManager = SVNClientManager.newInstance(options, WebAuthenticator.getUsername(), WebAuthenticator.getPassword());

    }

    /** Creates a local directory where the working copy will be checked out into. */
    @Override
    public Object doInBackground() throws Exception
    {
        mainFrame.enableGoButton(false);
        long start = new Date().getTime();

        File wcDir = new File(snapshotRoot, getFilePathFromUrl(svnurl)); // should have full name like "claims/tagname
        if (wcDir.exists())
        {
            error("the destination directory '" + wcDir.getAbsolutePath() + "' already exists!", null);
        }

        wcDir.mkdirs();

        mainFrame.addStatus("Checking out a working copy from '" + svnurl + "' to " + wcDir.getAbsolutePath() + "...");
        try
        {
            checkout(svnurl, HEAD, wcDir, true);
        }
        catch (SVNException svne)
        {
            error("Error while checking out a working copy for the location '" + svnurl + "'", svne);
        }

        long duration = new Date().getTime() - start;
        long minutes = duration / 1000 / 60;
        long seconds = (duration - (minutes * 1000 * 60)) / 1000;
        mainFrame.addStatus("Done with chekout, duration: " + minutes + " minutes " + seconds + " seconds");
        doBuildsForProjectBranch(buildableItems, wcDir, projectBranch, buildMap);

        return null;
    }

    /** Trigger the actual builds */
    public void doBuildsForProjectBranch(List<BuildableItem> checkedTargets, File tagRoot, ProjectBranchItem projectBranch,
                                         Map<ProjectBranchItem, List<BuildableItem>> buildMap)
    {
        //todo use map the right way!!
        for (BuildableItem target : checkedTargets)
        {
            Branch targetBranch = target.getBranch();
            Branch pBranch = projectBranch.getBranch();
            BuildableProjects targetProject = target.getProject();
            BuildableProjects branchProject = projectBranch.getProject();
            if (targetProject == branchProject && targetBranch.equals(pBranch))
            {
                try
                {
                    BuildHandler buildHandler = new BuildHandler(tagRoot, target, mainFrame);

//                    if (isHeadlessBuild)
//                    {
                    buildHandler.doInBackground();
//                    }
//                    else
//                    {
//                        buildHandler.execute();
//                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    mainFrame.addStatus(e.getMessage());

                    StackTraceElement[] stackTrace = e.getStackTrace();

                    for (StackTraceElement stackTraceElement : stackTrace)
                    {
                        mainFrame.addStatus(stackTraceElement.toString());
                    }

                    mainFrame.addStatus("");
                }
            }
        }

        mainFrame.enableGoButton(true);
    }

    /*
     * Checks out a working copy from a repository. Like 'svn checkout URL[@REV] PATH (-r..)' command; It's done by invoking
     *
     * SVNUpdateClient.doCheckout(SVNURL url, File dstPath, SVNRevision pegRevision, SVNRevision revision, boolean recursive)
     *
     * which takes the following parameters:
     *
     * url - a repository location from where a working copy is to be checked out;
     *
     * dstPath - a local path where the working copy will be fetched into;
     *
     * pegRevision - an SVNRevision representing a revision to concretize url (what exactly URL a user means and is sure of being the URL he needs); in other words that is the revision in which the URL is first looked up;
     *
     * revision - a revision at which a working copy being checked out is to be;
     *
     * recursive - if true and url corresponds to a directory then doCheckout(..) recursively fetches out the entire directory, otherwise - only child entries of the directory;
     */
    private long checkout(SVNURL url,
                          SVNRevision revision,
                          File destPath,
                          boolean isRecursive) throws SVNException
    {

        SVNUpdateClient updateClient = ourClientManager.getUpdateClient();

        // sets externals not to be ignored during the checkout
        updateClient.setIgnoreExternals(false);

        // returns the number of the revision at which the working copy is
        return updateClient.doCheckout(url, destPath, revision, revision, isRecursive);
    }


    /*
     * Displays error information and exits.
     */
    private void error(String message,
                       Exception e)
    {
        String text = message + ((e != null)
                                 ? (": " + e.getMessage())
                                 : "");
        System.err.println(text);
        mainFrame.addStatus(text);
    }


    /**
     * Get the file path from the URL - that is, if the tag url is: http://subversion/svn/cdm/tags/2007_05_30_12_25_30, then return
     * cdm/tags/2007_05_30_12_25_30
     */
    private String getFilePathFromUrl(SVNURL url)
    {
        String respositoryRoot = "svn/";
        String svnpath = url.getPath();
        String filePath = svnpath.substring(respositoryRoot.length() + 1);

        return filePath;
    }


}
