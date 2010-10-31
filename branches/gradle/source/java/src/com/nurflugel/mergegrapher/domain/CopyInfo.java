package com.nurflugel.mergegrapher.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Abstract class for representing the copying of information (creation or merge). */
public abstract class CopyInfo
{
  private Path sourcePath;
  private Long sourceRevision;
  private Long copyRevision;

  protected CopyInfo(Path sourcePath, long sourceRevision, long copyRevision)
  {
    this.sourcePath     = sourcePath;
    this.sourceRevision = sourceRevision;
    this.copyRevision   = copyRevision;
  }

  // --------------------- GETTER / SETTER METHODS ---------------------

  public Long getCopyRevision()
  {
    return copyRevision;
  }

  public void setCopyRevision(Long copyRevision)
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

  public Long getSourceRevision()
  {
    return sourceRevision;
  }

  public void setSourceRevision(Long sourceRevision)
  {
    this.sourceRevision = sourceRevision;
  }

  @Override
  public String toString()
  {
    return "CopyInfo{"
           + "sourcePath=" + sourcePath + ", sourceRevision=" + sourceRevision + ", copyRevision=" + copyRevision + '}';
  }

  public  List< Long> getInterestingRevisions(){
    List<Long> revisions=new ArrayList<Long>();
    if(sourceRevision!=null)revisions.add(sourceRevision);
    if(copyRevision!=null)revisions.add(copyRevision);


    return revisions;
  }

  public abstract void writeInfo(List<String> lines);
}
