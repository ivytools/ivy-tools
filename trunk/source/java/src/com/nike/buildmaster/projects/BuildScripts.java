package com.nike.buildmaster.projects;

import java.io.File;


/** Enumeration of all possible build script names */
public enum BuildScripts
{
    BUILD("build.xml"),
    BUILD_DIST("build-dist.xml");

    private String scriptName;

    BuildScripts(String scriptName)
    {
        this.scriptName = scriptName;
    }

    public String getScriptName()
    {
        return "build" + File.separator + scriptName;
    }
}
