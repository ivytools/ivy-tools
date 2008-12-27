package com.nurflugel.ivybrowser.ui;

import com.nurflugel.ivybrowser.domain.IvyPackage;

import javax.swing.*;


/** Created by IntelliJ IDEA. User: douglasbullard Date: Jan 27, 2008 Time: 5:46:26 PM To change this template use File | Settings | File Templates. */
public class IvyPackageCheckbox extends JCheckBox
{
    private IvyPackage ivyPackage;

    public IvyPackageCheckbox(IvyPackage ivyPackage)
    {
        this.ivyPackage = ivyPackage;
        setText(ivyPackage.getPrettyText());
        setSelected(true);
    }

    public IvyPackage getIvyPackage() { return ivyPackage; }
}
