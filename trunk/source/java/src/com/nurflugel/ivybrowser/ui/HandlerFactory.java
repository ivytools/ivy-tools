package com.nurflugel.ivybrowser.ui;

import ca.odell.glazedlists.EventList;

import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivybrowser.handlers.BaseWebHandler;
import com.nurflugel.ivybrowser.handlers.HtmlHandler;


/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Apr 27, 2009 Time: 10:45:52 PM To change this template use File | Settings | File Templates.
 */
public class HandlerFactory
{
    private HandlerFactory() { }

    public static BaseWebHandler getHandler(IvyBrowserMainFrame ivyBrowserMainFrame, String ivyRepositoryPath, EventList<IvyPackage> repositoryList)
    {

        // todo make this a factory to return the Subversion web dav handler by figuring out which one is which...
        return new HtmlHandler(ivyBrowserMainFrame, ivyRepositoryPath, repositoryList);
    }
}
