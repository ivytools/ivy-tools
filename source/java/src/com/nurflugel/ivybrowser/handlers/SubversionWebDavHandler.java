package com.nurflugel.ivybrowser.handlers;

import ca.odell.glazedlists.EventList;
import com.nurflugel.common.ui.UiMainFrame;
import com.nurflugel.ivybrowser.AppPreferences;
import com.nurflugel.ivybrowser.domain.DataSerializer;
import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivybrowser.handlers.tasks.SubversionWebDavHandlerTask;
import com.nurflugel.ivytracker.IvyTrackerMainFrame;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static java.util.concurrent.TimeUnit.MINUTES;

/** Subversion flavored handler. Subversion hosted repositories render HTML a bit differently than pure HTML-based repositories. */
public class SubversionWebDavHandler extends BaseWebIvyRepositoryBrowserHandler
{
  // --------------------------- CONSTRUCTORS ---------------------------
  public SubversionWebDavHandler(UiMainFrame mainFrame, String ivyRepositoryPath, EventList<IvyPackage> ivyPackages,
                                 Map<String, Map<String, Map<String, IvyPackage>>> packageMap, AppPreferences preferences)
  {
    super(mainFrame, ivyPackages, ivyRepositoryPath, packageMap);
  }

  // -------------------------- OTHER METHODS --------------------------
  @Override
  @SuppressWarnings({ "CallToPrintStackTrace", "UseOfSystemOutOrSystemErr" })
  public void findIvyPackages()
  {
    System.out.println("ivyRepositoryPath = " + ivyRepositoryPath);

    try
    {
      Date          startTime     = new Date();
      URL           repositoryUrl = new URL(ivyRepositoryPath);
      URLConnection urlConnection = repositoryUrl.openConnection();

      urlConnection.setAllowUserInteraction(true);
      urlConnection.connect();

      InputStream     in          = urlConnection.getInputStream();
      BufferedReader  reader      = new BufferedReader(new InputStreamReader(in));
      String          packageLine = reader.readLine();
      ExecutorService threadPool  = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

      while (packageLine != null)
      {
        boolean hasLink = packageLine.contains("href") && !packageLine.contains("..");

        if (packageLine.contains("</ul>"))
        {
          break;
        }

        if (hasLink)
        {
          String                      orgName = getContents(packageLine);
          SubversionWebDavHandlerTask task    = new SubversionWebDavHandlerTask(repositoryUrl, orgName, this);

          threadPool.execute(task);

          // if left in, this populates the display real time
          // if(somePackages.size()>0)mainFrame.populateTable(ivyPackages);
          if (!shouldRun || (isTest))
          {
            break;
          }
        }

        packageLine = reader.readLine();
      }

      reader.close();
      threadPool.shutdown();

      // block until all threads are done, or until time limit is reached
      threadPool.awaitTermination(5, MINUTES);

      // mainFrame.filterTable();
      System.out.println("ivyPackages = " + ivyPackages.size());

      Date  endTime  = new Date();
      float duration = endTime.getTime() - startTime.getTime();

      System.out.println("SubversionWebDavHandler Duration: " + (duration / 1000.0));
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    // todo just add to the table model, and then the UI will refresh on the fly
    if (mainFrame instanceof IvyTrackerMainFrame)
    {
      ((IvyTrackerMainFrame) mainFrame).setIvyDone(true);
    }

    // serialize results
    DataSerializer dataSerializer = new DataSerializer(ivyRepositoryPath, ivyPackages, packageMap);

    dataSerializer.saveToXml();
    mainFrame.stopProgressPanel();
  }

  @Override
  public String getContents(String packageLine)
  {
    int    index  = packageLine.indexOf("\"");
    String result = packageLine.substring(index + 1);

    index  = result.indexOf("/");
    result = result.substring(0, index);
    index  = result.indexOf("\"");

    if (index > -1)
    {
      result = result.substring(0, index);
    }

    return result;
  }

  @Override
  public boolean hasVersion(String versionLine)
  {
    boolean hasVersion = versionLine.contains("<li") && !versionLine.contains("..");

    return hasVersion;
  }
}
