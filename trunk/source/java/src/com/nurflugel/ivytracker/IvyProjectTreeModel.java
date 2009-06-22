package com.nurflugel.ivytracker;

import com.nurflugel.ivytracker.domain.IvyFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/** Created by IntelliJ IDEA. User: douglasbullard Date: Nov 28, 2008 Time: 10:43:35 PM To change this template use File | Settings | File Templates. */
public class IvyProjectTreeModel implements TreeModel
{
    private List<IvyFile>        projectIvyFiles;
    private Map<String, IvyFile> ivyFilesMap;
    private final String         rootText = "Nike build projects";

    public IvyProjectTreeModel(List<IvyFile> projectIvyFiles, Map<String, IvyFile> ivyFilesMap)
    {
        this.projectIvyFiles = Collections.unmodifiableList(projectIvyFiles);
        this.ivyFilesMap     = Collections.unmodifiableMap(ivyFilesMap);
    }

    public Object getRoot()
    {
        return rootText;
    }

    public Object getChild(Object o, int i)
    {
        if (rootText.equals(o))
        {
            return projectIvyFiles.get(i);
        }
        else if (o instanceof IvyFile)
        {
            IvyFile ivyFile = (IvyFile) o;
            String  key     = ivyFile.getDependencies().get(i);

            return ivyFilesMap.get(key);
        }

        return null;
    }

    public int getChildCount(Object o)
    {
        if (rootText.equals(o))
        {
            return projectIvyFiles.size();
        }
        else if (o instanceof IvyFile)
        {
            IvyFile ivyFile = (IvyFile) o;

            return ivyFile.getDependencies().size();
        }

        return 0;
    }

    public boolean isLeaf(Object o)
    {
        if (rootText.equals(o))
        {
            return projectIvyFiles.isEmpty();
        }
        else if (o instanceof IvyFile)
        {
            IvyFile ivyFile = (IvyFile) o;

            return ivyFile.getDependencies().isEmpty();
        }

        return false;
    }

    public void valueForPathChanged(TreePath treePath, Object o)
    {
    }

    public int getIndexOfChild(Object o, Object o1)
    {
        if (rootText.equals(o))
        {
            return projectIvyFiles.indexOf(o1);
        }
        else if (o instanceof IvyFile)
        {
            IvyFile ivyFile = (IvyFile) o;

            if (o1 instanceof IvyFile)
            {
                IvyFile child = (IvyFile) o1;
                String  key   = child.getKey();

                return ivyFile.getDependencies().indexOf(key);
            }
        }

        return 0;
    }

    public void addTreeModelListener(TreeModelListener treeModelListener)
    {
    }

    public void removeTreeModelListener(TreeModelListener treeModelListener)
    {
    }
}
