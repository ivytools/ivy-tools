package com.nurflugel.ivytracker.domain;

import java.util.List;

/** Created by IntelliJ IDEA. User: douglasbullard Date: Nov 23, 2008 Time: 10:18:27 PM To change this template use File | Settings | File Templates. */
public interface IvyFile
{
    String getOrg();

    String getModule();

    String getVersion();

    String getKey();

    void touch();

    int getCount();

    List<String> getDependencies();
}
