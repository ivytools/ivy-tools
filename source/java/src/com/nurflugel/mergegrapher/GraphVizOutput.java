package com.nurflugel.mergegrapher;

import com.nurflugel.mergegrapher.domain.CopyInfo;
import com.nurflugel.mergegrapher.domain.Path;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.*;

/** Handler to deal with GraphViz. */
public class GraphVizOutput
{
  public GraphVizOutput(Map<String, Path> pathMap, List<CopyInfo> copyInfo) throws IOException
  {
    List<String> lines                = new ArrayList<String>();
    List<Long>   interestingRevisions = findAllInterestingRevisions(pathMap, copyInfo);

    writeOpeningText(lines);
    writeRevisionsGraph(interestingRevisions, lines);

    for (Path path : pathMap.values())
    {
      path.writePath(lines);
    }

    for (CopyInfo info : copyInfo)
    {
      info.writeInfo(lines);
    }

    writeRanking(lines, interestingRevisions, pathMap);

    lines.add("}");

    FileUtils.writeLines(new File("output.dot"), lines);
  }

  /**
   * Write the rankings, so all the same revisions appear on the same line.
   *
   * <p>Basically, for each "interesting" revision, see if any of the paths have a corresponding revision. If so, then they have to add themselves to
   * the list.</p>
   *
   * <p>The output should be a line like this: { rank = same; r432 ;432};</p>
   */
  private void writeRanking(List<String> lines, List<Long> interestingRevisions, Map<String, Path> pathMap)
  {
    for (Long revision : interestingRevisions)
    {
      if (revision != -1)
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
  private void writeRevisionsGraph(List<Long> interestingRevisions, List<String> lines)
  {
    // write the declarations
    for (Long interestingRevision : interestingRevisions)
    {
      if (interestingRevision != -1)
      {
        lines.add("r" + interestingRevision + " [label=\"" + interestingRevision + "\" shape=plaintext];");
      }
    }

    // write the dependencies
    lines.add("node [shape=plaintext, fontsize=16];");

    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < interestingRevisions.size(); i++)
    {
      Long interestingRevision = interestingRevisions.get(i);

      if (interestingRevision != -1)
      {
        sb.append("r").append(interestingRevision);

        if (i < (interestingRevisions.size() - 1))
        {
          sb.append("->");
        }
      }
    }

    sb.append(";");
    lines.add(sb.toString());
  }

  /**
   * Write the opening lines of the graph.
   *
   * @param  lines  the list to add to
   */
  private void writeOpeningText(List<String> lines)
  {
    lines.add("");
    lines.add("digraph G {");
    lines.add("    node [shape=ellipse,fontname=\"Arial\",fontsize=\"10\"];");
    lines.add("    edge [fontname=\"Arial\",fontsize=\"8\"];");
    lines.add("    headline [label=\"CDM Branch Merge History\", shape=none, fontsize=\"20\"];");
  }

  /** Go through all the items and find anything of interest that we'll need to show in the graph. */
  private List<Long> findAllInterestingRevisions(Map<String, Path> pathMap, List<CopyInfo> copyInfo)
  {
    List<Long> interestingRevisions = new ArrayList<Long>();

    for (Path path : pathMap.values())
    {
      interestingRevisions.addAll(path.getInterestingRevisions());
    }

    for (CopyInfo info : copyInfo)
    {
      interestingRevisions.addAll(info.getInterestingRevisions());
    }

    // quick and dirty way to remove all duplicates
    Set<Long> set = new TreeSet<Long>(interestingRevisions);

    interestingRevisions.clear();
    interestingRevisions.addAll(set);

    return interestingRevisions;
  }
}
