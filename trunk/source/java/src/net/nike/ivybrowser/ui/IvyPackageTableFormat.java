package net.nike.ivybrowser.ui;

import ca.odell.glazedlists.gui.TableFormat;

import com.nurflugel.ivybrowser.domain.IvyPackage;


/** Created by IntelliJ IDEA. User: douglasbullard Date: Jan 25, 2008 Time: 7:12:02 PM To change this template use File | Settings | File Templates. */
public class IvyPackageTableFormat implements TableFormat
{
    static final int ORG         = 0;
    static final int MODULE      = 1;
    static final int REVISION    = 2;
    static final int FILE        = 3;
    static final int SOURCE      = 4;
    static final int JAVADOCS    = 5;
    private String[] columnNames = { "Org", "Module", "Revision", "File", "Source?", "Javadocs?" };

    public int getColumnCount() { return 6; }

    public String getColumnName(int i)
    {

        if ((i >= 0) && (i < columnNames.length)) { return columnNames[i]; }

        throw new IllegalStateException();

    }

    public Object getColumnValue(Object o,
                                 int    i)
    {
        IvyPackage ivyPackage = (IvyPackage) o;

        switch (i) {

            case ORG:
                return ivyPackage.getOrgName();

            case MODULE:
                return ivyPackage.getModuleName();

            case REVISION:
                return ivyPackage.getVersion();

            case FILE:
                return ivyPackage.getLibrary();

            case SOURCE:
                return ivyPackage.hasSourceCode();

            case JAVADOCS:
                return ivyPackage.hasJavaDocs();

            default:
                throw new IllegalStateException();
        }
    }
}
