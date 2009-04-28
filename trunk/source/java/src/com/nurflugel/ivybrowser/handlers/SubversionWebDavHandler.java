package com.nurflugel.ivybrowser.handlers;

import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivybrowser.ui.IvyBrowserMainFrame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Apr 27, 2009 Time: 10:19:45 PM To change this template use File | Settings | File Templates.
 */
public class SubversionWebDavHandler extends BaseWebHandler
{

    // --------------------------- CONSTRUCTORS ---------------------------


    public SubversionWebDavHandler(IvyBrowserMainFrame mainFrame, String ivyRepositoryPath, List<IvyPackage> ivyPackages)
    {
        super(mainFrame, ivyPackages, ivyRepositoryPath);
    }


    @Override public Object doInBackground()
    {
        findIvyPackages();
        mainFrame.showNormal();

        return null;
    }

    // -------------------------- OTHER METHODS --------------------------

    @Override public void findIvyPackages()
    {
        System.out.println("ivyRepositoryPath = " + ivyRepositoryPath);

        try {
            URL           repositoryUrl = new URL(ivyRepositoryPath);
            URLConnection urlConnection = repositoryUrl.openConnection();
            urlConnection.setAllowUserInteraction(true);
            urlConnection.connect();

            InputStream    in          = urlConnection.getInputStream();
            BufferedReader reader      = new BufferedReader(new InputStreamReader(in));
            String         packageLine = reader.readLine();
            int            i           = 0;

            while (packageLine != null) {
                boolean hasLink = packageLine.contains("href") && !packageLine.contains("..");

                if (packageLine.contains("</ul>")) {
                    break;
                }

                if (hasLink) {
                    String orgName = getContents(packageLine);
                    findModules(repositoryUrl, orgName);
                    mainFrame.populateTable();

                    // if left in, this populates the display real time
                    // if(somePackages.size()>0)mainFrame.populateTable(ivyPackages);
                    if (!shouldRun || (isTest && (i++ > 4))) {
                        break;
                    }
                }

                packageLine = reader.readLine();
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        // todo just add to the table model, and then the UI will refersh on the fly
        mainFrame.stopProgressPanel();
    }

    @Override protected String getContents(String packageLine)
    {
        int index = packageLine.indexOf("\"");

        String result = packageLine.substring(index + 1);
        index  = result.indexOf("/");
        result = result.substring(0, index);
        index  = result.indexOf("\"");

        if (index > -1) {
            result = result.substring(0, index);
        }

        return result;
    }

    @Override protected void findModules(URL repositoryUrl, String orgName) throws IOException
    {
        List<IvyPackage> ivyPackages   = new ArrayList<IvyPackage>();
        URL              moduleUrl     = new URL(repositoryUrl + "/" + orgName);
        URLConnection    urlConnection = moduleUrl.openConnection();
        urlConnection.setAllowUserInteraction(true);
        urlConnection.connect();

        InputStream    in         = urlConnection.getInputStream();
        BufferedReader reader     = new BufferedReader(new InputStreamReader(in));
        String         moduleLine = reader.readLine();

        while (moduleLine != null) {
            boolean isLibrary = hasVersion(moduleLine);

            if (isLibrary) {
                String moduleName = getContents(moduleLine);
                findVersions(repositoryUrl, orgName, moduleName);
            }

            moduleLine = reader.readLine();
        }

        reader.close();

    }


    @Override protected boolean hasVersion(String versionLine)
    {
        boolean hasVersion = versionLine.contains("<li") && !versionLine.contains("..");

        return hasVersion;
    }

    @Override protected boolean shouldProcessVersionedLibraryLine(String line)
    {
        boolean shouldProcess = line.contains("<li") && !line.contains("..") && !line.contains("md5") && !line.contains("sha1");

        return shouldProcess;
    }


}
