package com.nurflugel.ivytracker.handlers;

/** Created by IntelliJ IDEA. User: douglasbullard Date: Jan 8, 2009 Time: 9:41:53 PM To change this template use File | Settings | File Templates. */
public class ProjectIvyFile
{
  private String pathToIvyFile;
  private String projectName;

  public ProjectIvyFile(String pathToIvyFile, String projectName)
  {
    this.pathToIvyFile = pathToIvyFile;
    this.projectName   = projectName;
  }

  public String getPathToIvyFile()
  {
    return pathToIvyFile;
  }

  public String getProjectName()
  {
    return projectName;
  }
}
