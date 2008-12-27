package com.nike.buildmaster;

import java.net.Authenticator;
import java.net.PasswordAuthentication;


/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: May 30, 2008 Time: 5:38:09 PM To change this template use File | Settings | File
 * Templates.
 */
public class WebAuthenticator extends Authenticator
{
    private static final String USER_NAME = "a.clearcase.b2bbuild";
    private static final String PASSWORD = "bl00n1te";

    @Override
    public PasswordAuthentication getPasswordAuthentication()
    {
        return new PasswordAuthentication(USER_NAME, (PASSWORD.toCharArray()));
    }

    public static String getUsername()
    {
        return USER_NAME;
    }

    public static String getPassword()
    {
        return PASSWORD;
    }
}
