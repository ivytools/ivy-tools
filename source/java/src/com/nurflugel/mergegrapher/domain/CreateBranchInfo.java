package com.nurflugel.mergegrapher.domain;

/** This represents the creation of a branch. */
public class CreateBranchInfo extends CopyInfo
{
  /** The branch that was created. */
  private Path createdPath;

  protected CreateBranchInfo(Path sourcePath, long sourceRevision, long copyRevision, Path createdPath)
  {
    super(sourcePath, sourceRevision, copyRevision);
    this.createdPath = createdPath;
  }

  // --------------------- GETTER / SETTER METHODS ---------------------

  public Path getCreatedPath()
  {
    return createdPath;
  }

  public void setCreatedPath(Path createdPath)
  {
    this.createdPath = createdPath;
  }

  @Override
  public String toString()
  {
    return "CreateBranchInfo{"
           + "createdPath=" + createdPath + "} " + super.toString();
  }
}
