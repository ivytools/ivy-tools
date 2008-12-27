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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressWarnings({"CallToPrintStackTrace", "IOResourceOpenedButNotSafelyClosed", "UseOfSystemOutOrSystemErr"})
public class HtmlHandler extends SwingWorker<Object, Object>
{
    private boolean isTest = false;
    private boolean shouldRun = true;
    private List<IvyPackage> matchingLibraries;
    private IvyBrowserMainFrame mainFrame;
    private String ivyRepositoryPath;

    // --------------------------- CONSTRUCTORS ---------------------------

    // private String libraryName;

    public HtmlHandler(IvyBrowserMainFrame mainFrame, String ivyRepositoryPath)
    {
        this.mainFrame = mainFrame;
        this.ivyRepositoryPath = ivyRepositoryPath;
    }


    @Override
    public Object doInBackground()
    {
        findIvyPackages();
        mainFrame.showNormal();

        return null;
    }

    // -------------------------- OTHER METHODS --------------------------

    public List<IvyPackage> findIvyPackages()
    {
        List<IvyPackage> ivyPackages = new ArrayList<IvyPackage>();

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
                boolean hasLink = packageLine.contains("href") && !packageLine.contains("..");

                if (packageLine.contains("</ul>"))
                {
                    break;
                }

                if (hasLink)
                {
                    String orgName = getContents(packageLine);
                    List<IvyPackage> somePackages = findModules(repositoryUrl, orgName);
                    ivyPackages.addAll(somePackages);

                    // if left in, this populates the display real time
                    // if(somePackages.size()>0)mainFrame.populateTable(ivyPackages);
                    if (!shouldRun || (isTest && (i++ > 4)))
                    {
                        break;
                    }
                }

                packageLine = reader.readLine();
            }

            reader.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        matchingLibraries = ivyPackages;

        // todo just add to the table model, and then the UI will refersh on the fly
        mainFrame.populateTable(ivyPackages);

        return ivyPackages;
    }

    private String getContents(String packageLine)
    {
        int index = packageLine.indexOf("\"");

        String result = packageLine.substring(index + 1);
        index = result.indexOf("/");
        result = result.substring(0, index);
        index = result.indexOf("\"");

        if (index > -1)
        {
            result = result.substring(0, index);
        }

        return result;
    }

    private List<IvyPackage> findModules(URL repositoryUrl, String orgName) throws IOException
    {
        List<IvyPackage> ivyPackages = new ArrayList<IvyPackage>();
        URL moduleUrl = new URL(repositoryUrl + "/" + orgName);
        URLConnection urlConnection = moduleUrl.openConnection();
        urlConnection.setAllowUserInteraction(true);
        urlConnection.connect();

        InputStream in = urlConnection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String moduleLine = reader.readLine();

        while (moduleLine != null)
        {
            boolean isLibrary = moduleLine.contains("<li") && !moduleLine.contains("..");

            if (isLibrary)
            {
                String moduleName = getContents(moduleLine);
                findVersions(repositoryUrl, orgName, moduleName, ivyPackages);
            }

            moduleLine = reader.readLine();
        }

        reader.close();

        return ivyPackages;
    }

    private void findVersions(URL repositoryUrl, String orgName, String moduleName, List<IvyPackage> ivyPackages) throws IOException
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
            boolean hasVersion = versionLine.contains("<li") && !versionLine.contains("..");

            if (hasVersion)
            {
                String version = getContents(versionLine);
                mainFrame.setStatusLabel("Parsing " + moduleName + " version " + version);
                findVersionedLibrary(repositoryUrl, orgName, moduleName, version, ivyPackages);
            }

            versionLine = reader.readLine();
        }

        reader.close();
    }

    @SuppressWarnings({"OverlyComplexBooleanExpression"})
    private void findVersionedLibrary(URL repositoryUrl, String orgName, String moduleName, String version,
                                      List<IvyPackage> ivyPackages) throws IOException
    {
        URL versionUrl = new URL(repositoryUrl + "/" + orgName + "/" + moduleName + "/" + version);
        URLConnection urlConnection = versionUrl.openConnection();
        urlConnection.setAllowUserInteraction(true);
        urlConnection.connect();

        InputStream in = urlConnection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String versionLine = reader.readLine();
        List<IvyPackage> localPackages = new ArrayList<IvyPackage>();
        String ivyFile = null;
        Map<String, IvyPackage> jars = new HashMap<String, IvyPackage>();

        while (versionLine != null)
        {
            boolean shouldProcess = versionLine.contains("<li") && !versionLine.contains("..") && !versionLine.contains("md5") &&
                                    !versionLine.contains("sha1");

            if (shouldProcess)
            {
                boolean isSource = versionLine.contains("-src") || versionLine.contains("-source");
                boolean isJavadoc = versionLine.contains("-javadoc");
                boolean isLibraryFile = versionLine.contains(".jar") && !isSource && !isJavadoc;
                boolean isIvyFile = versionLine.contains(".ivy.xml");

                String library = getContents(versionLine);

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
                    ivyFile = getContents(versionLine);
                    library = library.replaceAll(".xml", "");

                    IvyPackage ivyPackage = new IvyPackage(orgName, moduleName, version, library);
                    localPackages.add(ivyPackage);
                }
                else // it's a javadoc or source
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

                        IvyPackage ivyPackage = jars.get(library);

                        if (ivyPackage != null)
                        {
                            ivyPackage.setHasSourceCode(true);
                        }
                    }

                    if (isJavadoc)
                    {

                        if (library.contains("-javadocs"))
                        {
                            library = library.replaceAll("-javadocs", "");
                        }

                        if (library.contains("-javadoc"))
                        {
                            library = library.replaceAll("-javadoc", "");
                        }

                        IvyPackage ivyPackage = jars.get(library);

                        if (ivyPackage != null)
                        {
                            ivyPackage.setHasJavaDocs(true);
                        }
                    }
                } // end if-else
            } // end if

            versionLine = reader.readLine();
        } // end while

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
            removeIvyFile(localPackages, ivyFile);
        }

        ivyPackages.addAll(localPackages);
    }

    private void removeIvyFile(List<IvyPackage> localPackages, String ivyFile)
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

    private boolean isAnythingOtherThanIvy(List<IvyPackage> localPackages)
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

    public List<IvyPackage> getMatchingLibraries()
    {
        return matchingLibraries;
    }
}
