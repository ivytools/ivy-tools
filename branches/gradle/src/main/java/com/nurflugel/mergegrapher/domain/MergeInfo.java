package com.nurflugel.mergegrapher.domain;

import com.nurflugel.ivybrowser.domain.Revision;
import java.util.ArrayList;
import java.util.List;

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

  protected MergeInfo(Path sourcePath, Revision sourceRevision, Revision copyRevision, Path destinationPath, String mergeRevisions)
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

  public List<Revision> getInterestingRevisions()
  {
    List<Revision> revisions = new ArrayList<Revision>();

    if (getSourceRevision() != null)
    {
      revisions.add(getSourceRevision());
    }

    if (getCopyRevision() != null)
    {
      revisions.add(getCopyRevision());
    }

    // todo merge revisions?
    return revisions;
  }

  @Override
  public void writeInfo(List<String> lines)
  {
    // todo something
  }
}
