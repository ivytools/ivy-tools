package com.nurflugel.ivybrowser.ui;

import ca.odell.glazedlists.TextFilterator;

import com.nurflugel.ivybrowser.domain.IvyPackage;

import java.util.List;


/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jan 25, 2008 Time: 10:36:57 PM To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({ "unchecked" })
public class IvyPackageFilterator implements TextFilterator
{
    public void getFilterStrings(List list, Object o)
    {
        IvyPackage item = (IvyPackage) o;
        list.add(item.getModuleName());
        list.add(item.getVersion());
        list.add(item.getLibrary());

        list.add(item.getOrgName());
    }
}
