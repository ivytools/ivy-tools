package com.nurflugel.versionfinder;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.EventTableModel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.commons.lang.StringUtils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import static com.nurflugel.common.ui.Util.*;
import static com.nurflugel.common.ui.Version.VERSION;
import static com.nurflugel.versionfinder.Jdk.JDK15;
import static com.nurflugel.versionfinder.Jdk.findByVersion;
import static com.nurflugel.versionfinder.PathLength.*;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.JFileChooser.*;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

/** Find class versions in libraries. */
@SuppressWarnings({ "UseOfSystemOutOrSystemErr", "CallToPrintStackTrace" })
public class VersionFinderUi extends JFrame
{
  private static final String FILES_DIR                 = "filesDir";
  private static final String JDK_THRESHOLD             = "jdkThreshold";
  private static final String SLASH                     = File.separator;
  private JButton             selectDirsFilesButton;
  private JButton             quitButton;
  private JButton             helpButton;
  private JTable              resultsTable;
  private JPanel              contentsPanel;
  private JPanel              jdkButtonPanel;
  private JRadioButton        fullPathsRadioButton;
  private JRadioButton        fileNameRadioButton;
  private JRadioButton        shortFilePathsRadioButton;
  private ButtonGroup         jdkButtonGroup;
  private Preferences         preferences;
  private File                fileDir;
  private File                tempDir                   = new File(System.getProperty("user.home"), "tempDir");
  private Jdk                 jdkThreshold              = JDK15;
  private List<ResultRow>     results                   = new ArrayList<ResultRow>();
  private String              commonText;

  public VersionFinderUi()
  {
    // $$$setupUI$$$();
    $$$setupUI$$$();

    String javaVersion = System.getProperty("java.version");

    System.out.println("VersionFinderUi.VersionFinderUi::java home version = " + javaVersion);

    Properties properties = System.getProperties();

    for (Object o : properties.keySet())
    {
      System.out.println("VersionFinderUi.VersionFinderUi::properties.getProperty(" + o + ") = " + properties.getProperty((String) o));
    }

    if (!StringUtils.startsWith(javaVersion, "1.6"))
    {
      showMessageDialog(this, "Cannot continue - Java Home must be defined pointing to JDK 1.6 or greater", "Missing JDK 1.6 in Java_Home",
                        ERROR_MESSAGE);
      System.exit(0);
    }
  }

