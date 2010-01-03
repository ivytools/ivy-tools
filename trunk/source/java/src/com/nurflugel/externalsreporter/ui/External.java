package com.nurflugel.externalsreporter.ui;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jun 17, 2008 Time: 4:54:29 PM To change this template use File | Settings | File Templates.
 */
public class External
{
    private String baseUrl;
  private String dir;
  private String url;

  public External(String baseUrl,String dir, String url)
  {
      this.baseUrl = baseUrl;
      this.dir = dir;
    this.url = url;
  }

// -------------------------- OTHER METHODS --------------------------

    public String getProjectBaseUrl() {
        return baseUrl;
    }

  public String getDir()
  {
    return dir;
  }

  public String getUrl()
  {
    return url;
  }

    @Override
    public String toString() {
        return "Base URL: "+baseUrl+"\tdir="+dir+"\turl="+url;
    }
}
