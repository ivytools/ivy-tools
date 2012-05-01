package com.nurflugel.mergegrapher;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.io.File;
import java.io.IOException;
import static org.apache.commons.io.FileUtils.readLines;
import static org.testng.Assert.*;

public class SubversionMergeGrapherTest
{
  private SubversionMergeGrapher grapher;
  private GraphVizOutput         graphVizOutput;

  // -------------------------- OTHER METHODS --------------------------
  @BeforeClass(groups = "mergeGrapher")
  public void setUp()
  {
    grapher = new SubversionMergeGrapher();
    grapher.setDirPath("build/testOut");
    graphVizOutput = grapher.getGraphVizOutput();
  }

  @Test(groups = "mergeGrapher")
  public void testGrapher03() throws IOException
  {
    setUp();
    doDotTest("grapher03");
  }

  private void doDotTest(String name) throws IOException
  {
    grapher.setProjectBaseUrl("http://localhost/svn/" + name);

    File dotFile = grapher.generateDotFile(graphVizOutput);

    // todo compare dot file with some expected value
    assertEquals(readLines(dotFile), readLines(new File("build/resources/test/" + name + "_expected.dot")), "Should have got expected results");
  }

  @Test(groups = "mergeGrapher")
  public void testGrapher04() throws IOException
  {
    doDotTest("grapher04");
  }

  @Test(groups = "mergeGrapher")
  public void testIsNewBranch()
  {
    assertTrue(grapher.isNewBranch("/branches", "/branches"));
    assertFalse(grapher.isNewBranch("/branches", "/tags"));
    assertTrue(grapher.isNewBranch("/branches/newBranch", "/branches"));
    assertFalse(grapher.isNewBranch("/branches/newBranch/newDir", "/branches"));
    assertFalse(grapher.isNewBranch("/branches/newBranch/newDir/diddkd", "/branches"));
  }

  @Test(groups = "mergeGrapher")
  public void testShouldProcess()
  {
    assertTrue(grapher.shouldProcessPath("/trunk"));
    assertTrue(grapher.shouldProcessPath("/branches/newBranch"));
    assertTrue(grapher.shouldProcessPath("/tags/Branches_"));
    assertFalse(grapher.shouldProcessPath("/branches"));
    assertFalse(grapher.shouldProcessPath("/tags"));
    assertFalse(grapher.shouldProcessPath("/tags/newBranch"));
    assertFalse(grapher.shouldProcessPath("/tags/Production"));
  }
}
