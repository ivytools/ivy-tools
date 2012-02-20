package com.nurflugel.externalsreporter.ui;

import ca.odell.glazedlists.TextFilterator;
import java.util.List;

/** Filterator for the externals lists. */
@SuppressWarnings({ "unchecked" })
public class ProjectsFilterator implements TextFilterator
{
  @Override
  public void getFilterStrings(List list, Object element)
  {
    ProjectExternalReference reference = (ProjectExternalReference) element;

    list.add(reference.getProjectBaseUrl());
    list.add(reference.getExternalDir());
  }
}
