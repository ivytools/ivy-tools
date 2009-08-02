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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;

/** Created by IntelliJ IDEA. User: douglasbullard Date: Apr 27, 2009 Time: 10:31:06 PM To change this template use File | Settings | File Templates. */
public abstract class BaseWebHandler extends SwingWorker<Object, Object>
{
    protected IvyBrowserMainFrame mainFrame;
    protected boolean             isTest            = false;
    protected boolean             shouldRun         = true;
    protected String              ivyRepositoryPath;
    protected List<IvyPackage>    ivyPackages;
    public static final int       NUMBER_OF_THREADS = 5;

    protected BaseWebHandler(IvyBrowserMainFrame mainFrame, List<IvyPackage> ivyPackages, String ivyRepositoryPath)
    {
        this.mainFrame         = mainFrame;
        this.ivyPackages       = ivyPackages;
        this.ivyRepositoryPath = ivyRepositoryPath;
    }

    @Override public Object doInBackground()
    {
        findIvyPackages();
        mainFrame.showNormal();

        return null;
    }

    public boolean isDirLink(String lowerLine)
    {
        boolean isHref     = lowerLine.contains("href");
        boolean isUp       = lowerLine.contains("..");
        boolean isPre      = lowerLine.startsWith("<pre");
        boolean isDir      = lowerLine.contains("[dir]");
        boolean hasDirLink = isHref && !isUp && !isPre && isDir;

        return hasDirLink;
    }

    protected void removeIvyFile(List<IvyPackage> localPackages)
    {
        for (IvyPackage localPackage : localPackages)
        {
            if (localPackage.getLibrary().endsWith(".ivy"))
            {
                localPackages.remove(localPackage);

                return;
            }
        }
    }

    protected boolean isAnythingOtherThanIvy(List<IvyPackage> localPackages)
    {
        for (IvyPackage localPackage : localPackages)
        {
            String library = localPackage.getLibrary();

            if (library != null)
            {
                if (!library.endsWith(".ivy"))
                {
                    return true;
                }
            }
        }

        return false;
    }

    protected abstract String getContents(String packageLine);

    protected void findVersionedLibrary(URL repositoryUrl, String orgName, String moduleName, String version)
                                 throws IOException
    {
        URL           versionUrl    = new URL(repositoryUrl + "/" + orgName + "/" + moduleName + "/" + version);
        URLConnection urlConnection = versionUrl.openConnection();

        urlConnection.setAllowUserInteraction(true);
        urlConnection.connect();

        InputStream             in            = urlConnection.getInputStream();
        BufferedReader          reader        = new BufferedReader(new InputStreamReader(in));
        String                  line          = reader.readLine();
        List<IvyPackage>        localPackages = new ArrayList<IvyPackage>();
        String                  ivyFile       = null;
        Map<String, IvyPackage> jars          = new HashMap<String, IvyPackage>();

        while (line != null)
        {
            boolean shouldProcess = shouldProcessVersionedLibraryLine(line);

            if (shouldProcess)
            {
                boolean isSource      = line.contains("-src") || line.contains("-source");
                boolean isJavadoc     = line.contains("-javadoc");
                boolean isLibraryFile = line.contains(".jar") && !isSource && !isJavadoc;
                boolean isIvyFile     = line.contains(".ivy.xml");
                String  library       = getContents(line);

                if (isLibraryFile)
                {
                    int index = library.indexOf(version);

                    if (index > -1)
                    {
                        library = library.substring(0, index - 1);
                    }

                    library = library.replaceAll(".jar", "");

                    IvyPackage ivyPackage = new IvyPackage(orgName, moduleName, version, library);

                    jars.put(library, ivyPackage);
                    localPackages.add(ivyPackage);
                }
                else if (isIvyFile)
                {
                    ivyFile = getContents(line);
                    library = library.replaceAll(".xml", "");

                    IvyPackage ivyPackage = new IvyPackage(orgName, moduleName, version, library);

                    localPackages.add(ivyPackage);
                }
                else  // it's a javadoc or source
                {
                    if (library.contains(".jar"))
                    {
                        library = library.replaceAll(".jar", "");
                    }

                    if (library.contains(".zip"))
                    {
                        library = library.replaceAll(".zip", "");
                    }

                    if (library.contains("-" + version))
                    {
                        library = library.replaceAll("-" + version, "");
                    }

                    if (isSource)
                    {
                        library = handleSources(jars.get(library), library);
                    }

                    if (isJavadoc)
                    {
                        handleJavadocs(jars.get(library), library);
                    }
                }  // end if-else
            }  // end if

            line = reader.readLine();
        }  // end while

        reader.close();

        if ((ivyFile != null))
        {
            for (IvyPackage localPackage : localPackages)
            {
                localPackage.setIvyFile(ivyFile);
                localPackage.setVersionUrl(versionUrl);
            }
        }

        // remove ivy package if there are any jars existing
        if (isAnythingOtherThanIvy(localPackages))
        {
            removeIvyFile(localPackages);
        }

        // for (IvyPackage localPackage : localPackages)
        // {
        // mainFrame.addIvyPackage(localPackage);
        // }
        ivyPackages.addAll(localPackages);
    }

