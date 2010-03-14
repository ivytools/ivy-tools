package com.nurflugel.mergegrapher.domain;

import com.nurflugel.mergegrapher.SubversionMergeGrapher;
import org.apache.commons.lang.StringUtils;
import org.tmatesoft.svn.core.SVNPropertyValue;
import java.util.*;
import static com.nurflugel.mergegrapher.SubversionMergeGrapher.BRANCHES;
import static com.nurflugel.mergegrapher.SubversionMergeGrapher.TAGS;
import static com.nurflugel.mergegrapher.SubversionMergeGrapher.TRUNK;
import static com.nurflugel.mergegrapher.domain.Type.A;
import static com.nurflugel.mergegrapher.domain.Type.D;
import static org.apache.commons.lang.StringUtils.replaceChars;

/** This is a representation of a path - could be trunk a branch, or a tag. You can get the full URL by adding this to the project base URL. */
@SuppressWarnings({ "UseOfSystemOutOrSystemErr" })
public class Path implements Comparable
{
  /** the revision the path was created in. */
  private Long creationRevision;

  /** the revision the path was deleted in. */
  private Long deleteRevision;

  /** the branch this was created from. */
  private Path copyPath;

  // headRevision???
  /** the path, minus the base URL of the project. */
  private String pathName;

  /** is the path active? Tags aren't, unless a branch was made from one */
  private boolean                 active       = true;
  private Map<Path, List<String>> mergeHistory = new TreeMap<Path, List<String>>();

  public Path(String pathName)
  {
    this.pathName = pathName.trim();

    if (pathName.startsWith(TAGS))
    {
      active = false;
    }
  }

  // ------------------------ INTERFACE METHODS ------------------------

  // --------------------- Interface Comparable ---------------------

  @Override
  @SuppressWarnings({ "AccessingNonPublicFieldOfAnotherObject" })
  public int compareTo(Object o)
  {
    return pathName.compareTo(((Path) o).pathName);
  }

  // -------------------------- OTHER METHODS --------------------------

  /** If this path has anything interesting concerning this revision, add it to the list. */
  public void addToRanking(Long revision, StringBuffer sb)
  {
    List<Long> interestingRevisions = getInterestingRevisions();

    if (interestingRevisions.contains(revision))
    {
      sb.append(getGraphVizName()).append(revision).append("; ");
    }
  }

  public List<Long> getInterestingRevisions()
  {
    Set<Long> interestingRevisions = new TreeSet<Long>();

    if (creationRevision != null)
    {
      interestingRevisions.add(creationRevision);
    }

    if (deleteRevision != null)
    {
      interestingRevisions.add(deleteRevision);
    }

    return new ArrayList<Long>(interestingRevisions);
  }

  public String getDisplayName()
  {
    return pathName;
  }

  public String getGraphVizName()
  {
    return processSafeName(pathName);
  }

  /** Take a name like branch 1.28 and return something like branch_1_28. */
  public static String processSafeName(String name)
  {
    String badChars = " `-=[]\\;',.~!@/#$%^&*()_+|}{\":<>?)";
    String results  = name;

    if(results.startsWith("/")){
     results=StringUtils.substringAfter(results,"/");
    }
    for (int i = 0; i < badChars.length(); i++)
    {
      results = StringUtils.replace(results, badChars.substring(i, i + 1), "_");
    }

    return results;
  }

  public void handleMergeInfoChange(SubversionMergeGrapher grapher, SVNPropertyValue value, SVNPropertyValue oldValue, Long revision,
                                    List<CopyInfo> copyInfo)
  {
    String textValue = value.toString();
    String oldTextValue = (oldValue == null) ? ""
                                             : oldValue.toString();
    String[] values     = textValue.split("\n");

    String   deltaValue = StringUtils.remove(textValue, oldTextValue);

    for (String theValue : values)
    {
      String       mergePathName  = StringUtils.substringBefore(theValue, ":");
      Path         path           = grapher.getPath(mergePathName);
      String       newMergeValues = StringUtils.substringAfter(theValue, ":");
      List<String> mergeValues    = null;

      if (mergeHistory.containsKey(path))
      {
        mergeValues = mergeHistory.get(path);
      }
      else
      {
        mergeValues = new ArrayList<String>();
        mergeHistory.put(path, mergeValues);
      }

      // newMergerValues=determineNewMergeValues(mergeValues, newMergeValues);
      mergeValues.add(newMergeValues);                                                // todo I still want to figure out what revisions were actually
                                                                                      // added to this.

      MergeInfo mergeInfo = new MergeInfo(path, -1, revision, this, newMergeValues);  // todo how does source revision fit in here?

      copyInfo.add(mergeInfo);

      // }
      System.out.println("      value = " + theValue);
    }

    System.out.println("---->deltaValue = " + deltaValue + " old=" + oldTextValue + "  new=" + textValue);
  }

