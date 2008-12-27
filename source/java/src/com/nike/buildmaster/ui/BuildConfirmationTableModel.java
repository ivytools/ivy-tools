package com.nike.buildmaster.ui;

import com.nike.buildmaster.ui.buildtree.BuildableItem;
import com.nike.externalsreporter.ui.tree.TargetNode;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/** Table model for the confirmation table. */
public class BuildConfirmationTableModel implements TableModel
{
    private static final int BUILD = 0;
    private static final int PROJECT = 1;
    private static final int BRANCH = 2;
    private static final int TAG_NAME = 3;
    private static final int TARGET = 4;
    private static final String[] columnNames = new String[]{"Build this?", "Project", "Branch", "Suggested Tag Name", "Target"};
    private List<TargetNode> targets;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");

    // --------------------------- CONSTRUCTORS ---------------------------

    public BuildConfirmationTableModel(List<TargetNode> checkedTargets)
    {
        targets = checkedTargets;

        for (TargetNode checkedTarget : checkedTargets)
        {
            if (checkedTarget.getBuildableItem().getProjectAbr() == null)
            {
                throw new NullPointerException("Project Abr. not found!");
            }
            //String suggestedTagName = checkedTarget.getBuildableItem().getBranch() + "_" + dateFormatter.format(new Date()) + ".00";
            String suggestedTagName = dateFormatter.format(new Date()) + "_" + checkedTarget.getBuildableItem().getProjectAbr() + "_" + checkedTarget.getBuildableItem().getBranch() + ".00";
            checkedTarget.getBuildableItem().getBranch().setTagName(suggestedTagName);
        }
    }

    // ------------------------ INTERFACE METHODS ------------------------

    // --------------------- Interface TableModel ---------------------

    public int getRowCount()
    {
        return targets.size();
    }

    public int getColumnCount()
    {
        return columnNames.length;
    }

    public String getColumnName(int columnIndex)
    {
        return columnNames[columnIndex];
    }

    public Class<?> getColumnClass(int columnIndex)
    {

        switch (columnIndex)
        {

            case BUILD:
                return Boolean.class;

            default:
                return String.class;
        }
    }

    public boolean isCellEditable(int rowIndex,
                                  int columnIndex)
    {

        switch (columnIndex)
        {

            case BUILD:
            case TAG_NAME:
                return true;

            default:
                return false;
        }
    }

    public Object getValueAt(int rowIndex,
                             int columnIndex)
    {
        TargetNode item = targets.get(rowIndex);

        BuildableItem buildItem = item.getBuildableItem();
        switch (columnIndex)
        {

            case BUILD:
                return item.isSelected();

            case PROJECT:
                return buildItem.getProject().getProjectName();

            case BRANCH:
                return buildItem.getBranch();

            case TARGET:
                return buildItem.getBuildTarget();

            case TAG_NAME:
            default:
                return buildItem.getBranch().getTagName();
        }
    }

    public void setValueAt(Object aValue,
                           int rowIndex,
                           int columnIndex)
    {
        TargetNode item = targets.get(rowIndex);

        switch (columnIndex)
        {

            case BUILD:
                item.setSelected((Boolean) aValue);

                break;

            case TAG_NAME:
            default:
                item.getBuildableItem().getBranch().setTagName((String) aValue);
        }
    }

    public void addTableModelListener(TableModelListener l)
    {
    }

    public void removeTableModelListener(TableModelListener l)
    {
    }

    // -------------------------- OTHER METHODS --------------------------

    public List<TargetNode> getConfirmedItems()
    {
        List<TargetNode> confirmedTargets = new ArrayList<TargetNode>();

        for (TargetNode target : targets)
        {

            if (target.isSelected())
            {
                confirmedTargets.add(target);
            }
        }

        return confirmedTargets;
    }
}
