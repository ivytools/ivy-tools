package com.nike.ivytracker;

import com.nike.ivytracker.domain.IvyFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;


/** Created by IntelliJ IDEA. User: douglasbullard Date: Nov 28, 2008 Time: 10:43:35 PM To change this template use File | Settings | File Templates. */
public class FindUsingProjectsTreeModel implements TreeModel
{
    private Map<String, IvyFile> ivyFilesMap;
    private IvyFile              ivyFile;

    public FindUsingProjectsTreeModel(IvyFile ivyFile, Map<String, IvyFile> ivyFilesMap)
    {
        this.ivyFile     = ivyFile;
        this.ivyFilesMap = Collections.unmodifiableMap(ivyFilesMap);
    }

    public Object getRoot() { return ivyFile; }

    public Object getChild(Object o, int i)
    {

        if (o instanceof IvyFile) {
            List<IvyFile> usingIvyFiles = findAllIvyFilesUsingThisAsDependency((IvyFile) o);

            return usingIvyFiles.get(i);
        }

        return null;
    }

    private List<IvyFile> findAllIvyFilesUsingThisAsDependency(IvyFile childIvyFile)
    {
        String        key      = childIvyFile.getKey();
        List<IvyFile> ivyFiles = new ArrayList<IvyFile>();

        for (IvyFile file : ivyFilesMap.values()) {
            List<String> dependencies = file.getDependencies();

            for (String dependency : dependencies) {

                if (dependency.equals(key)) {
                    ivyFiles.add(file);

                    break;
                }
            }
        }

        return ivyFiles;
    }

    public int getChildCount(Object o)
    {

        if (o instanceof IvyFile) { return findAllIvyFilesUsingThisAsDependency((IvyFile) o).size(); }

        return 0;
    }

    public boolean isLeaf(Object o)
    {

        if (o instanceof IvyFile) { return findAllIvyFilesUsingThisAsDependency((IvyFile) o).isEmpty(); }

        return false;
    }

    public void valueForPathChanged(TreePath treePath, Object o) { }

    public int getIndexOfChild(Object o, Object o1)
    {

        if (o instanceof IvyFile) {
            List<IvyFile> files = findAllIvyFilesUsingThisAsDependency((IvyFile) o);
            int           i     = 0;

            for (IvyFile file : files) {

                if (o1 instanceof IvyFile) {
                    IvyFile childFile = (IvyFile) o1;

                    if (file.getKey().equals(childFile.getKey())) { return i; }
                }

                i++;
            }

        }

        return 0;
    }

    public void addTreeModelListener(TreeModelListener treeModelListener) { }

    public void removeTreeModelListener(TreeModelListener treeModelListener) { }
}
