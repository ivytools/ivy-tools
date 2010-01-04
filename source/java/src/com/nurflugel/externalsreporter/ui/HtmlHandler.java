package com.nurflugel.externalsreporter.ui;

import org.apache.commons.lang.StringUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/** Class to parse the URL for files and such. */
public class HtmlHandler
{
  // -------------------------- OTHER METHODS --------------------------

  public List<String> getFiles(String repositoryUrl) throws IOException
  {
    List<String>  files         = new ArrayList<String>();
    URL           versionUrl    = new URL(repositoryUrl);
    URLConnection urlConnection = versionUrl.openConnection();

    // Authenticator.setDefault(new WebAuthenticator());

    urlConnection.setAllowUserInteraction(true);
    urlConnection.connect();

    try
    {
      InputStream    in     = urlConnection.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      String         line   = reader.readLine();

      while (line != null)
      {
        if (!line.contains("Parent Directory"))
        {
          // mainFrame.setStatusLabel("Parsing " + repositoryUrl + " for " + moduleName + " version " + version);
          // System.out.println("line = " + line);

          if (hasLinkText(line))
          {
            String link = getLink(line);

            if (isLinkADir(link))
            {  // todo trim trailing slash???
              files.add(repositoryUrl + link);
            }
          }
        }

        line = reader.readLine();
      }

      reader.close();
    }
    catch (IOException e)
    {
      System.out.println("Error contacting server at URL " + versionUrl + " " + e.getMessage());
    }

    return files;
  }

  /** returns true if the line has a link in it. */
  private boolean hasLinkText(String line)
  {
    if (line.contains("http") || line.contains(".."))
    {
      return false;
    }

    return (line.toUpperCase().contains("A HREF"));
  }

  /** Get the link text (the first one). */
  private String getLink(String line)
  {
    String linkText = StringUtils.substringAfter(line, "a href=\"");

    linkText = StringUtils.substringBefore(linkText, "\"");

    return linkText;
  }

  private boolean isLinkADir(String link)
  {
    return link.endsWith("/");
  }
}
