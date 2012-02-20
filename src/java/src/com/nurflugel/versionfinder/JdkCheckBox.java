package com.nurflugel.versionfinder;

import javax.swing.JCheckBox;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Oct 16, 2009 Time: 7:59:02 PM To change this template use File | Settings | File Templates.
 */
public class JdkCheckBox extends JCheckBox
{
  private Jdk jdk;

  public JdkCheckBox(Jdk jdk)
  {
    super(jdk.getName() + "+      (" + jdk.getVersion() + ")");
    this.jdk = jdk;
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public Jdk getJdk()
  {
    return jdk;
  }
}
