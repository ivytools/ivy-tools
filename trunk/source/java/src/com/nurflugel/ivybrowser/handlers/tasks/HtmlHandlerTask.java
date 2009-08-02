package com.nurflugel.ivybrowser.handlers.tasks;

import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivybrowser.handlers.BaseWebHandler;
import com.nurflugel.ivybrowser.handlers.HtmlHandler;
import com.nurflugel.ivybrowser.ui.IvyBrowserMainFrame;
import static org.apache.commons.lang.StringUtils.substringAfter;
import static org.apache.commons.lang.StringUtils.substringBefore;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

@SuppressWarnings({ "CallToPrintStackTrace", "IOResourceOpenedButNotSafelyClosed", "UseOfSystemOutOrSystemErr", "OverlyComplexMethod", "OverlyComplexBooleanExpression" })
public class HtmlHandlerTask implements Runnable// extends BaseWebHandler
{
    private IvyBrowserMainFrame mainFrame;
//    private String ivyRepositoryPath;
//    private List<IvyPackage> ivyPackages;
    private HtmlHandler htmlHandler;
    private URL repositoryUrl;
    private String orgName;

    // --------------------------- CONSTRUCTORS ---------------------------
    // private String libraryName;
    public HtmlHandlerTask(IvyBrowserMainFrame mainFrame, HtmlHandler htmlHandler, URL repositoryUrl, String orgName)
    {
//        super(mainFrame, ivyPackages, ivyRepositoryPath);
        this.mainFrame = mainFrame;
//        this.ivyRepositoryPath = ivyRepositoryPath;
//        this.ivyPackages = ivyPackages;
        this.htmlHandler = htmlHandler;
        this.repositoryUrl = repositoryUrl;
        this.orgName = orgName;
    }

     public void run()
    {
        try
        {
            findModules();
            // mainFrame.filterTable();
            
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }



//    protected String getContents(String packageLine)
//    {
//        String newText;
//
//        if (packageLine.contains("<A HREF=\""))
//        {
//            newText = substringAfter(packageLine, "<A HREF=\"");
//        }
//        else
//        {
//            newText = substringAfter(packageLine, "<a href=\"");
//        }
//
//        String result = substringBefore(newText, "\">");
//
//        return result;
//    }

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