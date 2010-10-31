package com.nurflugel.mergegrapher.domain;

import com.nurflugel.ivybrowser.domain.Revision;
import com.nurflugel.mergegrapher.SubversionMergeGrapher;
import org.apache.commons.lang.StringUtils;
import org.tmatesoft.svn.core.SVNPropertyValue;
import java.util.*;
import static com.nurflugel.ivybrowser.domain.Revision.NO_REVISION;
import static com.nurflugel.mergegrapher.SubversionMergeGrapher.*;
import static com.nurflugel.mergegrapher.domain.Type.A;
import static com.nurflugel.mergegrapher.domain.Type.D;
import static org.apache.commons.lang.StringUtils.replace;
import static org.apache.commons.lang.StringUtils.substringAfter;
import static org.apache.commons.lang.StringUtils.substringBefore;

/** This is a representation of a path - could be trunk a branch, or a tag. You can get the full URL by adding this to the project base URL. */
@SuppressWarnings({ "UseOfSystemOutOrSystemErr" })
public class Path implements Comparable
{
  /** the revision the path was created in. */
  private Revision creationRevision;

  /** last revision where somebody did something. */
  private Revision lastActiveRevision;

  /** the revision the path was deleted in. */
  private Revision deleteRevision;

  /** the branch this was created from. */
  private Path copyPath;

  // headRevision???
  /** the path, minus the base URL of the project. */
  private String pathName;

  // contains the set of revisions for this path - all good stuff, plus any merges
  private Set<Revision> interestingRevisions = new TreeSet<Revision>();

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
  public void addToRanking(Revision revision, StringBuffer sb)
  {
    if (interestingRevisions.contains(revision))
    {
      sb.append(getGraphVizName()).append(revision).append("; ");
    }
  }

  public List<Revision> getInterestingRevisions(List<CopyInfo> copyInfoList)
  {
    Set<Revision> revisions = new TreeSet<Revision>();

    revisions.addAll(interestingRevisions);

    for (CopyInfo copyInfo : copyInfoList)
    {
      if (copyInfo instanceof CreateBranchInfo)
      {
        if (equals(((CreateBranchInfo) copyInfo).getCreatedPath()))
        {
          revisions.add(copyInfo.getCopyRevision());
        }
      }
    }

    return new ArrayList<Revision>(revisions);
  }

  private void addInterestingRevision(Set<Revision> revisions, Revision revision)
  {
    if (revision != null)
    {
      revisions.add(revision);
    }
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
    String results = name;

    if (results.startsWith("/"))
    {
      results = substringAfter(results, "/");
    }

    String badChars = " `-=[]\\;',.~!@/#$%^&*()_+|}{\":<>?)";

    for (int i = 0; i < badChars.length(); i++)
    {
      results = replace(results, badChars.substring(i, i + 1), "_");
    }

    return results;
  }

  // dbulla CODEREVIEW - unit test this method
  public void handleMergeInfoChange(SubversionMergeGrapher grapher, SVNPropertyValue value, SVNPropertyValue oldValue, Revision revision,
                                    List<CopyInfo> copyInfo)
  {
    String textValue    = value.toString();
    String oldTextValue = (oldValue == null) ? ""
                                             : oldValue.toString();
    String[] values     = textValue.split("\n");
    String   deltaValue = StringUtils.remove(textValue, oldTextValue);

    for (String theValue : values)
    {
      String       mergePathName  = substringBefore(theValue, ":");
      Path         path           = grapher.getPath(mergePathName);
      String       newMergeValues = substringAfter(theValue, ":");
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
      mergeValues.add(newMergeValues);                                                         // todo I still want to figure out what
                                                                                               // revisions were actually
                                                                                               // added to this.

      MergeInfo mergeInfo = new MergeInfo(path, NO_REVISION, revision, this, newMergeValues);  // todo how does source revision fit in here?

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
  public void modifyPath(SubversionMergeGrapher grapher, Map<String, Path> pathMap, String copyPathName, Revision copyRevision, Type type,
                         Revision revision, List<CopyInfo> copyInfo)
  {
    Path theCopyPath = (copyPathName == null) ? null
                                              : pathMap.get(copyPathName);

    if ((copyPathName != null) && !pathMap.containsKey(copyPathName))
    {
      theCopyPath = grapher.getPath(copyPathName);
    }

    if (type == A)
    {
      Path copypath = getCopyPath();

      if ((theCopyPath != null) && (copypath == null))
      {
        setCopyPath(theCopyPath);
      }

      Revision theCreationRevision = getCreationRevision();

      if (theCreationRevision == null)
      {
        // if it's trunk/branches/tags, there's no copy revision, use the revision
        if (pathName.equals(TRUNK) || pathName.equals(TAGS) || pathName.equals(BRANCHES))
        {
          setCreationRevision(revision);
        }
        else if ((copyRevision.isRealRevision()))
        {
          setCreationRevision(copyRevision);
          copyInfo.add(new CreateBranchInfo(theCopyPath, copyRevision, revision, this));
        }
      }
    }
    else if ((type == D) && (getDeleteRevision() == null) && (revision.isRealRevision()))
    {
      setDeleteRevision(revision);
    }
  }

  // todo the declaration for any revisions in this will be the pathName + revision
  public void writePath(List<String> lines)
  {
    String graphVizName = getGraphVizName();

    lines.add(graphVizName + " [label=\"\" shape=plaintext];");

    // write the declarations
    for (Revision revision : interestingRevisions)
    {
      String line = graphVizName + revision + " [label=\"" + pathName + "\\n" + revision + "\" style=filled color=yellow];";

      lines.add(line);
    }

    // now write the dependencies of the timeline
    if (interestingRevisions.size() > 1)
    {
      List<Revision> revisions = new ArrayList<Revision>();

      revisions.addAll(interestingRevisions);

      StringBuilder sb = new StringBuilder(graphVizName + revisions.get(0));

      for (int i = 1; i < interestingRevisions.size(); i++)
      {
        sb.append("->").append(graphVizName).append(revisions.get(i));
      }

      sb.append("[weight=9999];");
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

  public Revision getCreationRevision()
  {
    return creationRevision;
  }

  public void setCreationRevision(Revision creationRevision)
  {
    this.creationRevision = creationRevision;
    interestingRevisions.add(creationRevision);
  }

  public Revision getDeleteRevision()
  {
    return deleteRevision;
  }

  public void setDeleteRevision(Revision deleteRevision)
  {
    this.deleteRevision = deleteRevision;
    interestingRevisions.add(deleteRevision);
  }

  public String getPathName()
  {
    return pathName;
  }

  public boolean isActive()
  {
    return active;
  }

  public void setLastActiveRevision(Revision lastActiveRevision)
  {
    // remove the old last revision
    if (this.lastActiveRevision != null)
    {
      interestingRevisions.remove(this.lastActiveRevision);
    }

    this.lastActiveRevision = lastActiveRevision;

    // now add the new one
    interestingRevisions.add(lastActiveRevision);
  }
}
