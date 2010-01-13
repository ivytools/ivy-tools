package com.nurflugel.externalsreporter.ui;

import java.io.IOException;

/**  */
@SuppressWarnings({ "CallToPrintStackTrace" })
public class ScanDirForExternalsTask implements Runnable
{
  private String            file;
  private RepositoryScanner repositoryScanner;

  public ScanDirForExternalsTask(String file, RepositoryScanner repositoryScanner)
  {
    this.file              = file;
    this.repositoryScanner = repositoryScanner;
  }

  @Override
  public void run()
  {
    try
    {
      repositoryScanner.findExternals(file);
    }
    catch (IOException e)
    {
      e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
    }
  }
}
