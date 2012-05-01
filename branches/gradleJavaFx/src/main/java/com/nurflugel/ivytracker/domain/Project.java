package com.nurflugel.ivytracker.domain;

/** A representation of a project which uses Ivy. */
public class Project implements Comparable
{
  private boolean isIncluded;
  private String  projectUrl;
  private String  ivyFile;

  public Project(String projectUrl)
  {
    this.projectUrl = projectUrl;
  }

  public boolean isIncluded()
  {
    return isIncluded;
  }

  public void setIncluded(boolean included)
  {
    isIncluded = included;
  }

  public String getProjectUrl()
  {
    return projectUrl;
  }

  public void setProjectUrl(String projectUrl)
  {
    this.projectUrl = projectUrl;
  }

  @Override
  public int compareTo(Object o)
  {
    return projectUrl.compareTo(((Project) o).getProjectUrl());
  }

  public void setIvyFile(String ivyFile)
  {
    this.ivyFile = ivyFile;
  }

  public String getIvyFile()
  {
    return ivyFile;
  }
}
