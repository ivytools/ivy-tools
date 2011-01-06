package com.nurflugel.ivytracker;

import com.nurflugel.ivybrowser.domain.IvyKey;
import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivytracker.domain.Project;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.*;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Nov 28, 2008 Time: 10:43:35 PM To change this template use File | Settings | File Templates.
 */
public class FindUsingProjectsTreeModel implements TreeModel
{
  private Map<Project, List<IvyPackage>> ivyFilesMap;
  private IvyPackage                     ivyFile;
  private final List<IvyPackage>         ivyRepositoryList;

  public FindUsingProjectsTreeModel(IvyPackage ivyFile, Map<Project, List<IvyPackage>> ivyFilesMap, List<IvyPackage> ivyRepositoryList)
  {
    this.ivyFile           = ivyFile;
    this.ivyRepositoryList = ivyRepositoryList;
    this.ivyFilesMap       = Collections.unmodifiableMap(ivyFilesMap);
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
    // if (o instanceof Project)
    // {
    // there is only one child, the Ivy file for the project itself
    // IvyPackage ivyPackage = ivyFilesMap.get(o);
    //
    // return ivyPackage;
    // }
    if (o instanceof IvyPackage)
    {
      List<Object> usingIvyFiles = findAllUsagesOfThisAsDependency((IvyPackage) o);

      return usingIvyFiles.get(i);
    }

    return null;
  }

  @Override
  public int getChildCount(Object o)
  {
    if (o instanceof Project)
    {
      // there is only one child, the Ivy file for the project itself
      return 0;
    }

    if (o instanceof IvyPackage)
    {
      return findAllUsagesOfThisAsDependency((IvyPackage) o).size();
    }

    return 0;
  }

  @Override
  public boolean isLeaf(Object o)
  {
    if (o instanceof IvyPackage)  // todo or Project
    {
      return findAllUsagesOfThisAsDependency((IvyPackage) o).isEmpty();
    }

    return false;
  }

  @Override
  public void valueForPathChanged(TreePath treePath, Object o) {}

  @Override
  public int getIndexOfChild(Object o, Object o1)
  {
    if ((o instanceof IvyPackage) || (o instanceof Project))
    {
      List<Object> files = findAllUsagesOfThisAsDependency((IvyPackage) o);
      int          i     = 0;

      for (Object file : files)
      {
        if (o1 instanceof IvyPackage)
        {
          IvyPackage childFile = (IvyPackage) o1;

          if (file.equals(childFile))
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
  private List<Object> findAllUsagesOfThisAsDependency(IvyPackage childIvyFile)  // todo also somehow return Project
  {
    IvyKey       key    = childIvyFile.getKey();
    List<Object> usages = new ArrayList<Object>();

    // for (Project project : ivyFilesMap.keySet())
    // {
    // IvyPackage ivyPackage = ivyFilesMap.get(project);
    //
    // if (ivyPackage.getKey().equals(key))
    // {
    // usages.add(project);
    // }
    // }
    for (IvyPackage file : ivyRepositoryList)
    {
      Collection<IvyPackage> dependencies = file.getDependencies();

      for (IvyPackage dependency : dependencies)
      {
        if (dependency.getKey().equals(key))
        {
          usages.add(file);

          break;
        }
      }
    }

    return usages;
  }
}
