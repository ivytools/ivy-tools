package com.nurflugel;

import com.nurflugel.common.ui.UiMainFrame;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/** Screen scraper for looking at the projects through the Subversion repository via Apache. */
@SuppressWarnings({ "UseOfSystemOutOrSystemErr", "CallToPrintStackTrace" })
public class HtmlParser
{
  private static final String TRUNK    = "trunk";
  private static final String BRANCHES = "branches";
  // -------------------------- OTHER METHODS --------------------------

  /**
   * See if trunk and any branches exist in the repository.
   *
   * @return  an array of URLs, each pointing to the branch or trunk.
   */
  public List<String> getProjectBuildableUrls(String projectBaseUrl, UiMainFrame mainFrame) throws IOException
  {
    BufferedReader reader  = null;
    List<String>   results = new ArrayList<String>();

    try
    {
      reader = getReader(projectBaseUrl);

      String branch = reader.readLine();

      while (branch != null)
      {
        String link = getLink(branch);

        if (link.equalsIgnoreCase(TRUNK))
        {
          results.add(projectBaseUrl + "/" + TRUNK);
        }
        else
        {
          if (link.equalsIgnoreCase(BRANCHES))
          {
            results.addAll(getBranches(projectBaseUrl + "/" + link));
          }
        }

        branch = reader.readLine();
      }
    }
    finally
    {
      closeReader(reader);
    }

    return results;
  }

  /** Get a buffered reader for a url. */
  private BufferedReader getReader(String projectBaseUrl) throws IOException
  {
    URL           repositoryUrl = new URL(projectBaseUrl);
    URLConnection urlConnection = repositoryUrl.openConnection();

    urlConnection.setAllowUserInteraction(true);
    urlConnection.connect();

    InputStream in = urlConnection.getInputStream();

    return new BufferedReader(new InputStreamReader(in));
  }

  /**
   * Takes text like "
   *
   * <ul>
   *   <li><a href="branches/">branches/</a></li>
   *   <li>" and returns "branches" from it.</li>
   * </ul>
   *
   * @return  the link. Returns an empty value if no link is found
   */
  private String getLink(String text)
  {
    String   result  = "";
    String[] strings = text.split("\"");

    if (strings.length > 2)
    {
      result = strings[1];
      result = result.substring(0, result.length() - 1);
    }

    return result;
  }

  /** Get all the branches for a project's root. */
  private List<String> getBranches(String branchesRoot) throws IOException
  {
    BufferedReader reader  = null;
    List<String>   results = new ArrayList<String>();

    try
    {
      reader = getReader(branchesRoot);

      String branch = reader.readLine();

      while (branch != null)
      {
        if (branch.contains("a href") && !branch.contains(".."))
        {
          String link = getLink(branch);

          if (!link.contains("tigris.org"))
          {
            results.add(branchesRoot + "/" + link);
          }
        }

        branch = reader.readLine();
      }
    }
    finally
    {
      closeReader(reader);
    }

    return results;
  }

  private void closeReader(BufferedReader reader)
  {
    if (reader != null)
    {
      try
      {
        reader.close();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
  }
}
