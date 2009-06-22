package com.nurflugel.ivytracker.domain;

import ca.odell.glazedlists.TextFilterator;

import java.util.List;

/** Created by IntelliJ IDEA. User: douglasbullard Date: Jan 25, 2008 Time: 10:36:57 PM To change this template use File | Settings | File Templates. */
@SuppressWarnings({ "unchecked" })
public class IvyFileFilterator implements TextFilterator
{
    public void getFilterStrings(List list, Object o)
    {
        IvyFile item = (IvyFile) o;

        list.add(item.getModule());
        list.add(item.getVersion());
        list.add(item.getOrg());
    }
}
