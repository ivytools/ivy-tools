package com.nurflugel.ivybrowser.ui;

import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivybrowser.handlers.BaseWebHandler;
import static com.nurflugel.ivybrowser.ui.BuilderMainFrame.centerApp;
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

public class IvyLineDialog extends JDialog
{
  /** Use serialVersionUID for interoperability. */
  private static final long   serialVersionUID         = 4553280884847762492L;
  private JPanel              contentPane;
  private JButton             buttonOK;
  private JButton             buttonCancel;
  private JCheckBox           forceThisVersionCheckBox;
  private JPanel              dependenciesPanel;
  private JPanel              ivyTextPanel;
  private JPanel              includedFilesPanel;
  private IvyPackage          ivyPackage;
  private String              ivyRepositoryPath;
  private IvyBrowserMainFrame mainFrame;

  public IvyLineDialog(IvyPackage ivyPackage, String ivyRepositoryPath, IvyBrowserMainFrame mainFrame) throws IOException
  {
    this.ivyPackage        = ivyPackage;
    this.ivyRepositoryPath = ivyRepositoryPath;
    this.mainFrame         = mainFrame;
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);

    LayoutManager textboxLayout = new BoxLayout(ivyTextPanel, Y_AXIS);
    LayoutManager boxLayout     = new BoxLayout(dependenciesPanel, Y_AXIS);
    LayoutManager filesLayout   = new BoxLayout(includedFilesPanel, Y_AXIS);

    ivyTextPanel.setLayout(textboxLayout);
    dependenciesPanel.setLayout(boxLayout);
    includedFilesPanel.setLayout(filesLayout);
    addListeners();
    createText();
    pack();

    Dimension requestedSize = getSize();
    Dimension screenSize    = Toolkit.getDefaultToolkit().getScreenSize();

    if (screenSize.height < requestedSize.height)
    {
      setSize(requestedSize.width, screenSize.height);
    }

    centerApp(this);
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
    getPasteText();
    dispose();
  }

  private void onCancel()
  {
    dispose();
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
          });
        sortedCheckboxes.add(checkBox);
      }

      Collections.sort(sortedCheckboxes);

      for (IvyPackageCheckbox sortedCheckbox : sortedCheckboxes)
      {
        dependenciesPanel.add(sortedCheckbox);
      }
    }

    populateIncludedJarsPanel();
    updatePastedText();
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

  private void populateIncludedJarsPanel() throws IOException
  {
    Collection<String>   includedFiles = ivyPackage.getPublications();
    final String         orgName       = ivyPackage.getOrgName();
    final String         moduleName    = ivyPackage.getModuleName();
    final String         version       = ivyPackage.getVersion();
    final BaseWebHandler handler       = HandlerFactory.getHandler(mainFrame, ivyRepositoryPath, null, mainFrame.getPackageMap());
    int                  height        = 0;

    for (String includedFile : includedFiles)
    {
      final JLabel fileLabel = new JLabel(includedFile);

      fileLabel.setToolTipText("Click to download this file");
      includedFilesPanel.add(fileLabel);
      height += fileLabel.getPreferredSize().height;
      fileLabel.addMouseListener(new MouseAdapter()
        {
          /** download the file. */
          @Override
          public void mouseClicked(MouseEvent mouseEvent)
          {
            try
            {
              handler.downloadFile(fileLabel, orgName, moduleName, version);
            }
            catch (IOException e)
            {
              e.printStackTrace();  // todo error message
            }
          }
        });
    }

    height += 15;

    Dimension newSize = new Dimension(includedFilesPanel.getWidth(), height);

    includedFilesPanel.setSize(newSize);
    includedFilesPanel.setMinimumSize(newSize);
    includedFilesPanel.setPreferredSize(newSize);
  }

  @SuppressWarnings({ "StringConcatenationInsideStringBufferAppend" })
  private void updatePastedText()
  {
    ivyTextPanel.removeAll();
    getPasteText();
    pack();
    centerApp(this);
  }

  private void getPasteText()
  {
    String sourceTag = ivyPackage.hasSourceCode() ? ",source"
                                                  : "";
    String javadocTag = ivyPackage.hasJavaDocs() ? ",javadoc"
                                                 : "";
    String forceText = forceThisVersionCheckBox.isSelected() ? " force=\"true\" "
                                                             : "";
    StringBuilder    pasteText        = new StringBuilder();
    List<IvyPackage> excludedPackages = new ArrayList<IvyPackage>();
    Component[]      components       = dependenciesPanel.getComponents();

    for (Component component : components)
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

    String text = "";

    if (excludedPackages.isEmpty())
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

      text = "</dependency>";
      ivyTextPanel.add(new JLabel(text));
      pasteText.append("\n").append(text);
    }

    StringSelection ivyLine = new StringSelection(pasteText.toString());

    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ivyLine, null);
  }

  // --------------------------- main() method ---------------------------
}
