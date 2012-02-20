package com.nurflugel.externalsreporter.ui;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jan 13, 2010 Time: 10:48:56 AM To change this template use File | Settings | File Templates.
 */
public interface UserConfig
{
  String getPassword();
  String getUserName();
  void setPassword(String password);
  void setUserName(String userName);
}
