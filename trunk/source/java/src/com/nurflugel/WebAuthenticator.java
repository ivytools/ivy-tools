package com.nurflugel;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: May 30, 2008 Time: 5:38:09 PM To change this template use File | Settings | File
 * Templates.
 */
public class WebAuthenticator extends Authenticator
{
    // todo - prompt user if these aren't set, Google doesn't require authentication, but other guys will, and this shouldn't be
    // hard-coded.  Put this into Preferences, keyed under the base URL or something...
    private static final String USER_NAME = "user_name";
    private static final String PASSWORD  = "password";

    @Override public PasswordAuthentication getPasswordAuthentication()
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