    private void handleJavadocs(IvyPackage ivyPackage, String library)
    {
        if (library.contains("-javadocs"))
        {
            library = library.replaceAll("-javadocs", "");
        }

        if (library.contains("-javadoc"))
        {
            library = library.replaceAll("-javadoc", "");
        }

        if (ivyPackage != null)
        {
            ivyPackage.setHasJavaDocs(true);
        }
    }

    private String handleSources(IvyPackage ivyPackage, String library)
    {
        if (library.contains("-src"))
        {
            library = library.replaceAll("-src", "");
        }

        if (library.contains("-sources"))
        {
            library = library.replaceAll("-sources", "");
        }

        if (library.contains("-source"))
        {
            library = library.replaceAll("-source", "");
        }

        if (ivyPackage != null)
        {
            ivyPackage.setHasSourceCode(true);
        }

        return library;
    }

    protected abstract boolean shouldProcessVersionedLibraryLine(String line);

    public void findVersions(URL repositoryUrl, String orgName, String moduleName)
                      throws IOException
    {
        URL           versionUrl    = new URL(repositoryUrl + "/" + orgName + "/" + moduleName);
        URLConnection urlConnection = versionUrl.openConnection();

        urlConnection.setAllowUserInteraction(true);
        urlConnection.connect();

        InputStream    in          = urlConnection.getInputStream();
        BufferedReader reader      = new BufferedReader(new InputStreamReader(in));
        String         versionLine = reader.readLine();

        while (versionLine != null)
        {
            boolean hasVersion = hasVersion(versionLine);

            if (hasVersion && !versionLine.contains("Parent Directory"))
            {
                String version = getContents(versionLine);

                mainFrame.setStatusLabel("Parsing " + moduleName + " version " + version);
                findVersionedLibrary(repositoryUrl, orgName, moduleName, version);
            }

            versionLine = reader.readLine();
        }

        reader.close();
    }

    protected abstract boolean hasVersion(String versionLine);

    //
    // protected abstract void findModules(URL repositoryUrl, String orgName)
    // throws IOException;
    //
    public abstract void findIvyPackages();

    public List<String> findIncludedFiles(String repositoryUrl, String orgName, String moduleName, String version)
    {
        List<String> includedFiles = new ArrayList<String>();

        try
        {
            URL           versionUrl    = new URL(repositoryUrl + "/" + orgName + "/" + moduleName + "/" + version);
            URLConnection urlConnection = versionUrl.openConnection();

            urlConnection.setAllowUserInteraction(true);
            urlConnection.connect();

            InputStream    in     = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String         line   = reader.readLine();

            while (line != null)
            {
                boolean shouldProcess = shouldProcessIncludedFileLine(line);

                if (shouldProcess)
                {
                    includedFiles.add(parseIncludedFileInfo(line, version));
                }

                line = reader.readLine();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return includedFiles;
    }

    /** Parse the file name out of the html line
     */
    protected abstract String parseIncludedFileInfo(String line, String version);

    protected abstract boolean shouldProcessIncludedFileLine(String line);
}
