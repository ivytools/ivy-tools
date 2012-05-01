package com.nurflugel.mergegrapher;

import com.nurflugel.Os;
import com.nurflugel.ivybrowser.domain.Revision;
import com.nurflugel.ivygrapher.GraphVizHandler;
import com.nurflugel.ivygrapher.Module;
import com.nurflugel.ivygrapher.NodeOrder;
import com.nurflugel.ivygrapher.OutputFormat;
import com.nurflugel.mergegrapher.domain.CopyInfo;
import com.nurflugel.mergegrapher.domain.Path;
import java.io.File;
import java.io.IOException;
import java.util.*;
import static org.apache.commons.io.FileUtils.writeLines;

/** Handler to deal with GraphViz. */
public class GraphVizOutput extends GraphVizHandler
{
  public GraphVizOutput(Os os, OutputFormat outputFormat, boolean deleteDotFileOnExit, String dotExecutablePath, boolean concentrateEdges,
                        NodeOrder nodeOrder)
  {
    super(os, outputFormat, deleteDotFileOnExit, dotExecutablePath, concentrateEdges, nodeOrder);
  }

  public File makeDotFile(Map<String, Path> pathMap, List<CopyInfo> copyInfo, String repositoryName, String dirPath) throws IOException
  {
    String         fileName                = repositoryName + ".dot";
    List<String>   lines                   = new ArrayList<String>();
    List<Revision> allInterestingRevisions = findAllInterestingRevisions(pathMap, copyInfo);

    writeOpeningText(lines, repositoryName);
    writeRevisionsGraph(allInterestingRevisions, lines);

    for (Path path : pathMap.values())
    {
      // we just want to write the interesting revisions for this path.  Should we have a collection of merges?
      path.writePath(lines);
    }

    for (CopyInfo info : copyInfo)
    {
      info.writeInfo(lines);
    }

    writeRanking(lines, allInterestingRevisions, pathMap);
    lines.add("}");

    File file = new File(dirPath, fileName);

    System.out.println("Writing output file " + file.getAbsolutePath());
    writeLines(file, lines);

    return file;
  }

  /**
   * Write the rankings, so all the same revisions appear on the same line.
   *
   * <p>Basically, for each "interesting" revision, see if any of the paths have a corresponding revision. If so, then they have to add themselves to
   * the list.</p>
   *
   * <p>The output should be a line like this: { rank = same; r432 ;432};</p>
   */
  private void writeRanking(List<String> lines, List<Revision> interestingRevisions, Map<String, Path> pathMap)
  {
    for (Revision revision : interestingRevisions)
    {
      if (revision.isRealRevision())
      {
        StringBuffer sb = new StringBuffer("{ rank = same; ");

        sb.append("r").append(revision).append("; ");

        for (Path path : pathMap.values())
        {
          path.addToRanking(revision, sb);
        }

        sb.append("};");
        lines.add(sb.toString());
      }
    }
  }

  /** Write the lines of the revisions. */
  private void writeRevisionsGraph(List<Revision> interestingRevisions, List<String> lines)
  {
    lines.add("r [label=\"\" shape=plaintext];");

    // write the declarations
    for (Revision interestingRevision : interestingRevisions)
    {
      if (interestingRevision.isRealRevision())
      {
        lines.add("r" + interestingRevision + " [label=\"" + interestingRevision + "\" shape=plaintext];");
      }
    }

    // write the dependencies
    // lines.add("node [shape=plaintext, fontsize=16];");
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < interestingRevisions.size(); i++)
    {
      Revision interestingRevision = interestingRevisions.get(i);

      if (interestingRevision.isRealRevision())
      {
        sb.append("r").append(interestingRevision);

        if (i < (interestingRevisions.size() - 1))
        {
          sb.append("->");
        }
      }
    }

    sb.append("[weight=9999];");
    lines.add(sb.toString());
  }

  /**
   * Write the opening lines of the graph.
   *
   * @param  lines           the list to add to
   * @param  repositoryName  the name of the repository
   */
  private void writeOpeningText(List<String> lines, String repositoryName)
  {
    lines.add("");
    lines.add("digraph G {");
    lines.add("    node [shape=ellipse,fontname=\"Arial\",fontsize=\"10\"];");
    lines.add("    edge [fontname=\"Arial\",fontsize=\"8\"];");
    lines.add("    headline [label=\"" + repositoryName + " Merge History\", shape=none, fontsize=\"20\"];");
  }

  /** Go through all the items and find anything of interest that we'll need to show in the graph. */
  private List<Revision> findAllInterestingRevisions(Map<String, Path> pathMap, List<CopyInfo> copyInfo)
  {
    List<Revision> interestingRevisions = new ArrayList<Revision>();

    for (Path path : pathMap.values())
    {
      interestingRevisions.addAll(path.getInterestingRevisions(copyInfo));
    }

    for (CopyInfo info : copyInfo)
    {
      interestingRevisions.addAll(info.getInterestingRevisions());
    }

    // quick and dirty way to remove all duplicates
    Set<Revision> set = new TreeSet<Revision>(interestingRevisions);

    interestingRevisions.clear();
    interestingRevisions.addAll(set);

    return interestingRevisions;
  }

  @Override
  protected void writeDotFileTargetDeclarations(Module ivyModule, Map<String, Module> moduleMap, List<String> lines)
  {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  protected void writeDotDependencies(Map<String, Module> moduleMap, List<String> lines, Module ivyModule)
  {
    // To change body of implemented methods use File | Settings | File Templates.
  }
}
