package com.nurflugel.mergegrapher;

import org.testng.annotations.Test;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class SubversionMergeGrapherTest
{
  @Test
  public void testIsNewBranch()
  {
    SubversionMergeGrapher grapher = new SubversionMergeGrapher();

    assertTrue(grapher.isNewBranch("/branches", "/branches"));
    assertFalse(grapher.isNewBranch("/branches", "/tags"));
    assertTrue(grapher.isNewBranch("/branches/newBranch", "/branches"));
    assertFalse(grapher.isNewBranch("/branches/newBranch/newDir", "/branches"));
    assertFalse(grapher.isNewBranch("/branches/newBranch/newDir/diddkd", "/branches"));
  }

  @Test
  public void testShouldProcess()
  {
    SubversionMergeGrapher grapher = new SubversionMergeGrapher();

    assertTrue(grapher.shouldProcessPath("/trunk"));
    assertTrue(grapher.shouldProcessPath("/branches/newBranch"));
    assertTrue(grapher.shouldProcessPath("/tags/Branches_"));

    assertFalse(grapher.shouldProcessPath("/branches"));
    assertFalse(grapher.shouldProcessPath("/tags"));
    assertFalse(grapher.shouldProcessPath("/tags/newBranch"));
    assertFalse(grapher.shouldProcessPath("/tags/Production"));
  }
}
