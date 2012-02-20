package com.nurflugel.ivytracker.handlers;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jan 31, 2010 Time: 5:53:46 PM To change this template use File | Settings | File Templates.
 */
public abstract class IvyFileFinderHandler extends SwingWorker<Object, Object>
{
  public abstract void doIt();
}
