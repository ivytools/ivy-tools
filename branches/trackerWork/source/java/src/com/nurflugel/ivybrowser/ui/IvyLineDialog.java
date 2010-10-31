package com.nurflugel.ivybrowser.ui;

import com.nurflugel.common.ui.Util;
import static com.nurflugel.common.ui.Util.centerApp;
import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivybrowser.handlers.BaseWebHandler;
import java.awt.*;
import static java.awt.Cursor.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import static java.awt.event.KeyEvent.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.swing.*;
import static javax.swing.BoxLayout.*;
import static javax.swing.JComponent.*;
import static org.apache.commons.lang.StringUtils.*;

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

  public IvyLineDialog(IvyPackage ivyPackage, String ivyRepositoryPath, IvyBrowserMainFrame mainFrame) throws IOException
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
    createText();
    pack();
    setSize(getSize().width, (int) (getSize().height * 1.25));
    centerApp(this);
    adjustSize();
  }

  private void adjustSize()
  {
    Dimension requestedSize = getSize();
    Dimension screenSize    = Toolkit.getDefaultToolkit().getScreenSize();

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
      if (component instanceof IvyPackageCheckbox)
      {
        IvyPackageCheckbox checkbox = (IvyPackageCheckbox) component;

        if (!checkbox.isSelected())
        {
          excludedPackages.add(checkbox.getIvyPackage());
        }
      }
    }

    for (Component fileComponent : fileComponents)
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

    String text = "";

    if (excludedPackages.isEmpty() && excludedFiles.isEmpty())
    {
      text = "<dependency org=\"" + ivyPackage.getOrgName() + "\"  name=\"" + ivyPackage.getModuleName() + "\"  rev=\"" + ivyPackage.getVersion()
             + "\"  conf=\"build,dist-war,test" + sourceTag + javadocTag + "\"" + forceText + "/>";
      ivyTextPanel.add(new JLabel(text));
      pasteText.append(text);
    }
    else
    {
      text = "<dependency org=\"" + ivyPackage.getOrgName() + "\"  name=\"" + ivyPackage.getModuleName() + "\"  rev=\"" + ivyPackage.getVersion()
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

    StringSelection ivyLine = new StringSelection(pasteText.toString());

    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ivyLine, null);
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

    // ivyTextPanel.doLayout();
    // ivyTextPanel.setSize(ivyTextPanel.getPreferredSize());
    // ivyTextPanel.invalidate();
    // ivyTextPanel.repaint();
    // pack();
    adjustSize();
    // centerApp(this);
  }

  private void createText() throws IOException
  {
    Set<IvyPackage> dependencies = (Set<IvyPackage>) ivyPackage.getDependencies();

    dependenciesPanel.removeAll();

    if (dependencies.isEmpty())
    {
      dependenciesPanel.add(new JLabel("No dependencies found"));
    }
    else  // todo put this into a method "populate dependencies
    {
      populateDependenciesPanel(dependencies);
    }

    populateIncludedJarsPanel();
    updatePastedText();
  }

  private void populateDependenciesPanel(Set<IvyPackage> dependencies)
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
                IvyLineDialog lineDialog = new IvyLineDialog(newIvyPackage, ivyRepositoryPath, mainFrame);

                lineDialog.setVisible(true);
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

  private void populateIncludedJarsPanel()
  {
    Collection<String> includedFiles = ivyPackage.getPublications();
    final String       orgName       = ivyPackage.getOrgName();
    final String       moduleName    = ivyPackage.getModuleName();
    final String       version       = ivyPackage.getVersion();

    // final BaseWebHandler handler       = HandlerFactory.getHandler(mainFrame, ivyRepositoryPath, null, mainFrame.getPackageMap());
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
                BaseWebHandler.downloadFile(fileLabel, orgName, moduleName, version, mainFrame, ivyRepositoryPath);
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
}