package com.nike.buildmaster.projects;

import static com.nike.buildmaster.projects.BuildScripts.BUILD;
import static com.nike.buildmaster.projects.BuildScripts.BUILD_DIST;
import static com.nike.buildmaster.projects.MasterProjects.ATLAS;
import static com.nike.buildmaster.projects.MasterProjects.NONE;
import static com.nike.buildmaster.projects.MasterProjects.OM;
import static com.nike.buildmaster.projects.Targets.*;

import java.util.ArrayList;
import java.util.List;


/**
 * An enumeratino of all buildable projects, with their "master" project, build script, and needed targets.
 * <p/>
 * This might be better as an XML file, however, this is pretty simple, and the program can just be recompiled as necessary.
 */
@SuppressWarnings({"EnumeratedConstantNamingConvention"})
public enum BuildableProjects
{
    ATLAS_ADMIN("http://subversion/svn/atlas.admin/", "Atlas Admin", "AA", ATLAS, BUILD, DISTRIBUTE, DISTRIBUTE_TOOLS),
    ATLAS_ORDER_CONFIRMATION("http://subversion/svn/atlas.orderconfirmation", "Atlas Order Confirmation", "AOC", ATLAS, BUILD, DISTRIBUTE_ATLAS),
    ATLAS_ORDER_QUERY("http://subversion/svn/atlas.orderquery", "Atlas Order Query", "AOQ", ATLAS, BUILD, DISTRIBUTE_ATLAS),
    ATLAS_ORDER_UPLOAD("http://subversion/svn/atlas.orderupload", "Atlas Order Upload", "AOU", ATLAS, BUILD, DISTRIBUTE_ATLAS),
    ATLAS_PLAN_ORDER_ID("http://subversion/svn/atlas.planorderid", "Atlas Plan Order Id", "APO", ATLAS, BUILD, DISTRIBUTE_ATLAS),
    ATLAS_PLAN_SERVICE("http://subversion/svn/atlas.planservice", "Atlas Plan Service", "APS", ATLAS, BUILD, DISTRIBUTE_ATLAS),
    ATLAS_PLAN_UPLOAD("http://subversion/svn/atlas.planupload", "Atlas Plan Upload", "APU", ATLAS, BUILD, DISTRIBUTE_ATLAS),
    ATLAS_PRODUCT_FEED("http://subversion/svn/atlas.productfeed", "Atlas Product Feed", "APF", ATLAS, BUILD, DISTRIBUTE_ATLAS),
    ATLAS_PRODUCT_QUERY("http://subversion/svn/atlas.productquery", "Atlas Product Query", "APQ", ATLAS, BUILD, DISTRIBUTE_ATLAS),
    ATLAS_REFERENCE_FEED("http://subversion/svn/atlas.referencefeed", "Atlas Reference Feed", "ARF", ATLAS, BUILD, DISTRIBUTE_ATLAS),
    ATLAS_CATALOG_SERVICE("http://camb2bp2:8090/svn/atlas.catalogservice", "Atlas Catalog Service", "ACS", ATLAS, BUILD, DISTRIBUTE),
    ATLAS_CUSTOMER_SERVICE("http://camb2bp2:8090/svn/atlas.customerservice", "Atlas Customer Service", "ACUS", ATLAS, BUILD, DISTRIBUTE),
    ATLAS_PRODUCT_SERVICE("http://camb2bp2:8090/svn/atlas.productservice", "Atlas Product Service", "APRS", ATLAS, BUILD, DISTRIBUTE),
    ATLAS_REFERENCE_SERVICE("http://camb2bp2:8090/svn/atlas.referenceservice", "Atlas Reference Service", "ARS", ATLAS, BUILD, DISTRIBUTE),
    ATLAS_USER_SERVICE("http://camb2bp2:8090/svn/atlas.userservice", "Atlas User Service", "AUS", ATLAS, BUILD, DISTRIBUTE_ATLAS),
    ATLAS_REFERENCE_QUERY("http://subversion/svn/atlas.referencequery", "Atlas Reference Query", "ARQ", ATLAS, BUILD, DISTRIBUTE_ATLAS),
    ATLAS_REP_ASSIGNMENT_FEED("http://subversion/svn/atlas.repassignmentfeed", "Atlas Rep Assignment Feed", "ARAF", ATLAS, BUILD, DISTRIBUTE_ATLAS),
    ATLAS_SHARED("http://subversion/svn/atlas.shared", "Atlas Shared", "AS", ATLAS, BUILD, DISTRIBUTE_DOMAIN),

