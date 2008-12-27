package com.nurflugel.externalsreporter.ui;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jun 17, 2008 Time: 4:54:29 PM To change this template use File | Settings | File
 * Templates.
 */
public class External
{
    private String dir;
    private String url;

    public External(String dir, String url)
    {
        this.dir = dir;
        this.url = url;
    }

    public String getDir()
    {
        return dir;
    }

    public String getUrl()
    {
        return url;
    }
}
