package com.nurflugel.ivybrowser.handlers;

import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivybrowser.ui.IvyBrowserMainFrame;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/** Created by IntelliJ IDEA. User: douglasbullard Date: Apr 27, 2009 Time: 10:31:06 PM To change this template use File | Settings | File Templates. */
@SuppressWarnings({"ProtectedField"})
public abstract class BaseWebHandler extends SwingWorker<Object, Object>
{
    protected IvyBrowserMainFrame mainFrame;
    protected boolean isTest = false;
    protected boolean shouldRun = true;
    protected String ivyRepositoryPath;
    private Map<String, Map<String, Map<String, IvyPackage>>> packageMap;
    protected List<IvyPackage> ivyPackages;
    public static final int NUMBER_OF_THREADS = 5;

    protected BaseWebHandler(IvyBrowserMainFrame mainFrame, List<IvyPackage> ivyPackages, String ivyRepositoryPath, Map<String, Map<String, Map<String, IvyPackage>>> packageMap)
    {
        this.mainFrame = mainFrame;
        this.ivyPackages = ivyPackages;
        this.ivyRepositoryPath = ivyRepositoryPath;
        this.packageMap = packageMap;
    }

    @Override
    public Object doInBackground()
    {
        findIvyPackages();
        mainFrame.showNormal();

        return null;
    }

    public boolean isDirLink(String lowerLine)
    {
        boolean isHref = lowerLine.contains("href");
        boolean isUp = lowerLine.contains("..");
        boolean isPre = lowerLine.startsWith("<pre");
        boolean isDir = lowerLine.contains("[dir]");
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
        URL versionUrl = new URL(repositoryUrl + "/" + orgName + "/" + moduleName + "/" + version);
        URLConnection urlConnection = versionUrl.openConnection();

        urlConnection.setAllowUserInteraction(true);
        urlConnection.connect();

        InputStream in = urlConnection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = reader.readLine();
        List<IvyPackage> localPackages = new ArrayList<IvyPackage>();
        String ivyFile = null;
        Map<String, IvyPackage> jars = new HashMap<String, IvyPackage>();

        while (line != null)
        {
            boolean shouldProcess = shouldProcessVersionedLibraryLine(line);

            if (shouldProcess)
            {
                boolean isSource = line.contains("-src") || line.contains("-source");
                boolean isJavadoc = line.contains("-javadoc");
                boolean isLibraryFile = line.contains(".jar") && !isSource && !isJavadoc;
                boolean isIvyFile = line.contains(".ivy.xml");
                String library = getContents(line);

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
        addPackages(localPackages);
    }

    private void addPackages(List<IvyPackage> localPackages)
    {
        ivyPackages.addAll(localPackages);
        for (IvyPackage localPackage : localPackages)
        {
            addPackage(localPackage);
        }
    }

    private void addPackage(IvyPackage ivyPackage)
    {
        String orgName = stripSlash(ivyPackage.getOrgName());
        String moduleName = stripSlash(ivyPackage.getModuleName());
        String version = stripSlash(ivyPackage.getVersion());
        
        Map<String, Map<String, IvyPackage>> orgMap = packageMap.get(orgName);
        if (orgMap == null)
        {
            orgMap = Collections.synchronizedMap(new HashMap<String, Map<String, IvyPackage>>());
            packageMap.put(orgName, orgMap);
        }
        Map<String,  IvyPackage> moduleMap = orgMap.get(moduleName);
        if (moduleMap == null)
        {
            moduleMap = Collections.synchronizedMap(new HashMap<String,  IvyPackage>());
            orgMap.put(moduleName, moduleMap);
        }
        IvyPackage thePackage = moduleMap.get(version);
        if (thePackage == null)
        {
                moduleMap.put(version,ivyPackage);
        }

    }

    public static String stripSlash(String text)
    {
        return text.replaceAll("/","");
    }

    private void handleJavadocs(IvyPackage ivyPackage, String library)
    {
        String library1 = library;
        if (library1.contains("-javadocs"))
        {
            library1 = library1.replaceAll("-javadocs", "");
        }

        if (library1.contains("-javadoc"))
        {
            library1 = library1.replaceAll("-javadoc", "");
        }

        if (ivyPackage != null)
        {
            ivyPackage.setHasJavaDocs(true);
        }
    }

    private String handleSources(IvyPackage ivyPackage, String library)
    {
        String library1 = library;
        if (library1.contains("-src"))
        {
            library1 = library1.replaceAll("-src", "");
        }

        if (library1.contains("-sources"))
        {
            library1 = library1.replaceAll("-sources", "");
        }

        if (library1.contains("-source"))
        {
            library1 = library1.replaceAll("-source", "");
        }

        if (ivyPackage != null)
        {
            ivyPackage.setHasSourceCode(true);
        }

        return library1;
    }

    protected abstract boolean shouldProcessVersionedLibraryLine(String line);

    public void findVersions(URL repositoryUrl, String orgName, String moduleName)
            throws IOException
    {
        URL versionUrl = new URL(repositoryUrl + "/" + orgName + "/" + moduleName);
        URLConnection urlConnection = versionUrl.openConnection();

        urlConnection.setAllowUserInteraction(true);
        urlConnection.connect();

        InputStream in = urlConnection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String versionLine = reader.readLine();

        while (versionLine != null)
        {
            boolean hasVersion = hasVersion(versionLine);

            if (hasVersion && !versionLine.contains("Parent Directory"))
            {
                String version = getContents(versionLine);

                mainFrame.setStatusLabel("Parsing " + moduleName + " version " + version);
                findVersionedLibrary(repositoryUrl, stripSlash(orgName), stripSlash(moduleName), stripSlash(version));
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
            URL versionUrl = new URL(repositoryUrl + "/" + orgName + "/" + moduleName + "/" + version);
            URLConnection urlConnection = versionUrl.openConnection();

            urlConnection.setAllowUserInteraction(true);
            urlConnection.connect();

            InputStream in = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = reader.readLine();

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

    /** Parse the file name out of the html line */
    protected abstract String parseIncludedFileInfo(String line, String version);

    protected abstract boolean shouldProcessIncludedFileLine(String line);
}
