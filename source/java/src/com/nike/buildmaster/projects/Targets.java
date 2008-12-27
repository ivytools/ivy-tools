package com.nike.buildmaster.projects;

/** Enum of possible build targets. */
@SuppressWarnings({"EnumeratedConstantNamingConvention"})
public enum Targets
{

    CLEAN_FULL("clean-full"),
    DISTRIBUTE("distribute"),
    DISTRIBUTE_ATLAS("clean-full, distribute"),
    DISTRIBUTE_TOOLS("distribute-tools"),
    DISTRIBUTE_CONTENT("distribute-content"),
    DISTRIBUTE_FULFILLER("distribute-fulfiller"),
    FULFILLER_DISTRIBUTE("fulfiller-distribute"),
    DISTRIBUTE_LOADERS("distribute-loaders"),
    DISTRIBUTE_DOMAIN("distribute-domain"),
    JAR_ALL("jar-all"),
    ALL("all"),
    DEPLOY_ALL("deploy-all");

    private String targetName;

    Targets(String name)
    {
        targetName = name;
    }

    public String getTargetName()
    {
        return targetName;
    }


    @Override
    public String toString()
    {
        return targetName;
    }
}
