package com.nurflugel.ivytracker.domain;

import ca.odell.glazedlists.matchers.Matcher;
import javax.swing.*;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Nov 28, 2008 Time: 6:47:40 PM To change this template use File | Settings | File Templates.
 */
public class CountValueMatcherEditor implements Matcher<IvyFile>
{
  private JRadioButton showAllLibrariesRadioButton;
  private JRadioButton showOnlyUsedLibrariesRadioButton;
  private JRadioButton showOnlyUnusedLibrariesRadioButton;

  public CountValueMatcherEditor(JRadioButton showAllLibrariesRadioButton, JRadioButton showOnlyUsedLibrariesRadioButton,
                                 JRadioButton showOnlyUnusedLibrariesRadioButton)
  {
    this.showAllLibrariesRadioButton        = showAllLibrariesRadioButton;
    this.showOnlyUsedLibrariesRadioButton   = showOnlyUsedLibrariesRadioButton;
    this.showOnlyUnusedLibrariesRadioButton = showOnlyUnusedLibrariesRadioButton;
  }

  public boolean matches(IvyFile item)
  {
    int count = item.getCount();

    if (showAllLibrariesRadioButton.isSelected())
    {
      return true;
    }

    if (showOnlyUnusedLibrariesRadioButton.isSelected() && (count == 0))
    {
      return true;
    }

    if (showOnlyUsedLibrariesRadioButton.isSelected() && (count > 0))
    {
      return true;
    }

    return false;
  }
}
