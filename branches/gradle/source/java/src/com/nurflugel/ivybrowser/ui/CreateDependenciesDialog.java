package com.nurflugel.ivybrowser.ui;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import com.nurflugel.ivybrowser.domain.IvyRepositoryItem;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import static com.nurflugel.common.ui.Util.centerApp;

@SuppressWarnings({ "RefusedBequest" })
public class CreateDependenciesDialog extends JDialog
{
  /** Use serialVersionUID for interoperability. */
  private static final long            serialVersionUID  = -6948838156489928491L;
  private JPanel                       contentPane;
  private JButton                      buttonOK;
  private JButton                      buttonCancel;
  private JTable                       dependenciesTable;
  private JTextField                   selectionText;
  private JPanel                       dependenciesPanel;
  private EventList<IvyRepositoryItem> ivyPackages;

  // --------------------------- CONSTRUCTORS ---------------------------
  public CreateDependenciesDialog(List<IvyRepositoryItem> packages)
  {
    ivyPackages = new BasicEventList<IvyRepositoryItem>();
    ivyPackages.addAll(packages);

    LayoutManager boxLayout = new BoxLayout(dependenciesPanel, BoxLayout.Y_AXIS);

    dependenciesPanel.setLayout(boxLayout);
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);
    addListeners();
    pack();
    setTitle("Select your dependencies from the list...");
    centerApp(this);
    filterTable();
    setVisible(true);
  }

  private void addListeners()
  {
    buttonOK.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          onOK();
        }
      });
    buttonCancel.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          onCancel();
        }
      });
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter()
      {
        @Override
        public void windowClosing(WindowEvent e)
        {
          onCancel();
        }
      });
    contentPane.registerKeyboardAction(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          onCancel();
        }
      }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    selectionText.addKeyListener(new KeyAdapter()
      {
        @Override
        public void keyReleased(KeyEvent e)
        {
          filterTable();
        }
      });
    dependenciesTable.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mouseClicked(MouseEvent e)
        {
          selectItem();
        }
      });

    // todo - add cell renderer to change colors of rows as cursor passes over them.
    // dependenciesTable.addMouseMotionListener(new MouseMotionAdapter() {
    // public void mouseMoved(MouseEvent e)
    // {
    // JTable aTable = (JTable) e.getSource();
    // int    row    = aTable.rowAtPoint(e.getPoint());
    // System.out.println("row = " + row);
    // }
    // });
  }

  private void onOK()
  {
    dispose();
  }

  private void onCancel()
  {
    dependenciesPanel.removeAll();
    dispose();
  }

  private void selectItem()
  {
    dependenciesPanel.removeAll();

    // get the items from the table
    EventTableModel tableModel   = (EventTableModel) dependenciesTable.getModel();
    int[]           selectedRows = dependenciesTable.getSelectedRows();

    for (int selectedRow : selectedRows)
    {
      Object                    o        = tableModel.getElementAt(selectedRow);
      IvyRepositoryItem         item     = (IvyRepositoryItem) o;
      IvyRepositoryItemCheckbox checkbox = new IvyRepositoryItemCheckbox(item);

      dependenciesPanel.add(checkbox);
    }

    // dependenciesPanel.validate();
    dependenciesPanel.revalidate();
  }

  @SuppressWarnings({ "unchecked" })
  private void filterTable()
  {
    SortedList<IvyRepositoryItem> sortedDependencies = new SortedList<IvyRepositoryItem>(ivyPackages, new IvyRepositoryItemComparator());
    FilterList<IvyRepositoryItem> filteredPackages   = new FilterList<IvyRepositoryItem>(sortedDependencies,
                                                                                         new TextComponentMatcherEditor(selectionText,
                                                                                                                        new IvyRepositoryItemFilterator()));
    EventTableModel<IvyRepositoryItem> tableModel = new EventTableModel<IvyRepositoryItem>(filteredPackages, new IvyRepositoryItemTableFormat());

    dependenciesTable.setModel(tableModel);

    TableComparatorChooser<IvyRepositoryItem> tableSorter = new TableComparatorChooser<IvyRepositoryItem>(dependenciesTable, sortedDependencies,
                                                                                                          true);

    dependenciesTable.setShowHorizontalLines(true);
    dependenciesTable.setShowVerticalLines(true);
    dependenciesTable.invalidate();
  }

  // -------------------------- OTHER METHODS --------------------------
  public List<IvyRepositoryItem> getDependancies()
  {
    Object[]                selectedObjects = dependenciesPanel.getComponents();
    List<IvyRepositoryItem> dependencies    = new ArrayList<IvyRepositoryItem>();

    for (Object selectedObject : selectedObjects)
    {
      if (selectedObject instanceof IvyRepositoryItemCheckbox)
      {
        IvyRepositoryItemCheckbox checkbox = (IvyRepositoryItemCheckbox) selectedObject;

        if (checkbox.isSelected())
        {
          dependencies.add(checkbox.getItem());
        }
      }
    }

    return dependencies;
  }

  // --------------------------- main() method ---------------------------
  public static void main(String[] args)
  {
    CreateDependenciesDialog dialog = new CreateDependenciesDialog(new ArrayList<IvyRepositoryItem>());

    dialog.pack();
    dialog.setVisible(true);
    System.exit(0);
  }
}
