package com.nurflugel.externalsreporter.ui;

import com.nurflugel.common.ui.Util;
import static com.nurflugel.common.ui.Util.getUrlNoTrailingSlash;
import org.apache.commons.lang.builder.ToStringBuilder;

/** Representation of an external and the project that uses it. */
public class External implements Comparable, Selectable
{
  private String  url;
  private boolean selected = true;

  public External(String url, boolean selected)
  {
    this.url      = url;
    this.selected = selected;
  }

  // -------------------------- OTHER METHODS --------------------------
  public String getUrl()
  {
    return url;
  }

  @Override
  public boolean isSelected()
  {
    return selected;
  }

  @Override
  public void setSelected(boolean externalSelected)
  {
    this.selected = externalSelected;
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
    return new ToStringBuilder(this).append("url", url).append("selected", selected).toString();
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
