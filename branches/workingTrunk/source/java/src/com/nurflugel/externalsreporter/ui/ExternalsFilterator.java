package com.nurflugel.externalsreporter.ui;

import ca.odell.glazedlists.TextFilterator;
import java.util.List;

/** Filterator for the externals lists. */
@SuppressWarnings({ "unchecked" })
public class ExternalsFilterator implements TextFilterator
{
  @Override
  public void getFilterStrings(List list, Object element)
  {
    External external = (External) element;

    list.add(external.getUrl());
  }
}
