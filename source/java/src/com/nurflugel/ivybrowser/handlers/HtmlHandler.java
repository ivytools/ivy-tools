package com.nurflugel.ivybrowser.handlers;

import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivybrowser.ui.IvyBrowserMainFrame;
import static org.apache.commons.lang.StringUtils.substringAfter;
import static org.apache.commons.lang.StringUtils.substringBefore;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;


@SuppressWarnings({"CallToPrintStackTrace", "IOResourceOpenedButNotSafelyClosed", "UseOfSystemOutOrSystemErr", "OverlyComplexMethod", "OverlyComplexBooleanExpression"})
public class HtmlHandler extends BaseWebHandler
{

    // --------------------------- CONSTRUCTORS ---------------------------

    // private String libraryName;

    public HtmlHandler(IvyBrowserMainFrame mainFrame, String ivyRepositoryPath, List<IvyPackage> ivyPackages)
    {
        super(mainFrame, ivyPackages, ivyRepositoryPath);
    }


//    @Override public Object doInBackground()
//    {
//        findIvyPackages();
//        mainFrame.showNormal();
//
//        return null;
//    }

    // -------------------------- OTHER METHODS --------------------------

    @Override
    public void findIvyPackages()
    {
        System.out.println("ivyRepositoryPath = " + ivyRepositoryPath);

        try
        {
            URL repositoryUrl = new URL(ivyRepositoryPath);
            URLConnection urlConnection = repositoryUrl.openConnection();
            urlConnection.setAllowUserInteraction(true);
            urlConnection.connect();

            InputStream in = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String packageLine = reader.readLine();
            int i = 0;

            while (packageLine != null)
            {
                String lowerLine = packageLine.toLowerCase();
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
                        findModules(repositoryUrl, orgName);
                        mainFrame.populateTable();

                        // if left in, this populates the display real time
                        // if(somePackages.size()>0)mainFrame.populateTable(ivyPackages);
                        if (!shouldRun || (isTest && (i++ > 4)))
                        {
                            break;
                        }
                    }
                }

                packageLine = reader.readLine();
            } // end while

            reader.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        mainFrame.stopProgressPanel();
    }

    private boolean isDirLink(String lowerLine)
    {
        boolean isHref = lowerLine.contains("href");
        boolean isUp = lowerLine.contains("..");
        boolean isPre = lowerLine.startsWith("<pre");
        boolean isDir = lowerLine.contains("[dir]");
        boolean hasDirLink = isHref && !isUp && !isPre && isDir;

        return hasDirLink;
    }

    @Override
    protected String getContents(String packageLine)
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

    @Override
    protected void findModules(URL repositoryUrl, String orgName) throws IOException
    {

        URL moduleUrl = new URL(repositoryUrl + "/" + orgName);
        URLConnection urlConnection = moduleUrl.openConnection();
        urlConnection.setAllowUserInteraction(true);
        urlConnection.connect();

        InputStream in = urlConnection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String moduleLine = reader.readLine();

        while (moduleLine != null)
        {
            boolean isLibrary = isDirLink(moduleLine.toLowerCase());

            if (isLibrary)
            {
                String moduleName = getContents(moduleLine);

                if (!moduleName.contains("Parent Directory") && !moduleName.contains("/Home/"))
                {

                    try
                    {
                        findVersions(repositoryUrl, orgName, moduleName);
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
            hasVersion = versionLine.contains("<A HREF") && versionLine.contains("[DIR]") && !versionLine.contains("..");
        }

        return hasVersion;
    }


    @Override
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


}
