package com.nurflugel.ivybrowser.handlers;

import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivybrowser.ui.IvyBrowserMainFrame;
import com.nurflugel.ivybrowser.handlers.tasks.HtmlHandlerTask;

import java.io.*;

import java.net.URL;
import java.net.URLConnection;

import java.util.List;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static java.util.concurrent.TimeUnit.MINUTES;

import static org.apache.commons.lang.StringUtils.substringAfter;
import static org.apache.commons.lang.StringUtils.substringBefore;
import org.apache.commons.lang.StringUtils;

@SuppressWarnings({ "CallToPrintStackTrace", "IOResourceOpenedButNotSafelyClosed", "UseOfSystemOutOrSystemErr", "OverlyComplexMethod", "OverlyComplexBooleanExpression" })
public class HtmlHandler extends BaseWebHandler
{


    // --------------------------- CONSTRUCTORS ---------------------------
    // private String libraryName;
    public HtmlHandler(IvyBrowserMainFrame mainFrame, String ivyRepositoryPath, List<IvyPackage> ivyPackages)
    {
        super(mainFrame, ivyPackages, ivyRepositoryPath);
    }

    // @Override public Object doInBackground()
    // {
    // findIvyPackages();
    // mainFrame.showNormal();
    //
    // return null;
    // }
    // -------------------------- OTHER METHODS --------------------------
    @Override public void findIvyPackages()
    {
        System.out.println("ivyRepositoryPath = " + ivyRepositoryPath);

        try
        {
            Date startTime=new Date();
            URL           repositoryUrl = new URL(ivyRepositoryPath);
            URLConnection urlConnection = repositoryUrl.openConnection();

            urlConnection.setAllowUserInteraction(true);
            urlConnection.connect();

            InputStream    in          = urlConnection.getInputStream();
            BufferedReader reader      = new BufferedReader(new InputStreamReader(in));
            String         packageLine = reader.readLine();
            ExecutorService threadPool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);


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
                        HtmlHandlerTask task=new HtmlHandlerTask(mainFrame, this, repositoryUrl, orgName);
                        threadPool.execute(task);


                        // if left in, this populates the display real time
                        // if(somePackages.size()>0)mainFrame.populateTable(ivyPackages);
                        if (!shouldRun || (isTest ))
                        {
                            break;
                        }
                    }
                }

                packageLine = reader.readLine();
            }  // end while

            reader.close();
             threadPool.shutdown();
            //block until all threads are done, or until time limit is reached
            threadPool.awaitTermination(5, MINUTES);
             mainFrame.filterTable();
            System.out.println("ivyPackages = " + ivyPackages.size());
            Date endTime=new Date();
            float duration = endTime.getTime() - startTime.getTime();
            System.out.println("HtmlHandler Duration: "+duration/1000.0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        mainFrame.stopProgressPanel();
    }

    @Override
    protected boolean shouldProcessIncludedFileLine(String line)
    {
        boolean isIvyFile = line.contains("ivy.xml");
        boolean isValidLine = shouldProcessVersionedLibraryLine(line);
        return isValidLine && !isIvyFile;
    }

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

        return result;
    }

    protected boolean hasVersion(String versionLine)
    {
        boolean hasVersion;

        if (versionLine.contains("<li"))
        {
            hasVersion = versionLine.contains("<li") && !versionLine.contains("..");
        }
        else
        {
            hasVersion = versionLine.contains("<A HREF") && versionLine.contains("[DIR]") && !versionLine.contains("..");
        }

        return hasVersion;
    }

    protected boolean shouldProcessVersionedLibraryLine(String line)
    {
        boolean shouldProcess;

        if (line.contains("<li"))
        {
            shouldProcess = line.contains("<li") && !line.contains("..") && !line.contains("md5") && !line.contains("sha1");
        }
        else
        {
            shouldProcess = line.contains("A HREF") && !line.contains("[DIR]") && !line.contains("<PRE>") && !line.contains("Parent Directory") && !line.contains("sha1") && !line.contains("md5");
        }

        return shouldProcess;
    }

    /** Parse the file name out of the html line
    */
    @Override protected String parseIncludedFileInfo(String line, String version)
    {
        String trimmedLine = line.trim();
        String parsedLine  = StringUtils.substringAfter(trimmedLine, "A HREF=\"");

        parsedLine = StringUtils.substringBefore(parsedLine, "\"");

        String size = StringUtils.substringAfterLast(trimmedLine, " ");

        return parsedLine + "   " + size;
    }

}
