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

  {
    // GUI initializer generated by IntelliJ IDEA GUI Designer
    // >>> IMPORTANT!! <<<
    // DO NOT EDIT OR ADD ANY CODE HERE!
    $$$setupUI$$$();
  }

  /**
   * Method generated by IntelliJ IDEA GUI Designer >>> IMPORTANT!! <<< DO NOT edit this method OR call it in your code!
   *
   * @noinspection  ALL
   */
  private void $$$setupUI$$$()
  {
    contentPane = new JPanel();
    contentPane.setLayout(new GridBagLayout());

    final JPanel panel1 = new JPanel();

    panel1.setLayout(new BorderLayout(0, 0));

    GridBagConstraints gbc;

    gbc           = new GridBagConstraints();
    gbc.gridx     = 0;
    gbc.gridy     = 3;
    gbc.gridwidth = 2;
    gbc.fill      = GridBagConstraints.BOTH;
    contentPane.add(panel1, gbc);

    final JPanel panel2 = new JPanel();

    panel2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    panel1.add(panel2, BorderLayout.EAST);
    buttonOK = new JButton();
    buttonOK.setText("OK");
    panel2.add(buttonOK);
    buttonCancel = new JButton();
    buttonCancel.setText("Cancel");
    panel2.add(buttonCancel);

    final JPanel panel3 = new JPanel();

    panel3.setLayout(new GridBagLayout());
    gbc           = new GridBagConstraints();
    gbc.gridx     = 0;
    gbc.gridy     = 1;
    gbc.gridwidth = 2;
    gbc.weighty   = 1.0;
    gbc.fill      = GridBagConstraints.BOTH;
    contentPane.add(panel3, gbc);

    final JScrollPane scrollPane1 = new JScrollPane();

    gbc         = new GridBagConstraints();
    gbc.gridx   = 1;
    gbc.gridy   = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill    = GridBagConstraints.BOTH;
    panel3.add(scrollPane1, gbc);
    dependenciesTable = new JTable();
    scrollPane1.setViewportView(dependenciesTable);
    selectionText = new JTextField();
    gbc           = new GridBagConstraints();
    gbc.gridx     = 1;
    gbc.gridy     = 0;
    gbc.weightx   = 1.0;
    gbc.anchor    = GridBagConstraints.WEST;
    gbc.fill      = GridBagConstraints.HORIZONTAL;
    contentPane.add(selectionText, gbc);

    final JLabel label1 = new JLabel();

    label1.setText("Type here to narrow selection:");
    gbc        = new GridBagConstraints();
    gbc.gridx  = 0;
    gbc.gridy  = 0;
    gbc.anchor = GridBagConstraints.EAST;
    contentPane.add(label1, gbc);

    final JScrollPane scrollPane2 = new JScrollPane();

    scrollPane2.setVerticalScrollBarPolicy(20);
    gbc           = new GridBagConstraints();
    gbc.gridx     = 0;
    gbc.gridy     = 2;
    gbc.gridwidth = 2;
    gbc.weighty   = 1.0;
    gbc.fill      = GridBagConstraints.BOTH;
    contentPane.add(scrollPane2, gbc);
    dependenciesPanel = new JPanel();
    dependenciesPanel.setLayout(new GridBagLayout());
    scrollPane2.setViewportView(dependenciesPanel);
    dependenciesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Selected Dependencies"));
  }

  /** @noinspection  ALL */
  public JComponent $$$getRootComponent$$$()
  {
    return contentPane;
  }
}
