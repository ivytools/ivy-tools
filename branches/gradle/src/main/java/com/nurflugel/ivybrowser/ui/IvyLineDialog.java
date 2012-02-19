package com.nurflugel.ivybrowser.ui;

import static com.nurflugel.common.ui.Util.centerApp;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.nurflugel.ivybrowser.AppPreferences;
import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivybrowser.handlers.BaseWebIvyRepositoryBrowserHandler;
import static javax.swing.JOptionPane.showConfirmDialog;
import static org.apache.commons.lang.StringUtils.substringBeforeLast;
import java.awt.*;
import static java.awt.Cursor.DEFAULT_CURSOR;
import static java.awt.Cursor.WAIT_CURSOR;
import static java.awt.Cursor.getPredefinedCursor;
import static java.awt.Toolkit.getDefaultToolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;

@SuppressWarnings({ "CallToPrintStackTrace" })
public class IvyLineDialog extends JDialog
{
  private static final long   serialVersionUID         = 4553280884847762492L;
  private JPanel              contentPane;
  private JButton             buttonOK;
  private JButton             buttonCancel;
  private JCheckBox           forceThisVersionCheckBox;
  private JPanel              dependenciesPanel;
  private JPanel              ivyTextPanel;
  private JPanel              publicationsPanel;
  private IvyPackage          ivyPackage;
  private String              ivyRepositoryPath;
  private IvyBrowserMainFrame mainFrame;
  private boolean             isOk;

  public IvyLineDialog(IvyPackage ivyPackage, String ivyRepositoryPath, IvyBrowserMainFrame mainFrame, AppPreferences preferences) throws IOException
  {
    this.ivyPackage        = ivyPackage;
    this.ivyRepositoryPath = ivyRepositoryPath;
    this.mainFrame         = mainFrame;
    setContentPane(contentPane);
    setModal(false);
    getRootPane().setDefaultButton(buttonOK);

    LayoutManager textboxLayout = new BoxLayout(ivyTextPanel, Y_AXIS);
    LayoutManager boxLayout     = new BoxLayout(dependenciesPanel, Y_AXIS);
    LayoutManager filesLayout   = new BoxLayout(publicationsPanel, Y_AXIS);

    ivyTextPanel.setLayout(textboxLayout);
    dependenciesPanel.setLayout(boxLayout);
    publicationsPanel.setLayout(filesLayout);
    addListeners();
    createText(preferences);
    pack();
    setSize(getSize().width, (int) (getSize().height * 1.25));
    centerApp(this);
    adjustSize();
  }

