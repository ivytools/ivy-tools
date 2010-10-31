package com.nurflugel.ivyformatter.ui;

import org.w3c.tidy.Tidy;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jul 28, 2008 Time: 5:57:02 PM To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({ "UseOfSystemOutOrSystemErr" })
public class IvyFormatterProcess
{
  private static final String NEW_LINE = "\n";

  private IvyFormatterProcess() {}

  // --------------------------- main() method ---------------------------
  public static void main(String[] args)
  {
    if ((args.length == 2) && args[0].equalsIgnoreCase("-file"))
    {
      formatFile(args[1]);
    }
    else
    {
      System.out.println("Usage: com.nurflugel.ivyformatter.IvyFormatterProcess -file fullyQualifiedFilePath");
    }
  }

  @SuppressWarnings({ "CallToPrintStackTrace" })
  public static void formatFile(String fileName)
  {
    if (fileName.endsWith("ivy.xml"))
    {
      try
      {
        String text = readFile(fileName);

        text = formatIvyFileText(text);
        writeFile(fileName, text);
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
  }

  private static String readFile(String fileName) throws IOException
  {
    File           ivyFile    = new File(fileName);
    StringBuilder  sb         = new StringBuilder();
    FileReader     fileReader = new FileReader(ivyFile);
    BufferedReader br         = new BufferedReader(fileReader);
    String         line;

    while ((line = br.readLine()) != null)
    {
      // System.out.println(line);
      sb.append(line);
    }

    br.close();
    fileReader.close();

    return sb.toString();
  }

  static String formatIvyFileText(String ivyText)
  {
    String text = ivyText;

    text = text.replaceAll("\n\n", "~~");

    Tidy tidy = new Tidy();  // obtain a new Tidy instance

    tidy.setTabsize(4);
    tidy.setIndentContent(true);

    // tidy.setXHTML(true); // set desired config options using tidy setters
    tidy.setDocType("XML");

    // tidy.setMakeClean(false);
    tidy.setWraplen(300);
    tidy.setXmlOut(true);
    tidy.setSmartIndent(true);

    // tidy.set
    tidy.setXmlTags(true);

    InputStream  inputStream  = new ByteArrayInputStream(text.getBytes());
    OutputStream outputStream = new ByteArrayOutputStream();

    tidy.parse(inputStream, outputStream);  // run tidy, providing an input and output stream
    text = outputStream.toString();

    // now, fix any wierdness so we get the format we want
    text = text.replaceAll("~~", "\n\n");
    text = text.replaceAll("\r\n", "\n");
    text = text.replaceAll("\n\n", "\n");
    text = text.replaceAll("\n\n", "\n");
    text = text.replaceAll("-&gt;", "->");
    text = text.replaceAll("\n *</dependency>", "\n        </dependency>");

    String[] lines = text.split(NEW_LINE);

    formatConfLines(lines);
    formatPublications(lines);
    formatDependencyLines(lines);
    formatExcludeLines(lines);
    formatFieldLines(lines);
    formatFilterLines(lines);
    formatFieldTypeLines(lines);
    formatDynamicFieldLines(lines);
    formatCopyFieldLines(lines);
    unindentExcludes(lines);
    text = pasteLinesTogether(lines);

    return text;
  }

  /**
   * Align everything line.
   *
   * <p><conf name="build" visibility="public" description="Dependencies only used during the build process."/></p>
   *
   * <p><conf name="dist" visibility="public" description="Dependencies that will be deployed via WebStart."/>.</p>
   */
  private static void formatConfLines(String[] lines)
  {
    List<Integer> confLines = getAffectedLines(lines, "<conf", "name");

    if (confLines.isEmpty())
    {
      return;
    }

    indent(confLines, lines, 8);
    alignLinesOnWord(confLines, lines, "visibility=");
    alignLinesOnWord(confLines, lines, "description=");
  }

  /** Align fieldtype lines. */
  private static void formatFieldTypeLines(String[] lines)
  {
    List<Integer> confLines = getAffectedLines(lines, "<fieldType");

    if (confLines.isEmpty())
    {
      return;
    }

    // indent(confLines, lines, 8);
    alignLinesOnWord(confLines, lines, "name=");
    alignLinesOnWord(confLines, lines, "class=");
    alignLinesOnWord(confLines, lines, "sortMissingLast=");
    alignLinesOnWord(confLines, lines, "precisionStep=");
    alignLinesOnWord(confLines, lines, "omitNorms=");
    alignLinesOnWord(confLines, lines, "positionIncrementGap=");
  }

  /** Align fieldtype lines. */
  private static void formatFilterLines(String[] lines)
  {
    List<Integer> confLines = getAffectedLines(lines, "<filter");

    if (confLines.isEmpty())
    {
      return;
    }

    // indent(confLines, lines, 8);
    alignLinesOnWord(confLines, lines, "class=");
    alignLinesOnWord(confLines, lines, "generateWordParts=");
    alignLinesOnWord(confLines, lines, "precisionStep=");
    alignLinesOnWord(confLines, lines, "omitNorms=");
    alignLinesOnWord(confLines, lines, "positionIncrementGap=");
  }

  /** Align field lines. */
  private static void formatFieldLines(String[] lines)
  {
    List<Integer> confLines = getAffectedLines(lines, "<field", "name");

    if (confLines.isEmpty())
    {
      return;
    }

    // indent(confLines, lines, 8);
    alignLinesOnWord(confLines, lines, "name=");
    alignLinesOnWord(confLines, lines, "type=");
    alignLinesOnWord(confLines, lines, "indexed=");
    alignLinesOnWord(confLines, lines, "stored=");
    alignLinesOnWord(confLines, lines, "compressed=");
    alignLinesOnWord(confLines, lines, "multiValued=");
    alignLinesOnWord(confLines, lines, "omitNorms=");
    alignLinesOnWord(confLines, lines, "termVectors=");
    alignLinesOnWord(confLines, lines, "termPositions=");
    alignLinesOnWord(confLines, lines, "termOffsets=");
    alignLinesOnWord(confLines, lines, "default=");
  }

  /** Align dynamic field lines. */
  private static void formatDynamicFieldLines(String[] lines)
  {
    List<Integer> confLines = getAffectedLines(lines, "<dynamicField");

    if (confLines.isEmpty())
    {
      return;
    }

    // indent(confLines, lines, 8);
    alignLinesOnWord(confLines, lines, "name=");
    alignLinesOnWord(confLines, lines, "type=");
    alignLinesOnWord(confLines, lines, "indexed=");
    alignLinesOnWord(confLines, lines, "stored=");
  }

  /** Align dynamic field lines. */
  private static void formatCopyFieldLines(String[] lines)
  {
    List<Integer> confLines = getAffectedLines(lines, "<copyField");

    if (confLines.isEmpty())
    {
      return;
    }

    // indent(confLines, lines, 8);
    alignLinesOnWord(confLines, lines, "source=");
    alignLinesOnWord(confLines, lines, "dest=");
  }

  /**
   * Find any lines that start with the given keywords.
   *
   * @param   lines     - an array of lines to search from
   * @param   keyWords  - the list of key words
   *
   * @return  the list of matching lines
   */
  private static List<Integer> getAffectedLines(String[] lines, String... keyWords)
  {
    List<Integer> contentLines = new ArrayList<Integer>();
    int           i            = 0;

    for (String line : lines)
    {
      String[] tokens = line.trim().split(" ");

      if ((tokens.length > keyWords.length))
      {
        boolean isMatch = true;

        for (int j = 0; j < keyWords.length; j++)
        {
          isMatch = isMatch && tokens[j].startsWith(keyWords[j]);
        }

        if (isMatch)
        {
          contentLines.add(i);
        }
      }

      i++;
    }

    return contentLines;
  }

  /** Strip off any leading space and indent with the number of spaces needed. */
  private static void indent(List<Integer> confLines, String[] lines, int numberOfLeadingSpaces)
  {
    String spaces = getLeadingSpaces(numberOfLeadingSpaces);

    for (Integer confLine : confLines)
    {
      String line = lines[confLine].trim();

      lines[confLine] = spaces + line;
    }
  }

  /** go through all of the lines and make sure they line up for the given work. */
  private static void alignLinesOnWord(List<Integer> confLines, String[] lines, String alignmentWord)
  {
    // iterate through lines, get highest index.
    int maxIndex = 0;

    for (Integer confLine : confLines)
    {
      String line  = lines[confLine];
      int    index = line.indexOf(alignmentWord);

      maxIndex = Math.max(maxIndex, index);
    }

    // now go through them again and pad them out
    for (Integer confLine : confLines)
    {
      String line  = lines[confLine];
      int    index = line.indexOf(alignmentWord);

      if (index > 0)
      {
        String spaces = getLeadingSpaces(maxIndex - index);

        lines[confLine] = line.substring(0, index) + spaces + line.substring(index);
      }
    }
  }

  // <artifact name="nurflugel-resourcebundler-javadoc" type="javadoc" ext="zip" conf="javadoc"/>
  // <artifact name="nurflugel-resourcebundler-source" type="source" ext="zip" conf="source"/>
  private static void formatPublications(String[] lines)
  {
    List<Integer> confLines = getAffectedLines(lines, "<artifact", "name");

    indent(confLines, lines, 8);
    alignLinesOnWord(confLines, lines, "name=");
    alignLinesOnWord(confLines, lines, "type=");
    alignLinesOnWord(confLines, lines, "ext=");
    alignLinesOnWord(confLines, lines, "conf=");
  }

  /**
   * <dependency org="org.jdesktop" name="swingworker" rev="1.1" conf="build,dist,source,javadoc"/>.
   *
   * <p><dependency org="org.junit" name="junit" rev="4.3.1" conf="build,test"/>.</p>
   */
  private static void formatDependencyLines(String[] lines)
  {
    List<Integer> dependencyLines = getAffectedLines(lines, "<dependency", "org");

    indent(dependencyLines, lines, 8);
    alignLinesOnWord(dependencyLines, lines, "name=");
    alignLinesOnWord(dependencyLines, lines, "rev=");
    alignLinesOnWord(dependencyLines, lines, "conf=");
  }

  /**
   * <exclude org="org.springframework" name="spring-dao"/>.
   *
   * <p><exclude org="org.springframework" name="spring-hibernate2"/></p>
   *
   * <p><exclude org="org.springframework" name="spring-ibatis"/></p>
   *
   * <p><exclude org="org.springframework" name="spring-jca"/>.</p>
   */
  private static void formatExcludeLines(String[] lines)
  {
    List<Integer> dependencyLines = getAffectedLines(lines, "<exclude");

    indent(dependencyLines, lines, 12);
    alignLinesOnWord(dependencyLines, lines, "org=");
    alignLinesOnWord(dependencyLines, lines, "name=");
  }

  /** Since any global exludes will have been indented, we go through and unindent them. */
  private static void unindentExcludes(String[] lines)
  {
    boolean wasCloseDependencyLine = false;
    boolean inExcludes             = false;
    int     index                  = 0;

    for (String longLine : lines)
    {
      String  line          = longLine.trim();
      boolean isExcludeLine = (line.contains("<exclude"));

      if (!inExcludes)
      {
        if (isExcludeLine && wasCloseDependencyLine)
        {
          inExcludes = true;
        }

        wasCloseDependencyLine = (line.contains("</dependency>")) || (line.startsWith("<dependency") && line.endsWith("/>"));     // or <dependency ..... />
      }

      if (inExcludes)
      {
        if (isExcludeLine)
        {
          lines[index] = indent(line, 8);
        }
      }

      index++;
    }
  }

  /** Strip off any leading space and indent with the number of spaces needed. */
  private static String indent(String lineToIndent, int numberOfLeadingSpaces)
  {
    String spaces = getLeadingSpaces(numberOfLeadingSpaces);
    String line   = lineToIndent.trim();

    line = spaces + line;

    return line;
  }

  private static String getLeadingSpaces(int numberOfLeadingSpaces)
  {
    StringBuilder leadingSpace = new StringBuilder();

    for (int i = 0; i < numberOfLeadingSpaces; i++)
    {
      leadingSpace.append(" ");
    }

    String spaces = leadingSpace.toString();

    return spaces;
  }

  private static String pasteLinesTogether(String[] lines)
  {
    StringBuilder builder = new StringBuilder();

    for (String line : lines)
    {
      builder.append(line);
      builder.append(NEW_LINE);
    }

    return builder.toString();
  }

  // todo use file utils and writelines
  private static void writeFile(String fileName, String text) throws IOException
  {
    File       outFile = new File(fileName);
    FileWriter fw      = new FileWriter(outFile);
    char[]     buffer  = new char[text.length()];

    text.getChars(0, text.length(), buffer, 0);
    fw.write(buffer);
    fw.close();
  }
}
