package com.nurflugel.mergegrapher.domain;

/** Abstract class for representing the copying of information (creation or merge). */
public abstract class CopyInfo
{
  private Path sourcePath;
  private long sourceRevision;
  private long copyRevision;

  protected CopyInfo(Path sourcePath, long sourceRevision, long copyRevision)
  {
    this.sourcePath     = sourcePath;
    this.sourceRevision = sourceRevision;
    this.copyRevision   = copyRevision;
  }

  // --------------------- GETTER / SETTER METHODS ---------------------

  public long getCopyRevision()
  {
    return copyRevision;
  }

  public void setCopyRevision(long copyRevision)
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

  public long getSourceRevision()
  {
    return sourceRevision;
  }

  public void setSourceRevision(long sourceRevision)
  {
    this.sourceRevision = sourceRevision;
  }

  @Override
  public String toString()
  {
    return "CopyInfo{"
           + "sourcePath=" + sourcePath + ", sourceRevision=" + sourceRevision + ", copyRevision=" + copyRevision + '}';
  }
}
