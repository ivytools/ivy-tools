package com.nurflugel.externalsreporter.ui;

import ca.odell.glazedlists.EventList;
import com.nurflugel.WebAuthenticator;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.SVNPropertyData;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import java.io.File;
import java.net.Authenticator;
import java.util.Date;
import static org.tmatesoft.svn.core.SVNURL.parseURIDecoded;
import static org.tmatesoft.svn.core.wc.SVNRevision.HEAD;

/** Handler for all the Subversion tasks. */
@SuppressWarnings({ "CallToPrintStackTrace", "UseOfSystemOutOrSystemErr" })
public class SubversionHandler
{
  public SubversionHandler()
  {
    DAVRepositoryFactory.setup();

    // Authenticator.setDefault(new WebAuthenticator());
  }
  // -------------------------- OTHER METHODS --------------------------

  /** Get any externals used in this URL's project. */
  public void getExternals(String projectBaseUrl, SVNWCClient wcClient, EventList<External> externalsList,
                           EventList<ProjectExternalReference> projectsList, boolean isSelectAllExternals, boolean isSelectAllProjects)
  {
    try
    {
      SVNURL          url             = parseURIDecoded(projectBaseUrl);
      String          propertyName    = "svn:externals";
      long            start           = new Date().getTime();
      SVNPropertyData svnPropertyData = wcClient.doGetProperty(url, propertyName, HEAD, HEAD);  // todo exception here
      long            time            = (new Date().getTime()) - start;

      System.out.println("time to get external = " + (((float) time) / 1000.0f) + " seconds");

      if (svnPropertyData != null)
      {
        System.out.println("buildableUrl = " + projectBaseUrl);

        SVNPropertyValue value     = svnPropertyData.getValue();
        String           textValue = value.toString();
        String[]         values    = textValue.split("\n");

        for (String externalUrl : values)
        {
          String[] externalLine = externalUrl.split(" ");
          String   externalDir  = externalLine[0];

          externalUrl = externalUrl.substring(externalDir.length());
          externalUrl = externalUrl.trim();

          External newExternal = new External(externalUrl, isSelectAllExternals);

          // if the external already is in the list, fetch the existing one
          if (externalsList.contains(newExternal))
          {
            newExternal = externalsList.get(externalsList.indexOf(newExternal));
          }

          ProjectExternalReference newReference = new ProjectExternalReference(projectBaseUrl, externalDir, newExternal, isSelectAllProjects);

          externalsList.getReadWriteLock().writeLock().lock();
          externalsList.add(newExternal);
          externalsList.getReadWriteLock().writeLock().unlock();
          projectsList.getReadWriteLock().writeLock().lock();
          projectsList.add(newReference);
          projectsList.getReadWriteLock().writeLock().unlock();
        }
      }
      else
      {
        System.out.println("Didn't get any externals data for " + url);
      }
    }
    catch (SVNException e)
    {
      System.out.println("Error trying to access " + projectBaseUrl);
      e.printStackTrace();
    }
  }
}
