package com.nurflugel.externalsreporter.ui;

import com.nurflugel.WebAuthenticator;
import static org.apache.commons.lang.StringUtils.substringAfter;
import static org.apache.commons.lang.StringUtils.substringBefore;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

/** Class to parse the URL for files and such. */
@SuppressWarnings({ "UseOfSystemOutOrSystemErr" })
public class HtmlHandler
{
  /**
   * Get the list of files for this URL.
   *
   * @param   repositoryUrl  the URL to scan. Should be a directory
   * @param   dirsOnly       if true, will only return links that are directories
   *
   * @return  a list of the URLs
   */
  public List<String> getFiles(String repositoryUrl, boolean dirsOnly) throws IOException
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

            if (isLinkADir(link) || !dirsOnly)
            {
              if (repositoryUrl.endsWith("/"))
              {
                files.add(repositoryUrl + link);
              }
              else
              {
                files.add(repositoryUrl + "/" + link);
              }
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
      e.printStackTrace();

      // this gets us around a bad password, but never saves it...
      if (e.getMessage().contains("redirected"))
      {
        Authenticator.setDefault(new WebAuthenticator());
        showMessageDialog(null, "Username/password authentication failed, try again", "Nice try", WARNING_MESSAGE);
      }
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

    return line.toUpperCase().contains("A HREF");
  }

  /** Get the link text (the first one). */
  private String getLink(String line)
  {
    String linkText = substringAfter(line, "a href=\"");

    linkText = substringBefore(linkText, "\"");

    return linkText;
  }

  private boolean isLinkADir(String link)
  {
    return link.endsWith("/");
  }
}
