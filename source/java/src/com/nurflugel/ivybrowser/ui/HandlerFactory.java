package com.nurflugel.ivybrowser.ui;

import ca.odell.glazedlists.EventList;
import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivybrowser.handlers.BaseWebHandler;
import com.nurflugel.ivybrowser.handlers.HtmlHandler;
import com.nurflugel.ivybrowser.handlers.SubversionWebDavHandler;


/** todo make this a factory to return the Subversion web dav handler by figuring out which one is which... */
public class HandlerFactory
{
    private static final boolean IS_WEB_REPOSITORY = true;

    private HandlerFactory()
    {
    }

    public static BaseWebHandler getHandler(IvyBrowserMainFrame ivyBrowserMainFrame, String ivyRepositoryPath, EventList<IvyPackage> repositoryList)
    {
        if (IS_WEB_REPOSITORY)
        {
            return new HtmlHandler(ivyBrowserMainFrame, ivyRepositoryPath, repositoryList);
        }
        else
        {
            return new SubversionWebDavHandler(ivyBrowserMainFrame, ivyRepositoryPath, repositoryList);
        }
    }
}
