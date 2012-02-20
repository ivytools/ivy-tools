package com.nurflugel.ivybrowser.handlers;

import ca.odell.glazedlists.EventList;
import com.nurflugel.common.ui.UiMainFrame;
import com.nurflugel.ivybrowser.AppPreferences;
import com.nurflugel.ivybrowser.domain.DataSerializer;
import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivybrowser.handlers.tasks.HtmlHandlerTask;
import static org.apache.commons.lang.StringUtils.substringAfter;
import static org.apache.commons.lang.StringUtils.substringBefore;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import static java.util.concurrent.TimeUnit.MINUTES;

@SuppressWarnings({
                    "CallToPrintStackTrace", "IOResourceOpenedButNotSafelyClosed", "UseOfSystemOutOrSystemErr", "OverlyComplexMethod",
                    "OverlyComplexBooleanExpression"
                  })
/**
 * The HTML handler for a HTML based repository.
 */
public class HtmlHandler extends BaseWebIvyRepositoryBrowserHandler
{
  // --------------------------- CONSTRUCTORS ---------------------------
  public HtmlHandler(UiMainFrame mainFrame, String ivyRepositoryPath, List<IvyPackage> ivyPackages,
                     Map<String, Map<String, Map<String, IvyPackage>>> packageMap, AppPreferences preferences)
  {
    super(mainFrame, ivyPackages, ivyRepositoryPath, packageMap);
  }

  // -------------------------- OTHER METHODS --------------------------
  @Override
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

      InputStream    in          = urlConnection.getInputStream();
      BufferedReader reader      = new BufferedReader(new InputStreamReader(in));
      String         packageLine = reader.readLine();

      while (packageLine != null)
      {
        String  lowerLine  = packageLine.toLowerCase();
        boolean hasDirLink = isDirLink(lowerLine);

        if (lowerLine.contains("</ul>"))
        {
          break;
        }

        if (hasDirLink)
        {
          System.out.println("packageLine = " + packageLine);

          String orgName = getContents(packageLine);

          if (!orgName.equalsIgnoreCase("/Home/") && !orgName.contains("Parent Directory"))
          {
            HtmlHandlerTask task = new HtmlHandlerTask(this, repositoryUrl, orgName);

            task.run();
            // threadPool.execute(task);

            // if left in, this populates the display real time
            // if(somePackages.size()>0)mainFrame.populateTable(ivyPackages);
            if (!shouldRun || (isTest))
            {
              break;
            }
          }
        }

        packageLine = reader.readLine();
      }  // end while

      reader.close();
      threadPool.shutdown();

      // block until all threads are done, or until time limit is reached
      threadPool.awaitTermination(5, MINUTES);

      // mainFrame.filterTable();
      System.out.println("ivyPackages = " + ivyPackages.size());

      Date  endTime  = new Date();
      float duration = endTime.getTime() - startTime.getTime();

      System.out.println("HtmlHandler Duration: " + (duration / 1000.0));
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    // serialize results
    DataSerializer dataSerializer = new DataSerializer(ivyRepositoryPath, ivyPackages, packageMap);

    dataSerializer.saveToXml();
    mainFrame.stopProgressPanel();
  }

  //J-
  /**
   * Get the contents of the link - in effect, where is this going? For example, the string
   *
   * <code>
   *    <IMG SRC="/icons/folder.gif" ALT="[DIR]"> <A HREF="aspectj/">aspectj/</A> 30-Jan-2010 23:54 -
   * </code>
   *
   * <p>would return "aspectj" as the contents.</p>
   */
  //J+
  @Override
  public String getContents(String packageLine)
  {
    String newText;

    if (packageLine.contains("<A HREF=\""))
    {
      newText = substringAfter(packageLine, "<A HREF=\"");
    }
    else
    {
      newText = substringAfter(packageLine, "<a href=\"");
    }

    String result = substringBefore(newText, "\">");

    return stripSlash(result);
  }

  @Override
  protected boolean hasVersion(String versionLine)
  {
    boolean hasVersion;

    if (versionLine.contains("<li"))
    {
      hasVersion = versionLine.contains("<li") && !versionLine.contains("..");
    }
    else
    {
      hasVersion = versionLine.toUpperCase().contains("<A HREF") && versionLine.toUpperCase().contains("[DIR]") && !versionLine.contains("..");
    }

    return hasVersion;
  }
}
