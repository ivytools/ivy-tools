package com.nurflugel.ivytracker;

import com.nurflugel.ivybrowser.domain.IvyKey;
import com.nurflugel.ivybrowser.domain.IvyPackage;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Nov 28, 2008 Time: 10:43:35 PM To change this template use File | Settings | File Templates.
 */
public class IvyProjectTreeModel implements TreeModel
{
  private List<IvyPackage>        projectIvyFiles;
  private Map<IvyKey, IvyPackage> ivyFilesMap;
  private final String            rootText = "Nike build projects";

  public IvyProjectTreeModel(List<IvyPackage> projectIvyFiles, Map<IvyKey, IvyPackage> ivyFilesMap)
  {
    this.projectIvyFiles = Collections.unmodifiableList(projectIvyFiles);
    this.ivyFilesMap     = Collections.unmodifiableMap(ivyFilesMap);
  }

  // ------------------------ INTERFACE METHODS ------------------------
  // --------------------- Interface TreeModel ---------------------
  @Override
  public Object getRoot()
  {
    return rootText;
  }

  @Override
  public Object getChild(Object o, int i)
  {
    if (rootText.equals(o))
    {
      return projectIvyFiles.get(i);
    }
    else if (o instanceof IvyPackage)
    {
      IvyPackage ivyFile = (IvyPackage) o;
      IvyKey     key     = ivyFile.getDependencies().get(i).getKey();

      return ivyFilesMap.get(key);
    }

    return null;
  }

  @Override
  public int getChildCount(Object o)
  {
    if (rootText.equals(o))
    {
      return projectIvyFiles.size();
    }
    else if (o instanceof IvyPackage)
    {
      IvyPackage ivyFile = (IvyPackage) o;

      return ivyFile.getDependencies().size();
    }

    return 0;
  }

  @Override
  public boolean isLeaf(Object o)
  {
    if (rootText.equals(o))
    {
      return projectIvyFiles.isEmpty();
    }
    else if (o instanceof IvyPackage)
    {
      IvyPackage ivyFile = (IvyPackage) o;

      return ivyFile.getDependencies().isEmpty();
    }

    return false;
  }

  @Override
  public void valueForPathChanged(TreePath treePath, Object o) {}

  @Override
  public int getIndexOfChild(Object o, Object o1)
  {
    if (rootText.equals(o))
    {
      return projectIvyFiles.indexOf(o1);
    }
    else if (o instanceof IvyPackage)
    {
      IvyPackage ivyFile = (IvyPackage) o;

      if (o1 instanceof IvyPackage)
      {
        IvyPackage child = (IvyPackage) o1;
        IvyKey     key   = child.getKey();

        return ivyFile.getDependencies().indexOf(key);
      }
    }

    return 0;
  }

  @Override
  public void addTreeModelListener(TreeModelListener treeModelListener) {}

  @Override
  public void removeTreeModelListener(TreeModelListener treeModelListener) {}
}
