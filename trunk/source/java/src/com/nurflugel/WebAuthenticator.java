package com.nurflugel;

import com.nurflugel.versionfinder.UsernamePasswordDialog;
import static org.apache.commons.lang.StringUtils.isEmpty;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * This class deals with the need for user authentication when required. If the username and password are null (they get fetched from the UserConfig
 * class if you passed than into the constructor) or if you used the default constructor, then you'll get a username/password dialog box popup.
 *
 * <p>If the values aren't null (both of them), you never see anything.</p>
 *
 * <p>todo - add a "reset" option to flush passwords.</p>
 */
public class WebAuthenticator extends Authenticator
{
  private static String userName;
  private static String password;
  // private static UserConfig config;

  // -------------------------- STATIC METHODS --------------------------
  public WebAuthenticator()
  {
    userName = "";
    password = "";
  }

  public WebAuthenticator(String userName, String password)
  {
    this.userName = userName;
    this.password = password;
  }

  // -------------------------- OTHER METHODS --------------------------
  @Override
  public PasswordAuthentication getPasswordAuthentication()
  {
    // todo one way of doing this is to get X many requests within so many seconds...
    if (isEmpty(userName) || isEmpty(password))
    {
      showDialog();
    }

    return new PasswordAuthentication(userName, (password.toCharArray()));
  }

  public static void showDialog()
  {
    UsernamePasswordDialog dialog = new UsernamePasswordDialog(userName, password);

    userName = dialog.getUsername();
    password = dialog.getPassword();
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

  public static void setUserName(String userName)
  {
    WebAuthenticator.userName = userName;
  }

  public static void setPassword(String password)
  {
    WebAuthenticator.password = password;
  }
}
