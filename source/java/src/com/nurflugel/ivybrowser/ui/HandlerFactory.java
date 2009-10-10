package com.nurflugel.ivybrowser.ui;

import ca.odell.glazedlists.EventList;
import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivybrowser.handlers.BaseWebHandler;
import com.nurflugel.ivybrowser.handlers.HtmlHandler;
import com.nurflugel.ivybrowser.handlers.SubversionWebDavHandler;
import javax.swing.*;
import static javax.swing.JOptionPane.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

/** todo make this a factory to return the Subversion web dav handler by figuring out which one is which... */
public class HandlerFactory
{
  private HandlerFactory() {}

  public static BaseWebHandler getHandler(IvyBrowserMainFrame ivyBrowserMainFrame, String ivyRepositoryPath, List<IvyPackage> repositoryList,
                                          Map<String, Map<String, Map<String, IvyPackage>>> packageMap)
  {
    boolean isSubversionRepository = isSubversionRepository(ivyRepositoryPath);

    if (isSubversionRepository)
    {
      return new SubversionWebDavHandler(ivyBrowserMainFrame, ivyRepositoryPath, repositoryList, packageMap);
    }
    else
    {
      return new HtmlHandler(ivyBrowserMainFrame, ivyRepositoryPath, repositoryList, packageMap);
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
    }
    catch (IOException e)
    {
      showMessageDialog(null, "Error reaching repository: " + e.getMessage(), "Unable to parse repository", ERROR_MESSAGE);
    }

    return false;
  }
}
