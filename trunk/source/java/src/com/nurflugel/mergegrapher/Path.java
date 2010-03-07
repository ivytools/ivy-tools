package com.nurflugel.mergegrapher;

import org.apache.commons.lang.builder.ToStringBuilder;

/** This is a representation of a path - could be trunk a branch, or a tag. */
public class Path
{
  /** the revision the path was created in. */
  private Long creationRevision;

  /** the revision the path was deleted in. */
  private Long deleteRevision;

  /** the branch this was created from. */
  private Path copyPath;               // todo should this be another path?

  // headRevision???
  /** the path, minus the base URL of the project. */
  private String pathName;

  public Path(String pathName)
  {
    this.pathName = pathName;
  }

  // -------------------------- OTHER METHODS --------------------------

  @Override
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }

    if ((o == null) || (getClass() != o.getClass()))
    {
      return false;
    }

    Path path = (Path) o;

    if ((pathName != null) ? (!pathName.equals(path.pathName))
                           : (path.pathName != null))
    {
      return false;
    }

    return true;
  }

  public Path getCopyPath()
  {
    return copyPath;
  }

  public void setCopyPath(Path copyPath)
  {
    this.copyPath = copyPath;
  }

  public Long getCreationRevision()
  {
    return creationRevision;
  }

  public void setCreationRevision(Long creationRevision)
  {
    this.creationRevision = creationRevision;
  }

  public Long getDeleteRevision()
  {
    return deleteRevision;
  }

  public void setDeleteRevision(Long deleteRevision)
  {
    this.deleteRevision = deleteRevision;
  }

  public String getPathName()
  {
    return pathName;
  }

  @Override
  public int hashCode()
  {
    return (pathName != null) ? pathName.hashCode()
                              : 0;
  }

  @Override
  public String toString()
  {
    return "Path{"
           + "pathName='" + pathName + '\'' + ", creationRevision=" + creationRevision + ", deleteRevision=" + deleteRevision + ", copyPath="
           + copyPath + '}';
  }
}
