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
}
