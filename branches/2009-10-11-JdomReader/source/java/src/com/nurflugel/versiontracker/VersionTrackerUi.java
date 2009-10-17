package com.nurflugel.versiontracker;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import static com.nurflugel.common.ui.Util.centerApp;
import static com.nurflugel.common.ui.Util.setLookAndFeel;
import static com.nurflugel.common.ui.Version.VERSION;
import org.apache.commons.lang.StringUtils;
import javax.swing.*;
import static javax.swing.JFileChooser.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import static java.awt.Cursor.*;
import static java.awt.Cursor.*;
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

/** Find class versions in libraries. */
@SuppressWarnings({ "UseOfSystemOutOrSystemErr", "CallToPrintStackTrace" })
public class VersionTrackerUi extends JFrame
{
  private static final String FILES_DIR             = "filesDir";
  private static final String JDK_THRESHOLD         = "jdkThreshold";
  private JButton             selectDirsFilesButton;
  private JButton             quitButton;
  private JButton             helpButton;
  private JTable              resultsTable;
  private JPanel              contentsPanel;
  private JPanel              jdkButtonPanel;
  private JRadioButton        fullPathsRadioButton;
  private JRadioButton        shortPathsRadioButton;
  private ButtonGroup         jdkButtonGroup;
  private Preferences         preferences;
  private File                fileDir;
  private File                tempDir               = new File("tempDir");
  private Jdk                 jdkThreshold          = Jdk.JDK15;

  public VersionTrackerUi()
  {
    setTitle("Version Tracker v. " + VERSION);
    setContentPane(contentsPanel);
    addListeners();
    setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", this);
    preferences = Preferences.userNodeForPackage(VersionTrackerUi.class);
    loadPreferences();

    pack();
    centerApp(this);
    setVisible(true);
    shortPathsRadioButton.addActionListener(new ActionListener()
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
  }

  private void addListeners()
  {
    selectDirsFilesButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent actionEvent)
        {
          setCursor(Cursor.WAIT_CURSOR);
          processDirs();
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
      List<ResultRow>        results       = new ArrayList<ResultRow>();
      File[]                 selectedFiles = fileChooser.getSelectedFiles();
      Map<File, Set<String>> jarResults    = new HashMap<File, Set<String>>();  // map of string(jar path, version list)

      for (File file : selectedFiles)
      {
        preferences.put(FILES_DIR, file.getParent());

        System.out.println("file = " + file);
        processArg(file, jarResults);
      }

      Set<File>  files    = jarResults.keySet();
      List<File> fileList = new ArrayList<File>(files);

      Collections.sort(fileList);

      int maxLength = 0;

      for (File file : fileList)
      {
        maxLength = Math.max(maxLength, file.getAbsolutePath().length());
      }

      for (File file : fileList)
      {
        StringBuilder builder  = new StringBuilder();
        Set<String>   versions = jarResults.get(file);

        for (String version : versions)
        {
          builder.append(version).append(" ");
        }

        // add to table model
        // System.out.println(file.getAbsolutePath() + getWhiteSpace(file, maxLength) + builder);
        ResultRow resultRow = new ResultRow(file, builder.toString(), this);

        results.add(resultRow);
      }

      populateTable(results);
      tempDir.deleteOnExit();
    }
  }

  private void processArg(File file, Map<File, Set<String>> jarResults)
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

  private void processJarFile(File jarFile, Map<File, Set<String>> jarResults)
  {
    if (jarFile.getName().endsWith(".jar"))
    {
      deleteFile(tempDir);
      tempDir.mkdirs();
      unzipFile(jarFile);
      processExpandedDir(jarResults, jarFile);
      deleteFile(tempDir);
    }
  }

  @SuppressWarnings({ "ResultOfMethodCallIgnored", "IOResourceOpenedButNotSafelyClosed" })
  private void unzipFile(File file)
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

        if (entry.isDirectory())
        {
          // Assume directories are stored parents first then children.
          // System.err.println("Extracting directory: " + name);
          // This is not robust, just for demonstration purposes.
          newFile.mkdir();

          continue;
        }

