package com.nurflugel.ivybrowser.ui;

import ca.odell.glazedlists.EventList;
import com.nurflugel.common.ui.UiMainFrame;
import com.nurflugel.ivybrowser.AppPreferences;
import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivybrowser.handlers.BaseWebIvyRepositoryBrowserHandler;
import com.nurflugel.ivybrowser.handlers.HtmlHandler;
import com.nurflugel.ivybrowser.handlers.SubversionWebDavHandler;
import com.nurflugel.ivytracker.Config;
import com.nurflugel.ivytracker.IvyTrackerMainFrame;
import com.nurflugel.ivytracker.domain.Project;
import com.nurflugel.ivytracker.handlers.IvyFileFinderHandler;
import com.nurflugel.ivytracker.handlers.SubversionIvyFileFinderHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

/** this is a factory to return the Subversion web dav handler by figuring out which one is which... (Subversion vs. html) */
@SuppressWarnings({ "CallToPrintStackTrace" })
public class HandlerFactory
{
  // -------------------------- STATIC METHODS --------------------------
  public static BaseWebIvyRepositoryBrowserHandler getIvyRepositoryHandler(UiMainFrame ivyBrowserMainFrame, String ivyRepositoryPath,
                                                                           EventList<IvyPackage> repositoryList,
                                                                           Map<String, Map<String, Map<String, IvyPackage>>> packageMap,
                                                                           AppPreferences preferences)
  {
    boolean isSubversionRepository = isSubversionRepository(ivyRepositoryPath);

    if (isSubversionRepository)
    {
      return new SubversionWebDavHandler(ivyBrowserMainFrame, ivyRepositoryPath, repositoryList, packageMap, preferences);
    }
    else
    {
      return new HtmlHandler(ivyBrowserMainFrame, ivyRepositoryPath, repositoryList, packageMap, preferences);
    }
  }

  private static boolean isSubversionRepository(String ivyRepositoryPath)
  {
    try
    {
      URL           repositoryUrl = new URL(ivyRepositoryPath);
      URLConnection urlConnection = repositoryUrl.openConnection();

      urlConnection.setAllowUserInteraction(true);
      urlConnection.connect();

      InputStream    in     = urlConnection.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      String         line   = reader.readLine();

      while (line != null)
      {
        boolean isSubversion = line.contains("Powered by") && line.contains("Subversion");

        if (isSubversion)
        {
          return true;
        }

        line = reader.readLine();
      }

      reader.close();
    }
    catch (UnknownHostException e)
    {
      showMessageDialog(null, "Network error contacting host: " + e.getMessage(), "Unable to parse repository", ERROR_MESSAGE);
      e.printStackTrace();
    }
    catch (IOException e)
    {
      showMessageDialog(null, "Error reaching repository: " + e.getMessage(), "Unable to parse repository", ERROR_MESSAGE);
      e.printStackTrace();
    }

    return false;
  }

  private HandlerFactory() {}

  /** Return a handler which will find all of the Ivy files in the list of repositories. */
  public static IvyFileFinderHandler getIvyFileFinderHandler(IvyTrackerMainFrame mainFrame, Map<Project, List<IvyPackage>> ivyFiles,
                                                             EventList<Project> projectUrls, Config config, String... repositories)
  {
    return new SubversionIvyFileFinderHandler(mainFrame, ivyFiles, projectUrls, config, repositories);
  }
}
