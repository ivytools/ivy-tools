package com.nurflugel.ivygrapher;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import javax.swing.*;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.FILES_ONLY;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Sep 29, 2009 Time: 6:10:55 PM To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({ "CallToPrintStackTrace" })
public class IvyGrapher
{
  private File[]              filesToGraph;
  private String              lastVisitedDir;
  private Preferences         preferences       = Preferences.userNodeForPackage(IvyGrapher.class);
  private static final String ORGANISATION      = "organisation";
  private static final String NAME              = "name";
  private static final String REVISION          = "revision";
  private static final String CALLER            = "caller";
  private static final String MODULE            = "module";
  private static final String DEPENDENCIES      = "dependencies";
  private static final String DIR               = "dir";
  private static final String CONF              = "conf";
  private static final String SUCCESSFUL        = "successful";
  private static final String METADATA_ARTIFACT = "metadata-artifact";
  private static final String STATUS            = "status";
  private static final String CONFS             = "confs";
  private static final String CALLERREV         = "callerrev";
  private static final String REV               = "rev";

  public IvyGrapher(String[] args)
  {
    String fileName = null;

    if (fileName != null)
    {
      filesToGraph = new File[] { new File(fileName) };
    }

    if (args.length > 0)
    {
      List<File> files = new ArrayList<File>();

      for (String arg : args)
      {
        files.add(new File(arg));
      }

      filesToGraph = files.toArray(new File[files.size()]);
    }
  }

  public static void main(String[] args)
  {
    IvyGrapher grapher = new IvyGrapher(args);

    try
    {
      grapher.createGraph();
    }
    catch (JDOMException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  private void createGraph() throws JDOMException, IOException
  {
    // todo add a finder UI for DOT location
    if (filesToGraph == null)
    {
      JFileChooser chooser = new JFileChooser();

      chooser.setDialogTitle("Select the Ivy file");

      String dirName = preferences.get(DIR, null);

      if (dirName != null)
      {
        File lastDir = new File(dirName);

        chooser.setCurrentDirectory(lastDir);
      }

      chooser.setFileSelectionMode(FILES_ONLY);
      chooser.setMultiSelectionEnabled(true);

      int returnVal = chooser.showDialog(null, "Use these files");

      if (returnVal == APPROVE_OPTION)
      {
        filesToGraph = chooser.getSelectedFiles();

        if (filesToGraph.length > 0)
        {
          dirName = filesToGraph[0].getParent();
          preferences.put(DIR, dirName);
        }
      }
    }

    for (File fileToGraph : filesToGraph)
    {
      processXmlFile(fileToGraph);
    }
  }

  private void processXmlFile(File fileToGraph) throws JDOMException, IOException
  {
    Map<String, Module> moduleMap    = new HashMap<String, Module>();
    SAXBuilder          builder      = new SAXBuilder();
    Document            doc          = builder.build(fileToGraph);
    Element             root         = doc.getRootElement();
    Element             info         = root.getChild("info");
    String              ivyOrg       = info.getAttributeValue(ORGANISATION);
    String              infoModule   = info.getAttributeValue(MODULE);
    String              infoRevision = info.getAttributeValue(REVISION);
    String              infoConf     = info.getAttributeValue(CONF);
    String              infoConfs    = info.getAttributeValue(CONFS);

    Module              ivyModule    = addModuleToMap(ivyOrg, infoModule, infoRevision, moduleMap);

    Element             dependencies = root.getChild(DEPENDENCIES);
    List                modules      = dependencies.getChildren(MODULE);

    for (Object oModule : modules)
    {
      Element moduleElement = (Element) oModule;
      String  organization  = moduleElement.getAttributeValue(ORGANISATION);
      String  name          = moduleElement.getAttributeValue(NAME);
      Module  module        = addModuleToMap(organization, name, null, moduleMap);
      List    revisions     = moduleElement.getChildren(REVISION);

      for (Object oRevision : revisions)
      {
        Element revision = (Element) oRevision;
        String  evicted  = revision.getAttributeValue("evicted");

        if (evicted == null)
        {
          String revisionNumber = revision.getAttributeValue(NAME);

          module.setRevision(revisionNumber);

          List callerList = revision.getChildren(CALLER);

          for (Object oCaller : callerList)
          {
            Element callerElement           = (Element) oCaller;
            String  callerOrganization      = callerElement.getAttributeValue(ORGANISATION);
            String  callerName              = callerElement.getAttributeValue(NAME);
            String  callerConf              = callerElement.getAttributeValue(CONF);
            String  callerPreferredRevision = callerElement.getAttributeValue(REV);
            String  callersRevision         = callerElement.getAttributeValue(CALLERREV);
            Module  caller                  = addModuleToMap(callerOrganization, callerName, callersRevision, moduleMap);

            module.addCaller(caller, callerPreferredRevision);
          }
        }
      }
    }

    GraphVizHandler handler = new GraphVizHandler(preferences);

    File            dotFile = handler.generateDotFile(fileToGraph, ivyModule, moduleMap);

    handler.processDotFile(dotFile);
  }

  private Module addModuleToMap(String organization, String name, String revision, Map<String, Module> moduleMap)
  {
    String key    = Module.generateKey(organization, name);

    Module module = moduleMap.get(key);

    if (module == null)
    {
      System.out.println("Couldn't find existing module for " + key);
      module = new Module(organization, name, revision);
      moduleMap.put(module.getNiceXmlKey(), module);
    }
    else
    {
      if (revision != null)
      {
        module.setRevision(revision);
      }
    }

    return module;
  }
}