        // System.err.println("Extracting file: " + name);
        if (name.endsWith(".class"))
        {
          File dir = newFile.getParentFile();

          dir.mkdirs();

          FileOutputStream     fileOutputStream = new FileOutputStream(newFile);
          BufferedOutputStream out              = new BufferedOutputStream(fileOutputStream);
          InputStream          inputStream      = zipFile.getInputStream(entry);

          copyInputStream(inputStream, out);
          fileOutputStream.close();

          break;
        }
      }

      zipFile.close();
    }
    catch (IOException ioe)
    {
      System.err.println("Unhandled (but not fatal) exception:");
      ioe.printStackTrace();
    }
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

  private void processExpandedDir(Map<File, Set<String>> jarResults, File jarFile)
  {
    File[]  files        = tempDir.listFiles();
    boolean wasProcessed = false;

    for (File file : files)
    {
      if (file.isDirectory())
      {
        wasProcessed = processDir(file, jarResults, jarFile, wasProcessed);
      }

      if (file.getName().endsWith(".class"))
      {
        if (!wasProcessed)
        {
          wasProcessed = true;  // todo pass this in as a parameter, so we can force recursion into all dirs
          processClassFile(file, jarResults, jarFile);
        }
      }

      deleteFile(file);
    }
  }

  private void processClassFile(File file, Map<File, Set<String>> jarResults, File jarFile)
  {
    try
    {
      // call javap with tempdir as classapth
      // remove tempdir from file name, convert file seperators to "."
      // call javap
      // parse output to catch major version
      // put into map for jar file
      String className = getClassName(file);

      className = StringUtils.substringBefore(className, ".class");

      Runtime runtime = Runtime.getRuntime();

      Process process = runtime.exec("javap -v -classpath " + tempDir.getAbsolutePath() + " " + className);

      printOutput(file, process, jarFile, jarResults);
      deleteFile(file);
    }
    catch (IOException e)
    {
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

  private void printOutput(File file, Process process, File jarFile, Map<File, Set<String>> jarResults) throws IOException
  {
    InputStream       inputStream  = process.getInputStream();
    InputStreamReader streamReader = new InputStreamReader(inputStream);
    BufferedReader    reader       = new BufferedReader(streamReader);
    String            line         = reader.readLine();
    int               i            = 0;

    while (line != null)
    {
      // System.out.println("line["+ i++ +"] = " + line);
      if (StringUtils.contains(line, "major version: "))
      {
        // System.out.println("line = " + line);
        String version = StringUtils.substringAfter(line, "major version: ");

        // System.out.println("Jar: " + jarFile.getName() + "     File: " + file.getName() + "     version = " + version);
        Set<String> versions = jarResults.get(jarFile);

        if (versions == null)
        {
          versions = new HashSet<String>();
          jarResults.put(jarFile, versions);
        }

        versions.add(version);
        process.destroy();

        break;
      }

      line = reader.readLine();
    }
  }

  private void deleteFile(File file)
  {
    boolean result = file.delete();

    if (!result) {}
  }

  private boolean processDir(File dir, Map<File, Set<String>> jarResults, File jarFile, boolean processed)
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
      else if (file.getName().endsWith(".class"))
      {
        if (!wasProcessed)
        {
          processClassFile(file, jarResults, jarFile);
          wasProcessed = true;
        }

        deleteFile(file);
      }
    }

    return wasProcessed;
  }

  private void populateTable(List<ResultRow> results)
  {
    resultsTable.setModel(new DefaultTableModel());

    EventList<ResultRow>  eventList  = new BasicEventList<ResultRow>(results);
    SortedList<ResultRow> sortedList = new SortedList<ResultRow>(eventList);

    // TextComponentMatcherEditor matcherEditor= new TextComponentMatcherEditor();
    EventTableModel<ResultRow> tableModel = new EventTableModel<ResultRow>(sortedList, new ResultRowTableFormat());

    resultsTable.setModel(tableModel);

    TableComparatorChooser<ResultRow> tableSorter = new TableComparatorChooser<ResultRow>(resultsTable, sortedList, true);
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

    jdkThreshold = Jdk.findByVersion(preferences.getInt(JDK_THRESHOLD, 49));

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

  private void refreshTableDisplay()
  {
    resultsTable.invalidate();
    resultsTable.repaint();
  }

  // -------------------------- OTHER METHODS --------------------------

  private void createUIComponents()
  {
    resultsTable = new JTable();
    resultsTable.setDefaultRenderer(Object.class, new VersionTableCellRenderer(this));
    jdkButtonPanel = new JPanel();
    jdkButtonGroup = new ButtonGroup();

    BoxLayout boxLayout = new BoxLayout(jdkButtonPanel, BoxLayout.Y_AXIS);

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

  public boolean useShortPaths()
  {
    return shortPathsRadioButton.isSelected();
  }

  // --------------------------- main() method ---------------------------

  public static void main(String[] args)
  {
    VersionTrackerUi ui = new VersionTrackerUi();
  }

  // --------------------- GETTER / SETTER METHODS ---------------------

  public Jdk getJdkThreshold()
  {
    return jdkThreshold;
  }
}
