package com.nurflugel.ivygrapher;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import static com.nurflugel.ivygrapher.FileDialog.*;

/** List model for the list dialog. */
public class FileDialogListModel implements ListModel
{
  private File              currentDir;
  private FileDialog        fileDialog;
  private List<FileWrapper> files = new ArrayList<FileWrapper>();

  public FileDialogListModel(File currentDir, FileDialog fileDialog)
  {
    this.currentDir = currentDir;
    this.fileDialog = fileDialog;
    scanFiles();
  }

  /** Scan the current dir and find all files available for display. */
  private void scanFiles()
  {
    files.clear();
    files.add(new FileWrapper(new File(UP)));

    File[] fileList = currentDir.listFiles();

    // add files first
    for (File file : fileList)
    {
      if (!file.isDirectory())
      {
        addToFiles(file);
      }
    }

    // add dirs last
    for (File file : fileList)
    {
      if (file.isDirectory())
      {
        addToFiles(file);
      }
    }

    fileDialog.invalidate();

    // fileDialog.validate();
    fileDialog.repaint();
  }

  private void addToFiles(File file)
  {
    String fileName = file.getName();

    if (!fileName.startsWith("resolved") && !fileName.startsWith("."))
    {
      if (file.isDirectory() || fileName.endsWith(".xml"))
      {
        FileWrapper fileWrapper = new FileWrapper(file);

        files.add(fileWrapper);
      }
    }
  }

  // ------------------------ INTERFACE METHODS ------------------------

  // --------------------- Interface ListModel ---------------------

  @Override
  public int getSize()
  {
    return files.size();
  }

  @Override
  public Object getElementAt(int index)
  {
    return files.get(index);
  }

  @Override
  public void addListDataListener(ListDataListener l)
  {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void removeListDataListener(ListDataListener l)
  {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  // -------------------------- OTHER METHODS --------------------------

  public void setCurrentDir(File currentDir)
  {
    System.out.println("setting currentDir = " + currentDir);
    this.currentDir = currentDir;
    scanFiles();
  }
}
