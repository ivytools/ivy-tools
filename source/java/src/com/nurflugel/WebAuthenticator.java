package com.nurflugel;

import com.nurflugel.externalsreporter.ui.Config;
import com.nurflugel.versionfinder.UsernamePasswordDialog;
import org.apache.commons.lang.StringUtils;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import static org.apache.commons.lang.StringUtils.*;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: May 30, 2008 Time: 5:38:09 PM To change this template use File | Settings | File Templates.
 */
public class WebAuthenticator extends Authenticator
{
  private static String userName;
  private static String password;
  private static Config config;

  // -------------------------- STATIC METHODS --------------------------

  public WebAuthenticator() {}

  public WebAuthenticator(Config config)
  {
    this.config = config;
    userName    = config.getUserName();
    password    = config.getPassword();
  }

  // -------------------------- OTHER METHODS --------------------------

  @Override
  public PasswordAuthentication getPasswordAuthentication()
  {
    if (isEmpty(userName) || isEmpty(password))
    {
      showDialog();
    }

    return new PasswordAuthentication(userName, (password.toCharArray()));
  }

  private static void showDialog()
  {
    UsernamePasswordDialog dialog = new UsernamePasswordDialog(userName, password);

    userName = dialog.getUsername();
    password = dialog.getPassword();

    if (config != null)
    {
      config.setUserName(userName);
      config.setPassword(password);
    }
  }

  public static String getUsername()
  {
    if (isEmpty(userName))
    {
      showDialog();
    }

    return userName;
  }

  public static String getPassword()
  {
    if (isEmpty(password))
    {
      showDialog();
    }

    return password;
  }
}
