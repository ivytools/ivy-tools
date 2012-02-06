package com.nurflugel.ivybrowser.handlers.tasks;

import com.nurflugel.ivybrowser.handlers.SubversionWebDavHandler;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Apr 27, 2009 Time: 10:19:45 PM To change this template use File | Settings | File Templates.
 */
public class SubversionWebDavHandlerTask implements Runnable
{
  private URL                     repositoryUrl;
  private String                  orgName;
  private SubversionWebDavHandler handler;

  // --------------------------- CONSTRUCTORS ---------------------------
  public SubversionWebDavHandlerTask(URL repositoryUrl, String orgName, SubversionWebDavHandler handler)
  {
    this.repositoryUrl = repositoryUrl;
    this.orgName       = orgName;
    this.handler       = handler;
  }

  // ------------------------ INTERFACE METHODS ------------------------
  // --------------------- Interface Runnable ---------------------
  @Override
  public void run()
  {
    try
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
        boolean isLibrary = handler.hasVersion(moduleLine);

        if (isLibrary)
        {
          String moduleName = handler.getContents(moduleLine);

          handler.findVersions(repositoryUrl, orgName, moduleName);
        }

        moduleLine = reader.readLine();
      }

      reader.close();
    }
    catch (StringIndexOutOfBoundsException e)
    {
      // do nothing, we're parsing a non-Ivy file
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
