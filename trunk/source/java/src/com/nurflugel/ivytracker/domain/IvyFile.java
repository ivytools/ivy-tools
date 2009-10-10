package com.nurflugel.ivytracker.domain;

import java.util.List;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Nov 23, 2008 Time: 10:18:27 PM To change this template use File | Settings | File Templates.
 */
public interface IvyFile
{
  // -------------------------- OTHER METHODS --------------------------

  int getCount();
  List<String> getDependencies();
  String getKey();
  String getModule();
  String getOrg();
  String getVersion();
  void touch();
}
