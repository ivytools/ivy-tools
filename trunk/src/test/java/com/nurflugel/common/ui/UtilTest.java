package com.nurflugel.common.ui;

import org.testng.annotations.Test;
import static com.nurflugel.common.ui.Util.filterHttp;
import static org.testng.Assert.assertEquals;

public class UtilTest
{
  @Test(groups = { "unit" })
  public void testTrimHttp()
  {
    assertEquals(filterHttp("http://www.nurflugel.com:8090/svn/projects/aaaa"), "projects/aaaa");
    assertEquals(filterHttp("http://www.nurflugel.com/svn/projects/aaaa"), "projects/aaaa");
    assertEquals(filterHttp("http://www.nurflugel.com/projects/aaaa"), "projects/aaaa");
    assertEquals(filterHttp("http://www.nurflugel.svn.com/projects/aaaa"), "projects/aaaa");
  }
}
