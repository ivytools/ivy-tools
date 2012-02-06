package com.nurflugel.ivygrapher;

import com.nurflugel.Os;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/** Engine for the Ivy Grapher. */
@SuppressWarnings({ "CallToPrintStackTrace" })
public class XmlHandler
{
  private static final String ORGANISATION = "organisation";
  private static final String NAME         = "name";
  private static final String REVISION     = "revision";
  private static final String CALLER       = "caller";
  private static final String MODULE       = "module";
  private static final String DEPENDENCIES = "dependencies";
  private static final String CONF         = "conf";
  private static final String CONFS        = "confs";
  private static final String CALLERREV    = "callerrev";
  private static final String REV          = "rev";

  // -------------------------- OTHER METHODS --------------------------
  void processXmlFile(File fileToGraph, Preferences preferences, NodeOrder nodeOrder, Os os, OutputFormat outputFormat, String dotExecutablePath,
                      boolean deleteDotFileOnExit, boolean concentrateEdges) throws JDOMException, IOException
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
      parseModule(moduleMap, oModule);
    }

    IvyGrapherGraphVizHandler handler = new IvyGrapherGraphVizHandler(nodeOrder, os, outputFormat, dotExecutablePath, deleteDotFileOnExit,
                                                                      concentrateEdges);
    File dotFile = handler.generateDotFile(fileToGraph, ivyModule, moduleMap);

    handler.processDotFile(dotFile);
  }

  /** Parse the element for the module. */
  private void parseModule(Map<String, Module> moduleMap, Object oModule)
  {
    Element moduleElement = (Element) oModule;
    String  organization  = moduleElement.getAttributeValue(ORGANISATION);
    String  name          = moduleElement.getAttributeValue(NAME);
    Module  module        = addModuleToMap(organization, name, null, moduleMap);
    List    revisions     = moduleElement.getChildren(REVISION);

    for (Object oRevision : revisions)
    {
      parseRevision(moduleMap, module, oRevision);
    }
  }

  /** Parse the element for the revision - avoid evicted revisions. */
  private void parseRevision(Map<String, Module> moduleMap, Module module, Object oRevision)
  {
    Element revision = (Element) oRevision;
    String  evicted  = revision.getAttributeValue("evicted");

    if (evicted == null)
    {
      Element artifacts = revision.getChild("artifacts");
      List    children  = artifacts.getChildren();

      if (!children.isEmpty())
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
