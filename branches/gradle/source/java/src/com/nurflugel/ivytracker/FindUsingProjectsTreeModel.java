package com.nurflugel.ivytracker;

import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivytracker.domain.IvyFile;
import com.nurflugel.ivytracker.domain.Project;
import java.util.*;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Nov 28, 2008 Time: 10:43:35 PM To change this template use File | Settings | File Templates.
 */
public class FindUsingProjectsTreeModel implements TreeModel
{
  private Map<Project, IvyPackage> ivyFilesMap;
  private IvyPackage               ivyFile;

  public FindUsingProjectsTreeModel(IvyPackage ivyFile, Map<Project, IvyPackage> ivyFilesMap)
  {
    this.ivyFile     = ivyFile;
    this.ivyFilesMap = Collections.unmodifiableMap(ivyFilesMap);
  }

  // ------------------------ INTERFACE METHODS ------------------------

  // --------------------- Interface TreeModel ---------------------

  @Override
  public Object getRoot()
  {
    return ivyFile;
  }

  @Override
  public Object getChild(Object o, int i)
  {
    if (o instanceof IvyPackage)
    {
      List<IvyPackage> usingIvyFiles = findAllIvyFilesUsingThisAsDependency((IvyPackage) o);

      return usingIvyFiles.get(i);
    }

    return null;
  }

  @Override
  public int getChildCount(Object o)
  {
    if (o instanceof IvyPackage)
    {
      return findAllIvyFilesUsingThisAsDependency((IvyPackage) o).size();
    }

    return 0;
  }

  @Override
  public boolean isLeaf(Object o)
  {
    if (o instanceof IvyPackage)
    {
      return findAllIvyFilesUsingThisAsDependency((IvyPackage) o).isEmpty();
    }

    return false;
  }

  @Override
  public void valueForPathChanged(TreePath treePath, Object o) {}

  @Override
  public int getIndexOfChild(Object o, Object o1)
  {
    if (o instanceof IvyFile)
    {
      List<IvyPackage> files = findAllIvyFilesUsingThisAsDependency((IvyPackage) o);
      int              i     = 0;

      for (IvyPackage file : files)
      {
        if (o1 instanceof IvyFile)
        {
          IvyFile childFile = (IvyFile) o1;

          if (file.getKey().equals(childFile.getKey()))
          {
            return i;
          }
        }

        i++;
      }
    }

    return 0;
  }

  @Override
  public void addTreeModelListener(TreeModelListener treeModelListener) {}

  @Override
  public void removeTreeModelListener(TreeModelListener treeModelListener) {}

  // -------------------------- OTHER METHODS --------------------------

  private List<IvyPackage> findAllIvyFilesUsingThisAsDependency(IvyPackage childIvyFile)
  {
    String           key      = childIvyFile.getKey();
    List<IvyPackage> ivyFiles = new ArrayList<IvyPackage>();

    for (IvyPackage file : ivyFilesMap.values())
    {
      Collection<IvyPackage> dependencies = file.getDependencies();

      for (IvyPackage dependency : dependencies)
      {
        if (dependency.getKey().equals(key))
        {
          ivyFiles.add(file);

          break;
        }
      }
    }

    return ivyFiles;
  }
}
