package com.nurflugel.ivybrowser.handlers.tasks;

import com.nurflugel.ivybrowser.handlers.BaseWebHandler;
import com.nurflugel.ivybrowser.handlers.HtmlHandler;
import com.nurflugel.ivybrowser.ui.IvyBrowserMainFrame;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

@SuppressWarnings({
                    "CallToPrintStackTrace", "IOResourceOpenedButNotSafelyClosed", "UseOfSystemOutOrSystemErr", "OverlyComplexMethod",
                    "OverlyComplexBooleanExpression"
                  })
public class HtmlHandlerTask implements Runnable
{
  private IvyBrowserMainFrame mainFrame;
  private HtmlHandler         htmlHandler;
  private URL                 repositoryUrl;
  private String              orgName;

  // --------------------------- CONSTRUCTORS ---------------------------
  public HtmlHandlerTask(IvyBrowserMainFrame mainFrame, HtmlHandler htmlHandler, URL repositoryUrl, String orgName)
  {
    this.mainFrame     = mainFrame;
    this.htmlHandler   = htmlHandler;
    this.repositoryUrl = repositoryUrl;
    this.orgName       = BaseWebHandler.stripSlash(orgName);
  }

  public void run()
  {
    try
    {
      findModules();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  public void findModules() throws IOException
  {
    URL           moduleUrl     = new URL(repositoryUrl + "/" + orgName);
    URLConnection urlConnection = moduleUrl.openConnection();

    urlConnection.setAllowUserInteraction(true);
    urlConnection.connect();

    InputStream    in         = urlConnection.getInputStream();
    BufferedReader reader     = new BufferedReader(new InputStreamReader(in));
    String         moduleLine = reader.readLine();

    while (moduleLine != null)
    {
      boolean isLibrary = htmlHandler.isDirLink(moduleLine.toLowerCase());

      if (isLibrary)
      {
        String moduleName = htmlHandler.getContents(moduleLine);

        if (!moduleName.contains("Parent Directory") && !moduleName.contains("/Home/"))
        {
          try
          {
            htmlHandler.findVersions(repositoryUrl, orgName, moduleName);
          }
          catch (FileNotFoundException e)
          {
            System.out.println("Had problem parsing package " + orgName + " " + moduleName);
          }
        }
      }

      moduleLine = reader.readLine();
    }

    reader.close();
  }
}
