package com.nurflugel.externalsreporter.ui;

import com.nurflugel.common.ui.Util;
import static com.nurflugel.common.ui.Util.getUrlNoTrailingSlash;

/** Representation of an external and the project that uses it. */
public class External implements Comparable
{
  private String  url;
  private boolean externalSelected = false;

  public External(String url)
  {
    this.url = url;
  }

  // -------------------------- OTHER METHODS --------------------------

  public String getUrl()
  {
    return url;
  }

  public boolean isSelected()
  {
    return externalSelected;
  }

  public void setSelected(boolean externalSelected)
  {
    this.externalSelected = externalSelected;
  }

  /** Get a key for Graphviz. */
  public String getKey()
  {
    return Util.filterUrlNames(url);
  }

  /** Get a label for Graphviz to display. */
  public String getLabel()
  {
    return Util.filterUrlNames(url);
  }

  @Override
  public String toString()
  {
    return "url=" + url;
  }

  @Override
  public int compareTo(Object o)
  {
    return url.compareTo(((External) o).getUrl());
  }

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

    External external    = (External) o;
    String   myUrl       = getUrlNoTrailingSlash(url);
    String   externalUrl = getUrlNoTrailingSlash(external.url);

    if ((myUrl != null) ? (!myUrl.equals(externalUrl))
                        : (externalUrl != null))
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = ((url != null) ? url.hashCode()
                                : 0);

    return result;
  }
}