  // -------------------------- OTHER METHODS --------------------------
  private void createUIComponents()
  {
    resultsTable = new JTable();
    resultsTable.setDefaultRenderer(Object.class, new VersionTableCellRenderer(this));
    jdkButtonPanel = new JPanel();
    jdkButtonGroup = new ButtonGroup();

    BoxLayout boxLayout = new BoxLayout(jdkButtonPanel, Y_AXIS);

    jdkButtonPanel.setLayout(boxLayout);

    Jdk[] jdks = Jdk.values();

    for (final Jdk jdk : jdks)
    {
      JdkCheckBox checkBox = new JdkCheckBox(jdk);

      jdkButtonGroup.add(checkBox);
      jdkButtonPanel.add(checkBox);
      checkBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent actionEvent)
          {
            jdkThreshold = jdk;
            refreshTableDisplay();
          }
        });
    }
  }

  private void refreshTableDisplay()
  {
    resultsTable.invalidate();
    resultsTable.repaint();
  }

  public PathLength getPathLength()
  {
    if (fileNameRadioButton.isSelected())
    {
      return FILE_NAME;
    }
    else if (fullPathsRadioButton.isSelected())
    {
      return FULL;
    }

    return SHORT;
  }

  // --------------------------- main() method ---------------------------
  public static void main(String[] args)
  {
    VersionFinderUi ui = new VersionFinderUi();

    ui.loadUi();
  }

  private void loadUi()
  {
    setAppTitle();
    setContentPane(contentsPanel);
    addListeners();
    setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", this);
    preferences = Preferences.userNodeForPackage(VersionFinderUi.class);
    loadPreferences();
    rmDirs(tempDir);
    pack();
    centerApp(this);
    setVisible(true);
  }

  private void setAppTitle()
  {
    setTitle("Version Finder v. " + VERSION);
  }

  private void addListeners()
  {
    selectDirsFilesButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent actionEvent)
        {
          setCursor(Cursor.WAIT_CURSOR);
          processDirs();
          filterNames();
          setCursor(Cursor.DEFAULT_CURSOR);
        }
      });
    quitButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent actionEvent)
        {
          doQuitAction();
        }
      });
    addWindowListener(new WindowAdapter()
      {
        @Override
        public void windowClosing(WindowEvent e)
        {
          super.windowClosing(e);
          doQuitAction();
        }
      });
    fileNameRadioButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent actionEvent)
        {
          refreshTableDisplay();
        }
      });
    fullPathsRadioButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent actionEvent)
        {
          refreshTableDisplay();
        }
      });
    shortFilePathsRadioButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent actionEvent)
        {
          refreshTableDisplay();
        }
      });
    addHelpListener("versionFinderHelp.hs", helpButton, this);
  }

  private void processDirs()
  {
    JFileChooser fileChooser = new JFileChooser();

    fileChooser.setMultiSelectionEnabled(true);
    fileChooser.setDialogTitle("Pick dirs or jars");
    fileChooser.setDialogType(OPEN_DIALOG);
    fileChooser.setDragEnabled(true);
    fileChooser.setMultiSelectionEnabled(true);
    fileChooser.setFileSelectionMode(FILES_AND_DIRECTORIES);

    if (fileDir != null)
    {
      fileChooser.setCurrentDirectory(fileDir);
    }

    int choice = fileChooser.showDialog(this, "Select these files or dirs");

    if (choice == APPROVE_OPTION)
    {
      processUsersFiles(fileChooser);
    }
  }

  private void processUsersFiles(JFileChooser fileChooser)
  {
    results.clear();

    File[]                     selectedFiles = fileChooser.getSelectedFiles();
    Map<File, Set<MajorMinor>> jarResults    = new HashMap<File, Set<MajorMinor>>();  // map of string(jar path, version list)

    for (File file : selectedFiles)
    {
      preferences.put(FILES_DIR, file.getParent());
      fileDir = file.getParentFile();
      processArg(file, jarResults);
    }

    Set<File>  files    = jarResults.keySet();
    List<File> fileList = new ArrayList<File>(files);

    Collections.sort(fileList);

    for (File file : fileList)
    {
      processFile(jarResults, file);
    }

    populateTable();
    setAppTitle();
    rmDirs(tempDir);
    tempDir.deleteOnExit();
  }

  private void processArg(File file, Map<File, Set<MajorMinor>> jarResults)
  {
    String name = file.getName();

    if (name.endsWith(".jar") || name.endsWith(".zip"))
    {
      processJarFile(file, jarResults);
    }
    else if (file.isDirectory())
    {
      processDir(file, jarResults, file, false);
    }
  }

  // todo thread task
  private void processJarFile(File jarFile, Map<File, Set<MajorMinor>> jarResults)
  {
    if (jarFile.getName().endsWith(".jar"))
    {
      setTitle("Processing " + jarFile.getName());
      rmDirs(tempDir);
      tempDir.mkdirs();

      File classFile = unzipFile(jarFile);

      if (classFile != null)
      {
        processClassFile(classFile, jarResults, jarFile);
      }

      rmDirs(tempDir);
    }
  }

  @SuppressWarnings({ "ResultOfMethodCallIgnored", "IOResourceOpenedButNotSafelyClosed" })
  private File unzipFile(File file)
  {
    try
    {
      ZipFile     zipFile = new ZipFile(file);
      Enumeration entries = zipFile.entries();

      while (entries.hasMoreElements())
      {
        ZipEntry entry   = (ZipEntry) entries.nextElement();
        String   name    = entry.getName();
        File     newFile = new File(tempDir, name);

        if (name.endsWith(".class"))
        {
          File dir = newFile.getParentFile();

          dir.mkdirs();

          FileOutputStream     fileOutputStream = new FileOutputStream(newFile);
          BufferedOutputStream out              = new BufferedOutputStream(fileOutputStream);
          InputStream          inputStream      = zipFile.getInputStream(entry);

          copyInputStream(inputStream, out);
          fileOutputStream.close();

          return newFile;
        }
      }

      zipFile.close();
    }
    catch (IOException ioe)
    {
      System.err.println("Unhandled (but not fatal) exception:");
      ioe.printStackTrace();
    }

    return null;
  }

  public void copyInputStream(InputStream in, OutputStream out) throws IOException
  {
    byte[] buffer = new byte[1024];
    int    len;

    while ((len = in.read(buffer)) >= 0)
    {
      out.write(buffer, 0, len);
    }

    in.close();
    out.close();
  }

  private void processClassFile(File file, Map<File, Set<MajorMinor>> jarResults, File jarFile)
  {
    try
    {
      String className = getClassName(file);

      className = StringUtils.substringBefore(className, ".class");

      Runtime runtime = Runtime.getRuntime();

      // now this works - go figure...
      String[] command = { "javap", "-verbose", "-classpath", tempDir.getAbsolutePath(), className };
      Process  process = runtime.exec(command);

      printOutput(file, process, jarFile, jarResults);
    }
    catch (IOException e)
    {  // todo show a dialog
      e.printStackTrace();
    }
  }

  private String getClassName(File file)
  {
    String path          = file.getAbsolutePath();
    String leadingPath   = tempDir.getAbsolutePath();
    String classPathName = StringUtils.substringAfter(path, leadingPath);
    String className     = StringUtils.replace(classPathName, File.separator, ".");

    className = StringUtils.substringAfter(className, ".");

    return className;
  }

  private void printOutput(File file, Process process, File jarFile, Map<File, Set<MajorMinor>> jarResults) throws IOException
  {
    InputStream       inputStream  = process.getInputStream();
    InputStreamReader streamReader = new InputStreamReader(inputStream);
    BufferedReader    reader       = new BufferedReader(streamReader);
    String            line         = reader.readLine();

    // int i = 0;
    String major = "";
    String minor = "";

    while (line != null)
    {
      // System.out.println("line[" + i++ + "] = " + line);
      if (StringUtils.contains(line, "major version:"))
      {
        major = StringUtils.substringAfter(line, "major version:").trim();
      }
      else if (StringUtils.contains(line, "minor version:"))
      {
        minor = StringUtils.substringAfter(line, "minor version:").trim();
      }

      if ((major.length() > 0) && (minor.length() > 0))
      {
        Set<MajorMinor> versions = jarResults.get(jarFile);

        if (versions == null)
        {
          versions = new HashSet<MajorMinor>();
          jarResults.put(jarFile, versions);
        }

        versions.add(new MajorMinor(major, minor));
        process.destroy();

        break;
      }

      line = reader.readLine();
    }
  }

  private boolean processDir(File dir, Map<File, Set<MajorMinor>> jarResults, File jarFile, boolean processed)
  {
    File[]  files        = dir.listFiles();
    boolean wasProcessed = processed;

    for (File file : files)
    {
      if (file.isDirectory())
      {
        wasProcessed = processDir(file, jarResults, jarFile, wasProcessed);
      }

      if (file.getName().endsWith(".jar"))
      {
        processJarFile(file, jarResults);
      }
      // else if (file.getName().endsWith(".class"))
      // {
      // if (!wasProcessed)
      // {
      // processClassFile(file, jarResults, jarFile);
      // wasProcessed = true;
      // }
      // }
    }

    return wasProcessed;
  }

  private void processFile(Map<File, Set<MajorMinor>> jarResults, File file)
  {
    Set<MajorMinor> versions  = jarResults.get(file);
    MajorMinor      version   = versions.iterator().next();
    ResultRow       resultRow = new ResultRow(file, version, this);

    results.add(resultRow);
  }

  private void populateTable()
  {
    resultsTable.setModel(new DefaultTableModel());

    EventList<ResultRow> eventList = new BasicEventList<ResultRow>(results);
    // SortedList<ResultRow> sortedList = new SortedList<ResultRow>(eventList);

    // TextComponentMatcherEditor matcherEditor= new TextComponentMatcherEditor();
    // EventTableModel<ResultRow> tableModel = new EventTableModel<ResultRow>(sortedList, new ResultRowTableFormat(results));
    EventTableModel<ResultRow> tableModel = new EventTableModel<ResultRow>(eventList, new ResultRowTableFormat(results));

    resultsTable.setModel(tableModel);

    // TableComparatorChooser<ResultRow> tableSorter = new TableComparatorChooser<ResultRow>(resultsTable, sortedList, true);
  }

  public String filterNames()
  {
    List<String> paths = new ArrayList<String>();

    for (ResultRow result : results)
    {
      paths.add(result.getFile().getAbsolutePath());
    }

    String[] strings = paths.toArray(new String[paths.size()]);

    commonText = StringUtils.getCommonPrefix(strings);

    return commonText;
  }

  private void doQuitAction()
  {
    savePreferences();
    dispose();
    System.exit(0);
  }

  private void savePreferences()
  {
    if (fileDir != null)
    {
      preferences.put(FILES_DIR, fileDir.getAbsolutePath());
    }

    preferences.putInt(JDK_THRESHOLD, jdkThreshold.getVersion());
  }

  private void loadPreferences()
  {
    String fileDirPath = preferences.get(FILES_DIR, null);

    if (fileDirPath != null)
    {
      fileDir = new File(fileDirPath);
    }

    jdkThreshold = findByVersion(preferences.getInt(JDK_THRESHOLD, 49));

    Component[] components = jdkButtonPanel.getComponents();

    for (Component component : components)
    {
      if (component instanceof JdkCheckBox)
      {
        JdkCheckBox box = (JdkCheckBox) component;

        if (box.getJdk() == jdkThreshold)
        {
          box.setSelected(true);
        }
      }
    }
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public String getCommonText()
  {
    return commonText;
  }

  public Jdk getJdkThreshold()
  {
    return jdkThreshold;
  }

  public List<ResultRow> getResults()
  {
    return results;
  }

  /**
   * Method generated by IntelliJ IDEA GUI Designer >>> IMPORTANT!! <<< DO NOT edit this method OR call it in your code!
   *
   * @noinspection  ALL
   */
  private void $$$setupUI$$$()
  {
    createUIComponents();
    contentsPanel = new JPanel();
    contentsPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));

    final JPanel panel1 = new JPanel();

    panel1.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
    contentsPanel.add(panel1,
                      new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    panel1.add(jdkButtonPanel,
               new GridConstraints(0, 1, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    jdkButtonPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "JDK Threshold"));

    final Spacer spacer1 = new Spacer();

    panel1.add(spacer1,
               new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW,
                                   null, null, null, 0, false));

    final JPanel panel2 = new JPanel();

    panel2.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
    panel1.add(panel2,
               new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Directory path length"));
    fullPathsRadioButton = new JRadioButton();
    fullPathsRadioButton.setText("Long file paths");
    panel2.add(fullPathsRadioButton,
               new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));
    fileNameRadioButton = new JRadioButton();
    fileNameRadioButton.setSelected(false);
    fileNameRadioButton.setText("Just file names");
    panel2.add(fileNameRadioButton,
               new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));
    shortFilePathsRadioButton = new JRadioButton();
    shortFilePathsRadioButton.setSelected(true);
    shortFilePathsRadioButton.setText("Short file paths");
    panel2.add(shortFilePathsRadioButton,
               new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));

    final JPanel panel3 = new JPanel();

    panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
    contentsPanel.add(panel3,
                      new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

    final JScrollPane scrollPane1 = new JScrollPane();

    panel3.add(scrollPane1,
               new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    scrollPane1.setViewportView(resultsTable);

    final JPanel panel4 = new JPanel();

    panel4.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    contentsPanel.add(panel4,
                      new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0,
                                          false));
    selectDirsFilesButton = new JButton();
    selectDirsFilesButton.setText("Select Dirs/Files");
    panel4.add(selectDirsFilesButton);
    helpButton = new JButton();
    helpButton.setEnabled(true);
    helpButton.setText("Help");
    panel4.add(helpButton);
    quitButton = new JButton();
    quitButton.setText("Quit");
    panel4.add(quitButton);

    ButtonGroup buttonGroup;

    buttonGroup = new ButtonGroup();
    buttonGroup.add(fullPathsRadioButton);
    buttonGroup.add(fileNameRadioButton);
    buttonGroup.add(shortFilePathsRadioButton);
  }

  /** @noinspection  ALL */
  public JComponent $$$getRootComponent$$$()
  {
    return contentsPanel;
  }
}