  /**
   * Modify the path object with the information given. Paths can only have one copy path (where they were copied from), only one copy revision, and
   * only one delete revision.
   *
   * @param  copyPathName  if this is type "A", then this is the path this path was copied from (i.e., if you make a branch from trunk, then trunk is
   *                       the copyPath). Note - this WILL BE NULL for initial creation of trunk, branches, and tag dirs.
   * @param  copyRevision  the revision the branch is being created from
   * @param  type          Either A or D
   * @param  revision      the revision of whatever action is taking place (creation or deletion)
   * @param  copyInfo      the list of creation and merge changes.
   */
  public void modifyPath(SubversionMergeGrapher grapher, Map<String, Path> pathMap, String copyPathName, long copyRevision, Type type, long revision,
                         List<CopyInfo> copyInfo)
  {
    Path theCopyPath = (copyPathName == null) ? null
                                              : pathMap.get(copyPathName);

    if ((copyPathName != null) && !pathMap.containsKey(copyPathName))
    {
      theCopyPath = grapher.getPath(copyPathName);
    }

    if (type == A)
    {
      if ((theCopyPath != null) && (getCopyPath() == null))
      {
        setCopyPath(theCopyPath);
      }

      if (getCreationRevision() == null)
      {
        // if it's trunk/branches/tags, there's no copy revision, use the revision
        if (pathName.equals(TRUNK) || pathName.equals(TAGS) || pathName.equals(BRANCHES))
        {
          setCreationRevision(revision);
        }
        else if ((copyRevision != -1))
        {
          setCreationRevision(copyRevision);
          copyInfo.add(new CreateBranchInfo(theCopyPath, copyRevision, revision, this));
        }
      }
    }

    if ((type == D) && (getDeleteRevision() == null) && (revision != -1))
    {
      setDeleteRevision(revision);
    }
  }

  // todo the declaration for any revisions in this will be the pathName + revision
  public void writePath(List<String> lines, List<Long> allInterestingRevisions)
  {
    String graphVizName = getGraphVizName();

    // write the declarations
    for (Long interestingRevision : allInterestingRevisions)
    {
      String line = graphVizName + interestingRevision + " [label=\"" + pathName + "\\n" + interestingRevision + "\" style=filled color=yellow];";

      lines.add(line);
    }

    // now write the dependencies of the timeline
    if (allInterestingRevisions.size() > 1)
    {
      StringBuilder sb = new StringBuilder(graphVizName + allInterestingRevisions.get(0));

      for (int i = 1; i < allInterestingRevisions.size(); i++)
      {
        sb.append("->").append(graphVizName).append(allInterestingRevisions.get(i));
      }

      sb.append(";");
      lines.add(sb.toString());
    }
  }

  // ------------------------ CANONICAL METHODS ------------------------

  @Override
  @SuppressWarnings({ "AccessingNonPublicFieldOfAnotherObject" })
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }

    if ((o == null) || (getClass() != o.getClass()))
    {
      return false;
    }

    Path path = (Path) o;

    return !((pathName != null) ? (!pathName.equals(path.pathName))
                                : (path.pathName != null));
  }

  @Override
  public int hashCode()
  {
    return (pathName != null) ? pathName.hashCode()
                              : 0;
  }

  @Override
  public String toString()
  {
    return "Path{"
           + "pathName='" + pathName + '\'' + ", creationRevision=" + creationRevision + ", deleteRevision=" + deleteRevision + ", copyPath="
           + copyPath + '}';
  }

  // --------------------- GETTER / SETTER METHODS ---------------------

  public Path getCopyPath()
  {
    return copyPath;
  }

  public void setCopyPath(Path copyPath)
  {
    this.copyPath = copyPath;
  }

  public Long getCreationRevision()
  {
    return creationRevision;
  }

  public void setCreationRevision(Long creationRevision)
  {
    this.creationRevision = creationRevision;
  }

  public Long getDeleteRevision()
  {
    return deleteRevision;
  }

  public void setDeleteRevision(Long deleteRevision)
  {
    this.deleteRevision = deleteRevision;
  }

  public String getPathName()
  {
    return pathName;
  }

  public boolean isActive()
  {
    return active;
  }
}
