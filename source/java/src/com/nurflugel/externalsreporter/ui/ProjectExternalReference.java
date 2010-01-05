package com.nurflugel.externalsreporter.ui;

import com.nurflugel.common.ui.Util;
import static com.nurflugel.common.ui.Util.getUrlNoTrailingSlash;

/** Representation of an external and the project that uses it. */
public class ProjectExternalReference implements Comparable, Selectable
{
  private String   baseUrl;
  private String   externalDir;
  private External external;
  private boolean  isSelected = true;

  public ProjectExternalReference(String baseUrl, String externalDir, External external)
  {
    this.baseUrl     = baseUrl;
    this.externalDir = externalDir;
    this.external    = external;
  }

  // -------------------------- OTHER METHODS --------------------------

  public External getExternal()
  {
    return external;
  }

  public String getProjectBaseUrl()
  {
    return baseUrl;
  }

  public String getExternalDir()
  {
    return externalDir;
  }

  public boolean isSelected()
  {
    return isSelected;
  }

  public void setSelected(boolean selected)
  {
    this.isSelected = selected;
  }

  /** Get a key for Graphviz. */
  public String getKey()
  {
    return Util.filterUrlNames(baseUrl) + externalDir;
  }

  /** Get a label for Graphviz to display. */
  public String getLabel()
  {
    return Util.filterUrlNames(baseUrl) + "\\n" + externalDir;
  }

  @Override
  public String toString()
  {
    return "Base URL: " + baseUrl + "\tdir=" + externalDir + "\texternal=" + external.getUrl();
  }

  @Override
  public int compareTo(Object o)
  {
    ProjectExternalReference other = (ProjectExternalReference) o;

    return (baseUrl + externalDir).compareTo(other.getProjectBaseUrl() + other.getExternalDir());
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

    ProjectExternalReference other        = (ProjectExternalReference) o;

    String                   otherBaseUrl = getUrlNoTrailingSlash(other.baseUrl);
    String                   myUrl        = getUrlNoTrailingSlash(baseUrl);

    if ((myUrl != null) ? (!myUrl.equals(otherBaseUrl))
                        : (otherBaseUrl != null))
    {
      return false;
    }

    if ((externalDir != null) ? (!externalDir.equals(other.externalDir))
                              : (other.externalDir != null))
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = (baseUrl != null) ? baseUrl.hashCode()
                                   : 0;

    result = (31 * result) + ((externalDir != null) ? externalDir.hashCode()
                                                    : 0);

    return result;
  }
}
