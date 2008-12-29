package com.nurflugel.ivytracker.domain;

import ca.odell.glazedlists.EventList;

import com.nurflugel.ivytracker.IvyTrackerMainFrame;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import org.jdom.input.SAXBuilder;

import java.io.IOException;

import java.net.URL;

import java.util.*;


/** A representation of an item in the Ivy repository. */
@SuppressWarnings({ "CallToPrintStackTrace" })
public class IvyFileImpl implements IvyFile
{
    private List<String>     dependencies = new ArrayList<String>();
    private String           org;
    private String           module;
    private String           version;
    private static IvyTrackerMainFrame mainFrame;
    private int              count;


    public IvyFileImpl(String org, String module, String version, URL url, Collection<IvyFile> ivyFiles, Map<String, IvyFile> ivyFilesMap, IvyTrackerMainFrame mainFrame, EventList<IvyFile> repositoryList)
    {
        this.org       = org;
        this.module    = module;
        this.version   = version;
        this.mainFrame = mainFrame;

        addToFiles(org, module, version, ivyFilesMap, ivyFiles, this, repositoryList);

        try {
            SAXBuilder builder = new SAXBuilder();
            Document   doc     = builder.build(url);
            Element    root    = doc.getRootElement();
            Element    child1  = root.getChild("dependencies");

            if (child1 != null) {
                List children = child1.getChildren("dependency");

                for (Object child : children) {
                    Element childElement = (Element) child;
                    String  childOrg     = childElement.getAttribute("org").getValue();
                    String  childName    = childElement.getAttribute("name").getValue();
                    String  childRev     = childElement.getAttribute("rev").getValue();
                    String  dependency   = getKey(childOrg, childName, childRev);
                    String  text         = "    " + dependency;
                    System.out.println(text);
                    mainFrame.setStatusLabel(text);

                    if (!dependencies.contains(dependency)) {
                        dependencies.add(dependency);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JDOMException e) {
            e.printStackTrace();
        }
    }

    private IvyFile addToFiles(String org, String module, String version, Map<String, IvyFile> ivyFilesMap, Collection<IvyFile> ivyFiles, IvyFile ivyFile, EventList<IvyFile> repositoryList)
    {
        String key = getKey(org, module, version);

        // see if we've already got this file in the map
        IvyFile testFile = ivyFilesMap.get(key);
        System.out.println(key);
        mainFrame.setStatusLabel(key);

        IvyFile returnValue;

        if (testFile == null) // we dont' have it?  Add it..
        {
            ivyFiles.add(ivyFile);
            ivyFilesMap.put(key, ivyFile);

            returnValue = ivyFile;
        } else // we do have it?  Fine, return what we already have...
        {
            returnValue = testFile;
        }

        if (!repositoryList.contains(returnValue)) {
            repositoryList.add(returnValue);
        }

        return returnValue;
    }

    public static String getKey(String org, String module, String version) { return org + ":" + module + ":" + version; }

    public String getKey() { return getKey(org, module, version); }


    public List<String> getDependencies() { return dependencies; }

    public String getOrg() { return org; }

    public String getModule() { return module; }

    public String getVersion() { return version; }

    public void touch()
    {
        String text = "Touching " + getKey();
        System.out.println(text);

        // mainFrame.setStatusLabel(text);
        count++;
    }

    public int getCount() { return count; }

    @Override public String toString() { return org + " " + module + " " + version + " " + count; }
}
