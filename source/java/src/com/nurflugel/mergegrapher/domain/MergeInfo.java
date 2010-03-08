package com.nurflugel.mergegrapher.domain;

/** Representation of a merge. */
public class MergeInfo extends CopyInfo
{
  /** The branch that the information represented by this object is being put into. */
  private Path destinationPath;

  /**
   * Since this is potentially a list of comma and hyphen-delimited strings, we're just going to represent this by a string instead of trying to deal
   * with all the possibilities of the range of revisions.
   */
  private String mergeRevisions;

  protected MergeInfo(Path sourcePath, long sourceRevision, long copyRevision, Path destinationPath, String mergeRevisions)
  {
    super(sourcePath, sourceRevision, copyRevision);
    this.destinationPath = destinationPath;
    this.mergeRevisions  = mergeRevisions;
  }

  // --------------------- GETTER / SETTER METHODS ---------------------

  public Path getDestinationPath()
  {
    return destinationPath;
  }

  public void setDestinationPath(Path destinationPath)
  {
    this.destinationPath = destinationPath;
  }

  public String getMergeRevisions()
  {
    return mergeRevisions;
  }

  public void setMergeRevisions(String mergeRevisions)
  {
    this.mergeRevisions = mergeRevisions;
  }

  @Override
  public String toString()
  {
    return "MergeInfo{"
           + "destinationPath=" + destinationPath + ", mergeRevisions='" + mergeRevisions + '\'' + "} " + super.toString();
  }
}
