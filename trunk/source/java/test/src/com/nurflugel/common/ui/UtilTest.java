package com.nurflugel.common.ui;

import org.testng.Assert;
import org.testng.annotations.Test;

public class UtilTest
{
  @Test(groups = { "unit" })
  public void testTrimHttp()
  {
    Assert.assertEquals(Util.filterHttp("http://www.nurflugel.com:8090/svn/projects/aaaa"), "projects/aaaa");
    Assert.assertEquals(Util.filterHttp("http://www.nurflugel.com/svn/projects/aaaa"), "projects/aaaa");
    Assert.assertEquals(Util.filterHttp("http://www.nurflugel.com/projects/aaaa"), "projects/aaaa");
    Assert.assertEquals(Util.filterHttp("http://www.nurflugel.svn.com/projects/aaaa"), "projects/aaaa");
  }
}
