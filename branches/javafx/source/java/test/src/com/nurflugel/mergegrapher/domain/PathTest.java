package com.nurflugel.mergegrapher.domain;

import org.testng.annotations.Test;
import static com.nurflugel.mergegrapher.domain.Path.processSafeName;
import static org.testng.Assert.assertEquals;

public class PathTest
{
  @Test
  public void testAddToRanking() {}

  @Test
  public void testGetInterestingRevisions() {}

  @Test
  public void testHandleMergeInfoChange() {}

  @Test
  public void testModifyPath() {}

  @Test
  public void testProcessSafeName()
  {
    assertEquals(processSafeName("abcdefg 123"), "abcdefg_123");
    assertEquals(processSafeName("abcdefg.123"), "abcdefg_123");
    assertEquals(processSafeName("abcdefg~123"), "abcdefg_123");
    assertEquals(processSafeName("abcdefg"), "abcdefg");
    assertEquals(processSafeName("abcdefg123"), "abcdefg123");
  }
}
