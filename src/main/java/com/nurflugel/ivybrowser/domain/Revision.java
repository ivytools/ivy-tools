package com.nurflugel.ivybrowser.domain;

/** Created by IntelliJ IDEA. User: douglasbullard Date: Jul 6, 2010 Time: 7:33:22 PM To change this template use File | Settings | File Templates. */
public class Revision implements Comparable
{
  public static final Revision NO_REVISION    = new Revision(-1);
  private long                 revisionNumber;

  public Revision() {}

  public Revision(long revisionNumber)
  {
    this.revisionNumber = revisionNumber;
  }
  // ------------------------ INTERFACE METHODS ------------------------

  // --------------------- Interface Comparable ---------------------
  @Override
  public int compareTo(Object o)
  {
    Revision other = (Revision) o;
    long     num   = other.getRevisionNumber();

    if (num == revisionNumber)
    {
      return 0;
    }

    if (num < revisionNumber)
    {
      return 1;
    }

    return -1;
  }

  // -------------------------- OTHER METHODS --------------------------
  public boolean isRealRevision()
  {
    return revisionNumber != NO_REVISION.getRevisionNumber();
  }

  // ------------------------ CANONICAL METHODS ------------------------
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

    Revision revision = (Revision) o;

    if (revisionNumber != revision.revisionNumber)
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    return (int) (revisionNumber ^ (revisionNumber >>> 32));
  }

  @Override
  public String toString()
  {
    return "" + revisionNumber;
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public long getRevisionNumber()
  {
    return revisionNumber;
  }

  public void setRevisionNumber(long revisionNumber)
  {
    this.revisionNumber = revisionNumber;
  }
}
