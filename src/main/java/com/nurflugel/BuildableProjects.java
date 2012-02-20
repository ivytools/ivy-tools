package com.nurflugel;

/**
 * An enumeratino of all buildable projects, with their "master" project, build script, and needed targets.
 *
 * <p>This might be better as an XML file, however, this is pretty simple, and the program can just be recompiled as necessary.</p>
 */
@SuppressWarnings({ "EnumeratedConstantNamingConvention" })
public class BuildableProjects
{
  private String projectName;
  private String projectBaseUrl;

  // private MasterProjects projectMaster;
  private String projectAbr;
  // ------------------------ CANONICAL METHODS ------------------------

  // // todo this really doesn't belong here... but it's a nice way for all targets of a project to get the same tag name
  // public String getSuggestedTagName()
  // {
  // return suggestedTagName;
  // }
  //
  // public void setSuggestedTagName(String suggestedTagName)
  // {
  // this.suggestedTagName = suggestedTagName;
  // }
  @Override
  public String toString()
  {
    return projectName;
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public String getProjectAbr()
  {
    return projectAbr;
  }

  // private Targets[] buildTargets;
  // private String suggestedTagName;
  // private BuildScripts buildScript;
  // public static List<BuildableProjects> getProjectsForMaster(MasterProjects masterProject)
  // {
  // BuildableProjects[] buildableProjects = values();
  // List<BuildableProjects> result = new ArrayList<BuildableProjects>();
  //
  // for (BuildableProjects project : buildableProjects)
  // {
  //
  // if (project.getProjectMaster() == masterProject)
  // {
  // result.add(project);
  // }
  // }
  //
  // return result;
  // }
  // --------------------------- CONSTRUCTORS ---------------------------
  // BuildableProjects(String projectBaseUrl, String projectName, String projectAbr, MasterProjects projectMaster)
  // {
  // this.projectBaseUrl = projectBaseUrl;
  // this.projectName = projectName;
  // this.projectAbr = projectAbr;
  // this.projectMaster = projectMaster;
  //
  // // default value
  // buildScript = BUILD;
  // buildTargets = new Targets[]{DISTRIBUTE};
  // }
  //
  // BuildableProjects(String projectBaseUrl, String projectName, String projectAbr, MasterProjects projectMaster, BuildScripts buildScript,
  // Targets... buildTargets)
  // {
  // this(projectBaseUrl, projectName, projectAbr, projectMaster);
  // this.buildScript = buildScript;
  // this.buildTargets = buildTargets;
  // }
  //
  // // -------------------------- OTHER METHODS --------------------------
  //
  // public BuildScripts getBuildScript()
  // {
  // return buildScript;
  // }
  //
  // public Targets[] getBuildTargets()
  // {
  // return buildTargets;
  // }
  public String getProjectBaseUrl()
  {
    return projectBaseUrl;
  }

  // public MasterProjects getProjectMaster()
  // {
  // return projectMaster;
  // }
  public String getProjectName()
  {
    return projectName;
  }
}
