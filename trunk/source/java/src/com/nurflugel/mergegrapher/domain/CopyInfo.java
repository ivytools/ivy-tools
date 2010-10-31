package com.nurflugel.mergegrapher.domain;

import com.nurflugel.ivybrowser.domain.Revision;
import java.util.ArrayList;
import java.util.List;

/** Abstract class for representing the copying of information (creation or merge). */
public abstract class CopyInfo
{
  private Path     sourcePath;
  private Revision sourceRevision;
  private Revision copyRevision;

  protected CopyInfo(Path sourcePath, Revision sourceRevision, Revision copyRevision)
  {
    this.sourcePath     = sourcePath;
    this.sourceRevision = sourceRevision;
    this.copyRevision   = copyRevision;
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public Revision getCopyRevision()
  {
    return copyRevision;
  }

  public void setCopyRevision(Revision copyRevision)
  {
    this.copyRevision = copyRevision;
  }

  public Path getSourcePath()
  {
    return sourcePath;
  }

  public void setSourcePath(Path sourcePath)
  {
    this.sourcePath = sourcePath;
  }

  public Revision getSourceRevision()
  {
    return sourceRevision;
  }

  public void setSourceRevision(Revision sourceRevision)
  {
    this.sourceRevision = sourceRevision;
  }

  @Override
  public String toString()
  {
    return "CopyInfo{"
             + "sourcePath=" + sourcePath + ", sourceRevision=" + sourceRevision + ", copyRevision=" + copyRevision + '}';
  }

  public List<Revision> getInterestingRevisions()
  {
    List<Revision> revisions = new ArrayList<Revision>();

    if (sourceRevision != null)
    {
      revisions.add(sourceRevision);
    }

    if (copyRevision != null)
    {
      revisions.add(copyRevision);
    }

    return revisions;
  }

  public abstract void writeInfo(List<String> lines);
}