  private void adjustSize()
  {
    Dimension requestedSize = getSize();
    Dimension screenSize    = getDefaultToolkit().getScreenSize();

    if (requestedSize.height < screenSize.height)
    {
      setSize(requestedSize.width, requestedSize.height);
    }
    else
    {
      setSize(requestedSize.width, screenSize.height);
    }

    setLocation(getLocation().x, 0);
    doLayout();
    invalidate();
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
    addWindowListener(new WindowAdapter()
      {
        @Override
        public void windowClosing(WindowEvent e)
        {
          onCancel();
        }
      });

    // call onCancel() on ESCAPE
    contentPane.registerKeyboardAction(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          onCancel();
        }
      }, KeyStroke.getKeyStroke(VK_ESCAPE, 0), WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    forceThisVersionCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          updatePastedText();
        }
      });

    // call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
  }

  private void onOK()
  {
    isOk = true;
    getPasteText();
    dispose();
  }

  /** Generate the text to paste into the buffer. */
  private void getPasteText()
  {
    String sourceTag = ivyPackage.hasSourceCode() ? ",source"
                                                  : "";
    String javadocTag = ivyPackage.hasJavaDocs() ? ",javadoc"
                                                 : "";
    String forceText = forceThisVersionCheckBox.isSelected() ? " force=\"true\" "
                                                             : "";
    StringBuilder    pasteText         = new StringBuilder();
    List<IvyPackage> excludedPackages  = new ArrayList<IvyPackage>();
    List<String>     excludedFiles     = new ArrayList<String>();
    Component[]      packageComponents = dependenciesPanel.getComponents();
    Component[]      fileComponents    = publicationsPanel.getComponents();

    for (Component component : packageComponents)
    {
      addExcludedPackages(excludedPackages, component);
    }

    for (Component fileComponent : fileComponents)
    {
      addExcludedFiles(excludedFiles, fileComponent);
    }

    String text = "";

    if (excludedPackages.isEmpty() && excludedFiles.isEmpty())
    {
      generateIvyLineNoExcludes(sourceTag, javadocTag, forceText, pasteText);
    }
    else
    {
      generateIvyLineWithExcludes(sourceTag, javadocTag, forceText, pasteText, excludedPackages, excludedFiles);
    }

    // convert the text to a StringSelection
    StringSelection ivyLine = new StringSelection(pasteText.toString());

    // and paste it into the buffer
    getDefaultToolkit().getSystemClipboard().setContents(ivyLine, null);
  }

  private void generateIvyLineWithExcludes(String sourceTag, String javadocTag, String forceText, StringBuilder pasteText,
                                           List<IvyPackage> excludedPackages, List<String> excludedFiles)
  {
    String text = "<dependency org=\"" + ivyPackage.getOrgName() + "\"  name=\"" + ivyPackage.getModuleName() + "\"  rev=\"" + ivyPackage.getVersion()
                    + "\"  conf=\"dist-ear" + sourceTag + javadocTag + "\"" + forceText + ">";

    ivyTextPanel.add(new JLabel(text));
    pasteText.append(text);

    for (IvyPackage excludedPackage : excludedPackages)
    {
      text = "    <exclude org=\"" + excludedPackage.getOrgName() + "\" name=\"" + excludedPackage.getModuleName() + "\"/>";
      ivyTextPanel.add(new JLabel(text));
      pasteText.append("\n").append(text);
    }

    for (String excludedFile : excludedFiles)
    {
      text = "    <exclude  name=\"" + excludedFile + "\"/>";
      ivyTextPanel.add(new JLabel(text));
      pasteText.append("\n").append(text);
    }

    text = "</dependency>";
    ivyTextPanel.add(new JLabel(text));
    pasteText.append("\n").append(text);
  }

  private void generateIvyLineNoExcludes(String sourceTag, String javadocTag, String forceText, StringBuilder pasteText)
  {
    String text = "<dependency org=\"" + ivyPackage.getOrgName() + "\"  name=\"" + ivyPackage.getModuleName() + "\"  rev=\"" + ivyPackage.getVersion()
                    + "\"  conf=\"build,dist-war,test" + sourceTag + javadocTag + "\"" + forceText + "/>";

    ivyTextPanel.add(new JLabel(text));
    pasteText.append(text);
  }

  private void addExcludedFiles(List<String> excludedFiles, Component fileComponent)
  {
    if (fileComponent instanceof JCheckBox)
    {
      JCheckBox checkBox = (JCheckBox) fileComponent;

      if (!checkBox.isSelected())
      {
        String file = checkBox.getText();

        file = substringBeforeLast(file, ".");
        excludedFiles.add(file);
      }
    }
  }

  private void addExcludedPackages(List<IvyPackage> excludedPackages, Component component)
  {
    if (component instanceof IvyPackageCheckbox)
    {
      IvyPackageCheckbox checkbox = (IvyPackageCheckbox) component;

      if (!checkbox.isSelected())
      {
        excludedPackages.add(checkbox.getIvyPackage());
      }
    }
  }

  private void onCancel()
  {
    isOk = false;
    dispose();
  }

  @SuppressWarnings({ "StringConcatenationInsideStringBufferAppend" })
  private void updatePastedText()
  {
    ivyTextPanel.removeAll();
    getPasteText();
    ivyTextPanel.updateUI();
    dependenciesPanel.updateUI();
    publicationsPanel.updateUI();
    adjustSize();
  }

  private void createText(AppPreferences preferences) throws IOException
  {
    List<IvyPackage> dependencies1 = ivyPackage.getDependencies();
    Set<IvyPackage>  dependencies  = new HashSet<IvyPackage>();

    dependencies.addAll(dependencies1);
    dependenciesPanel.removeAll();

    if (dependencies.isEmpty())
    {
      dependenciesPanel.add(new JLabel("No dependencies found"));
    }
    else  // todo put this into a method "populate dependencies
    {
      populateDependenciesPanel(dependencies, preferences);
    }

    populateIncludedJarsPanel(preferences);
    updatePastedText();
  }

  private void populateDependenciesPanel(Set<IvyPackage> dependencies, final AppPreferences preferences)
  {
    List<IvyPackageCheckbox> sortedCheckboxes = new ArrayList<IvyPackageCheckbox>();

    for (final IvyPackage dependency : dependencies)
    {
      IvyPackageCheckbox checkBox = new IvyPackageCheckbox(dependency);

      checkBox.setToolTipText("Right-click to bring up another window of this item's dependencies");
      checkBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            updatePastedText();
          }
        });
      checkBox.addMouseListener(new MouseAdapter()
        {
          /** Popup still yet another line dialog box if they click on the label of the checkbox. */
          @Override
          public void mouseClicked(MouseEvent mouseEvent)
          {
            if (mouseEvent.isMetaDown())
            {
              setCursor(getPredefinedCursor(WAIT_CURSOR));

              // get package from look up of org module rev in hashtable
              IvyPackage newIvyPackage = getPackageFromMap(dependency, mainFrame.getPackageMap());

              try
              {
                if (newIvyPackage == null)
                {
                  mainFrame.showMissingIvyVersionMessage();
                }
                else
                {
                  IvyLineDialog lineDialog = new IvyLineDialog(newIvyPackage, ivyRepositoryPath, mainFrame, preferences);

                  lineDialog.setVisible(true);
                }

                setCursor(getPredefinedCursor(DEFAULT_CURSOR));
              }
              catch (IOException e)
              {
                e.printStackTrace();  // todo show error dialog
              }
            }
          }
        });
      sortedCheckboxes.add(checkBox);
    }

    Collections.sort(sortedCheckboxes);

    for (IvyPackageCheckbox sortedCheckbox : sortedCheckboxes)
    {
      dependenciesPanel.add(sortedCheckbox);
    }
  }

  private IvyPackage getPackageFromMap(IvyPackage dependency, Map<String, Map<String, Map<String, IvyPackage>>> packageMap)
  {
    String                               orgName    = dependency.getOrgName();
    String                               moduleName = dependency.getModuleName();
    String                               version    = dependency.getVersion();
    Map<String, Map<String, IvyPackage>> modules    = packageMap.get(orgName);
    Map<String, IvyPackage>              versions   = modules.get(moduleName);
    IvyPackage                           aPackage   = versions.get(version);

    return aPackage;
  }

  private void populateIncludedJarsPanel(final AppPreferences preferences)
  {
    Collection<String> includedFiles = ivyPackage.getPublications();
    final String       orgName       = ivyPackage.getOrgName();
    final String       moduleName    = ivyPackage.getModuleName();
    final String       version       = ivyPackage.getVersion();

    // final BaseWebHandler handler       = HandlerFactory.getIvyRepositoryHandler(mainFrame, ivyRepositoryPath, null, mainFrame.getPackageMap());
    int height = 0;

    for (String includedFile : includedFiles)
    {
      final JCheckBox fileLabel = new JCheckBox(includedFile, true);

      fileLabel.setToolTipText("Click to download this file, unselect to exclude");
      publicationsPanel.add(fileLabel);
      height += fileLabel.getPreferredSize().height;
      fileLabel.addMouseListener(new MouseAdapter()
        {
          /** download the file. */
          @Override
          public void mouseClicked(MouseEvent mouseEvent)
          {
            if (mouseEvent.isMetaDown())
            {
              try
              {
                BaseWebIvyRepositoryBrowserHandler.downloadFile(fileLabel, orgName, moduleName, version, mainFrame, ivyRepositoryPath, preferences);
              }
              catch (IOException e)
              {
                e.printStackTrace();  // todo error message
              }
            }
          }
        });
      fileLabel.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent actionEvent)
          {
            updatePastedText();
          }
        });
    }

    height += 15;

    Dimension newSize = new Dimension(publicationsPanel.getWidth(), height);

    // todo put this into a scrollpane
    publicationsPanel.setSize(newSize);
    publicationsPanel.setMinimumSize(newSize);
    publicationsPanel.setPreferredSize(newSize);
  }

  public boolean isOk()
  {
    return isOk;
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
    contentPane.setLayout(new GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1));

    final JPanel panel1 = new JPanel();

    panel1.setLayout(new GridBagLayout());
    contentPane.add(panel1,
                    new GridConstraints(3, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));

    final JPanel panel2 = new JPanel();

    panel2.setLayout(new GridBagLayout());

    GridBagConstraints gbc;

    gbc         = new GridBagConstraints();
    gbc.gridx   = 1;
    gbc.gridy   = 0;
    gbc.weighty = 1.0;
    gbc.fill    = GridBagConstraints.BOTH;
    panel1.add(panel2, gbc);
    buttonOK = new JButton();
    buttonOK.setText("OK");
    gbc         = new GridBagConstraints();
    gbc.gridx   = 0;
    gbc.gridy   = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill    = GridBagConstraints.HORIZONTAL;
    panel2.add(buttonOK, gbc);
    buttonCancel = new JButton();
    buttonCancel.setText("Cancel");
    gbc         = new GridBagConstraints();
    gbc.gridx   = 1;
    gbc.gridy   = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill    = GridBagConstraints.HORIZONTAL;
    panel2.add(buttonCancel, gbc);

    final JPanel panel3 = new JPanel();

    panel3.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
    contentPane.add(panel3,
                    new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

    final JLabel label1 = new JLabel();

    label1.setText("The following line has been pasted into your cut/copy/paste buffer:");
    panel3.add(label1,
               new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                                   GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

    final JPanel panel4 = new JPanel();

    panel4.setLayout(new GridBagLayout());
    panel4.setDoubleBuffered(false);
    panel4.setMaximumSize(new Dimension(2147483647, 48));
    panel3.add(panel4,
               new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));
    panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Options"));
    forceThisVersionCheckBox = new JCheckBox();
    forceThisVersionCheckBox.setText("Force this version");
    forceThisVersionCheckBox.setToolTipText("If checked, this version will override any others in your Ivy configuration.  Use with caution!");
    gbc         = new GridBagConstraints();
    gbc.gridx   = 0;
    gbc.gridy   = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.anchor  = GridBagConstraints.WEST;
    panel4.add(forceThisVersionCheckBox, gbc);
    ivyTextPanel = new JPanel();
    ivyTextPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    panel3.add(ivyTextPanel,
               new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

    final JScrollPane scrollPane1 = new JScrollPane();

    contentPane.add(scrollPane1,
                    new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    dependenciesPanel = new JPanel();
    dependenciesPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    dependenciesPanel.setToolTipText("If any of these are unchecked, that dependency will not be brought in.  Use with caution!");
    scrollPane1.setViewportView(dependenciesPanel);
    dependenciesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Dependencies to include"));

    final JScrollPane scrollPane2 = new JScrollPane();

    contentPane.add(scrollPane2,
                    new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    publicationsPanel = new JPanel();
    publicationsPanel.setLayout(new BorderLayout(0, 0));
    publicationsPanel.setMinimumSize(new Dimension(12, 40));
    publicationsPanel.setPreferredSize(new Dimension(12, 40));
    publicationsPanel.setToolTipText("These jars come across from Ivy into your unversioned/lib dirs");
    scrollPane2.setViewportView(publicationsPanel);
    publicationsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Included files:"));
  }

  /** @noinspection  ALL */
  public JComponent $$$getRootComponent$$$()
  {
    return contentPane;
  }
}
