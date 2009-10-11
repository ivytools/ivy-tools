package com.nurflugel.externalsreporter.ui;

import com.nurflugel.BuildableItem;
import com.nurflugel.common.ui.UiMainFrame;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.wc.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Handler for all the Subversion tasks. */
@SuppressWarnings({ "CallToPrintStackTrace", "UseOfSystemOutOrSystemErr" })
public class SubversionHandler
{
  private SVNClientManager clientManager;
  private UiMainFrame      mainFrame;

  public SubversionHandler(UiMainFrame mainFrame)
  {
    this.mainFrame = mainFrame;
    DAVRepositoryFactory.setup();

    /*
     * Creates a default run-time configuration options driver. Default options created in this way use the Subversion run-time configuration area
     * (for instance, on a Windows platform it can be found in the '%APPDATA%\Subversion' directory).
     *
     * readonly = true - not to save  any configuration changes that can be done during the program run to a config file (config settings will only be
     * read to initialize; to enable changes the readonly flag should be set to false).
     *
     * SVNWCUtil is a utility class that creates a default options driver.
     */
    ISVNOptions options = SVNWCUtil.createDefaultOptions(true);

    /*
     * Creates an instance of SVNClientManager providing authentication information (name, password) and an options driver
     */
    clientManager = null;  // todo fix for SVN 1.5!!!

    // clientManager = SVNClientManager.newInstance(options, WebAuthenticator.getUsername(), WebAuthenticator.getPassword());
  }

  // -------------------------- OTHER METHODS --------------------------

  /*
   * Duplicates srcURL to dstURL (URL->URL)in a repository remembering history. Like 'svn copy srcURL dstURL -m "some comment"' command. It's done by
   * invoking
   *
   * doCopy(SVNURL srcURL, SVNRevision srcRevision, SVNURL dstURL, boolean isMove, String commitMessage)
   *
   * which takes the following parameters:
   *
   * srcURL - a source URL that is to be copied;
   *
   * srcRevision - a definite revision of srcURL
   *
   * dstURL - a URL where srcURL will be copied; if srcURL & dstURL are both directories then there are two cases: a) dstURL already exists - then
   * doCopy(..) will duplicate the entire source directory and put it inside dstURL (for example, consider srcURL = svn://localhost/rep/MyRepos,
   * dstURL = svn://localhost/rep/MyReposCopy, in this case if doCopy(..) succeeds MyRepos will be in MyReposCopy -
   * svn://localhost/rep/MyReposCopy/MyRepos); b) dstURL doesn't exist yet - then doCopy(..) will create a directory and recursively copy entries from
   * srcURL into dstURL (for example, consider the same srcURL = svn://localhost/rep/MyRepos, dstURL = svn://localhost/rep/MyReposCopy, in this case
   * if doCopy(..) succeeds MyRepos entries will be in MyReposCopy, like: svn://localhost/rep/MyRepos/Dir1 ->
   * svn://localhost/rep/MyReposCopy/Dir1...);
   *
   * isMove - if false then srcURL is only copied to dstURL what corresponds to 'svn copy srcURL dstURL -m "some comment"'; but if it's true then
   * srcURL
   * will be copied and deleted - 'svn move srcURL dstURL -m "some comment"';
   *
   * commitMessage - a commit log message since URL->URL copying is immediately committed to a repository.
   */
  private SVNCommitInfo copy(SVNURL srcURL, SVNURL dstURL, String commitMessage) throws SVNException
  {
    // SVNRevision.HEAD means the latest revision. Returns SVNCommitInfo containing information on the new revision committed (revision number, etc.)
    boolean failWhenDestExists = true;
    boolean isMove             = false;

    // return clientManager.getCopyClient().doCopy(srcURL, HEAD, dstURL, isMove, failWhenDestExists, commitMessage);
    return null;  // todo fix for SVN 1.5!!!
  }

  /** Get any externals used in this URL's project. */
  public List<External> getExternals(String buildableUrl, SVNWCClient wcClient) throws SVNException
  {
    List<External>  theMap          = new ArrayList<External>();
    SVNURL          url             = SVNURL.parseURIDecoded(buildableUrl);
    String          propertyName    = "svn:externals";
    long            start           = new Date().getTime();
    SVNPropertyData svnPropertyData = null;  // todo fix for SVN 1.5!!!

    // SVNPropertyData svnPropertyData = wcClient.doGetProperty(url, propertyName, HEAD, HEAD, false);
    long time = (new Date().getTime()) - start;

    System.out.println("time to get external = " + (((float) time) / 1000.0f) + " seconds");

    if (svnPropertyData != null)
    {
      System.out.println("buildableUrl = " + buildableUrl);
      // todo fix for SVN 1.5!!!
      // String value = svnPropertyData.getValue();
      // String[] values = value.split("\n");
      //
      // for (String line : values)
      // {
      // String[] externalLine = line.split(" ");
      // String dir = externalLine[0];
      // line = line.substring(dir.length());
      // line = line.substring(line.lastIndexOf(" ") + 1);
      //
      // External external = new External(dir, line);
      // theMap.add(external);
      // }
    }
    else
    {
      System.out.println("Didn't get any externals data for " + url);
    }

    return theMap;
  }

  public SVNWCClient getWcClient()
  {
    return clientManager.getWCClient();
  }

  /** Make a tag of the buildable item. */
  public SVNURL makeTag(BuildableItem item)
  {
    String baseUrl = item.getProject().getProjectBaseUrl();
    long   start   = new Date().getTime();

    try
    {
      SVNURL repositryUrl = SVNURL.parseURIEncoded(baseUrl);
      SVNURL fromUrl      = repositryUrl.appendPath(item.getBranch().getPath(), false);
      SVNURL tagUrl       = repositryUrl.appendPath("tags/" + item.getBranch().getTagName(), false);

      try
      {
        long committedRevision = copy(fromUrl, tagUrl, "tagging '" + fromUrl + "' to '" + tagUrl + "'").getNewRevision();
        long duration          = new Date().getTime() - start;

        if (mainFrame != null)
        {
          mainFrame.addStatus("Created a tag: " + tagUrl + " with revision " + committedRevision + ",  duration: " + (duration / 1000) + " seconds");
        }

        return tagUrl;
      }
      catch (SVNException e)
      {
        String message = "Error making tag\n" + e.getErrorMessage();

        mainFrame.showSevereError(message, e);

        return tagUrl;
      }
    }
    catch (SVNException e)
    {
      String message = "Error parsing URLs\n" + e.getErrorMessage();

      mainFrame.showSevereError(message, e);
    }

    return null;
  }
}