    ADMIN("http://subversion/svn/admin/", "Admin", "ADM", NONE, BUILD, DISTRIBUTE, DISTRIBUTE_TOOLS),
    JAVA_SHARED("http://camb2bp2:8090/svn/javashared", "Java Shared", "JS", OM, BUILD, JAR_ALL),
    OM_SHARED("http://subversion/svn/omshared", "OM Shared", "OMS", OM, BUILD, JAR_ALL),
    CCT("http://subversion/svn/campaigncreationtool", "CCT", "CCT", OM, BUILD, DISTRIBUTE),
    CLAIMS_RETURNS("http://subversion/svn/claimsreturns", "Claims and Returns", "CR", OM, BUILD, DISTRIBUTE, FULFILLER_DISTRIBUTE, DISTRIBUTE_CONTENT),
    GOLF_CUSTOM_CLUBS("http://subversion/svn/golfcustomclubs", "Golf and Custom Clubs", "GCC", OM, BUILD, DISTRIBUTE),
    OM_BATCH("http://subversion/svn/ombatch", "OM Batch", "OMB", OM, BUILD, DISTRIBUTE),
    ORDER_CAPTURE("http://subversion/svn/ordercapture", "Order Capture", "OC", OM, BUILD_DIST, DISTRIBUTE),
    ORDER_CAPTURE_ADMIN("http://subversion/svn/ordercaptureadmin", "Order Capture Admin", "OCA", OM, BUILD, ALL),
    ORDER_STATUS("http://subversion/svn/orderstatus", "Order Status", "OS", OM, BUILD_DIST, DISTRIBUTE, DISTRIBUTE_CONTENT),
    ORDER_STATUS_DASHBOARD("http://subversion/svn/orderstatusdashboard", "Orderdashboard", "OSDB", OM, BUILD, DISTRIBUTE),
    DRP_SHARED("http://subversion/svn/drpshared", "Drp Shared", "DRP", OM, BUILD, DISTRIBUTE),
    PORTAL("http://subversion/svn/portal", "Portal", "P", OM, BUILD, DEPLOY_ALL, DISTRIBUTE),
    UPC("http://subversion/svn/upc", "UPC", "UPC", OM, BUILD, DISTRIBUTE),
    CDM("http://camb2bp2:8090/svn/cdm", "CDM", "CDM", NONE, BUILD, DISTRIBUTE, DISTRIBUTE_LOADERS),
    CDM_II("http://camb2bp2:8090/svn/cdmII", "CDM_II", "CDM_II", NONE, BUILD, DISTRIBUTE, DISTRIBUTE_LOADERS),
    G2P_EXTRACT_JOBS("http://subversion/svn/g2pextractjobs", "Go to Plan Extract Jobs", "G2P", NONE),
    SIMPLE_AVAILABILITY("http://subversion/svn/simpleavailability", "Simple Availability", "SA", NONE, BUILD, ALL);


    private String projectName;
    private String projectBaseUrl;
    private MasterProjects projectMaster;
    private String projectAbr;
    private Targets[] buildTargets;
    //    private String suggestedTagName;
    private BuildScripts buildScript;

    // -------------------------- STATIC METHODS --------------------------

    public static List<BuildableProjects> getProjectsForMaster(MasterProjects masterProject)
    {
        BuildableProjects[] buildableProjects = values();
        List<BuildableProjects> result = new ArrayList<BuildableProjects>();

        for (BuildableProjects project : buildableProjects)
        {

            if (project.getProjectMaster() == masterProject)
            {
                result.add(project);
            }
        }

        return result;
    }

    // --------------------------- CONSTRUCTORS ---------------------------

    BuildableProjects(String projectBaseUrl, String projectName, String projectAbr, MasterProjects projectMaster)
    {
        this.projectBaseUrl = projectBaseUrl;
        this.projectName = projectName;
        this.projectAbr = projectAbr;
        this.projectMaster = projectMaster;

        // default value
        buildScript = BUILD;
        buildTargets = new Targets[]{DISTRIBUTE};
    }

    BuildableProjects(String projectBaseUrl, String projectName, String projectAbr, MasterProjects projectMaster, BuildScripts buildScript,
                      Targets... buildTargets)
    {
        this(projectBaseUrl, projectName, projectAbr, projectMaster);
        this.buildScript = buildScript;
        this.buildTargets = buildTargets;
    }

    // -------------------------- OTHER METHODS --------------------------

    public BuildScripts getBuildScript()
    {
        return buildScript;
    }

    public Targets[] getBuildTargets()
    {
        return buildTargets;
    }

    public String getProjectBaseUrl()
    {
        return projectBaseUrl;
    }

    public MasterProjects getProjectMaster()
    {
        return projectMaster;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public String getProjectAbr()
    {
        return projectAbr;
    }
//    // todo this really doesn't belong here... but it's a nice way for all targets of a project to get the same tag name
//    public String getSuggestedTagName()
//    {
//        return suggestedTagName;
//    }
//
//    public void setSuggestedTagName(String suggestedTagName)
//    {
//        this.suggestedTagName = suggestedTagName;
//    }

    @Override
    public String toString()
    {
        return projectName;
    }

}
