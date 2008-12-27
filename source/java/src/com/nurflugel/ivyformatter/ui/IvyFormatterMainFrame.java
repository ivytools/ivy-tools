package com.nurflugel.ivyformatter.ui;


import com.nurflugel.common.ui.Version;
import com.nurflugel.ivytracker.MainFrame;
import com.nurflugel.ivybrowser.ui.BuilderMainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

import org.w3c.tidy.Tidy;


/**
 * Created by IntelliJ IDEA. User: douglasbullard Date: Jul 28, 2008 Time: 5:57:02 PM To change this template use File | Settings | File
 * Templates.
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public class IvyFormatterMainFrame extends JFrame {

    /**
     * Use serialVersionUID for interoperability.
     */
    private static final long serialVersionUID = -6797243387476820162L;
    private JButton formatTextButton;
    private JButton quitButton;
    private JTextArea textArea;
    private JPanel contentPane = new JPanel();
    private static final String NEW_LINE = "\n";

    public IvyFormatterMainFrame() {
        $$$setupUI$$$();
        setContentPane(contentPane);
        setTitle("Ivy Beautifier v. " + Version.VERSION);
        MainFrame.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", this);

        formatTextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                formatText();
            }
        });
        quitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.exit(0);
            }
        });
        pack();
        setSize(1000, 1000);
        BuilderMainFrame.centerApp(this);
        setVisible(true);
    }

    private void formatText() {
        String text = textArea.getText();

        if (text.trim().length() == 0) {
            Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

            try {
                if ((t != null) && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    text = (String) t.getTransferData(DataFlavor.stringFlavor);
                }
            }
            catch (Exception e) {
                System.out.println("e = " + e);
            }

        }

        text = text.replaceAll("\n\n", "~~");

        Tidy tidy = new Tidy(); // obtain a new Tidy instance
        tidy.setTabsize(4);
        tidy.setIndentContent(true);
//        tidy.setXHTML(true); // set desired config options using tidy setters
        tidy.setDocType("XML");
//        tidy.setMakeClean(false);
        tidy.setWraplen(300);
        tidy.setXmlOut(true);
        tidy.setSmartIndent(true);
//        tidy.set
        tidy.setXmlTags(true);
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        tidy.parse(inputStream, outputStream); // run tidy, providing an input and output stream
        text = outputStream.toString();

//        text = text.replaceAll("\r\n", "\n");
        text = text.replaceAll("~~", "\n\n");
        text = text.replaceAll("\r\n", "\n");
        text = text.replaceAll("\n\n", "\n");
        text = text.replaceAll("\n\n", "\n");

        String[] lines = text.split(NEW_LINE);
        formatConfLines(lines);
        formatPublications(lines);
        formatDependencyLines(lines);
        formatExcludeLines(lines);
        text = pasteLinesTogether(lines);
        putLinesIntoBuffer(text);
        textArea.setText(text);
        JOptionPane.showMessageDialog(this, "Formatted text has been pasted into your buffer");
    }

    // <artifact name="nurflugel-resourcebundler-javadoc" type="javadoc" ext="zip" conf="javadoc"/>
    // <artifact name="nurflugel-resourcebundler-source" type="source" ext="zip" conf="source"/>
    private void formatPublications(String[] lines) {
        List<Integer> confLines = getAffectedLines(lines, new String[]{"<artifact", "name"});

        indent(confLines, lines, 8);
        alignLinesOnWord(confLines, lines, "name=");
        alignLinesOnWord(confLines, lines, "type=");
        alignLinesOnWord(confLines, lines, "ext=");
        alignLinesOnWord(confLines, lines, "conf=");

    }

    /**
     * <exclude org="org.springframework" name="spring-dao"/>. <p/> <p><exclude org="org.springframework" name="spring-hibernate2"/></p>
     * <p/> <p><exclude org="org.springframework" name="spring-ibatis"/></p> <p/> <p><exclude org="org.springframework"
     * name="spring-jca"/>.</p>
     */
    private void formatExcludeLines(String[] lines) {
        List<Integer> dependencyLines = getAffectedLines(lines, new String[]{"<exclude"});

        indent(dependencyLines, lines, 12);
        alignLinesOnWord(dependencyLines, lines, "org=");
        alignLinesOnWord(dependencyLines, lines, "name=");
    }

    private List<Integer> getAffectedLines(String[] lines, String[] keyWords) {
        List<Integer> contentLines = new ArrayList<Integer>();
        int i = 0;

        for (String line : lines) {
            String[] tokens = line.trim().split(" ");

            if ((tokens.length > keyWords.length)) {
                boolean isMatch = true;

                for (int j = 0; j < keyWords.length; j++) {
                    isMatch = isMatch && tokens[j].startsWith(keyWords[j]);
                }

                if (isMatch) {
                    contentLines.add(i);
                }
            }

            i++;
        }

        return contentLines;
    }


    /**
     * <dependency org="org.jdesktop" name="swingworker" rev="1.1" conf="build,dist,source,javadoc"/>. <p/> <p><dependency org="org.junit"
     * name="junit" rev="4.3.1" conf="build,test"/>.</p>
     */
    private void formatDependencyLines(String[] lines) {
        List<Integer> dependencyLines = getAffectedLines(lines, new String[]{"<dependency", "org"});

        indent(dependencyLines, lines, 8);
        alignLinesOnWord(dependencyLines, lines, "name=");
        alignLinesOnWord(dependencyLines, lines, "rev=");
        alignLinesOnWord(dependencyLines, lines, "conf=");
    }

    /**
     * Align everything line. <p/> <p><conf name="build" visibility="public" description="Dependencies only used during the build
     * process."/></p> <p/> <p><conf name="dist" visibility="public" description="Dependencies that will be deployed via WebStart."/>.</p>
     */
    private void formatConfLines(String[] lines) {
        List<Integer> confLines = getAffectedLines(lines, new String[]{"<conf", "name"});

        indent(confLines, lines, 8);
        alignLinesOnWord(confLines, lines, "visibility=");
        alignLinesOnWord(confLines, lines, "description=");
    }


    private void putLinesIntoBuffer(String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
    }

    private String pasteLinesTogether(String[] lines) {
        StringBuilder builder = new StringBuilder();

        for (String line : lines) {
            builder.append(line);
            builder.append(NEW_LINE);
        }

        return builder.toString();
    }

    /**
     * Strip off any leading space and indent with the number of spaces needed.
     */
    private void indent(List<Integer> confLines, String[] lines, int numberOfLeadingSpaces) {
        String spaces = getLeadingSpaces(numberOfLeadingSpaces);

        for (Integer confLine : confLines) {
            String line = lines[confLine].trim();
            lines[confLine] = spaces + line;
        }
    }

    private String getLeadingSpaces(int numberOfLeadingSpaces) {
        StringBuilder leadingSpace = new StringBuilder();

        for (int i = 0; i < numberOfLeadingSpaces; i++) {
            leadingSpace.append(" ");
        }

        String spaces = leadingSpace.toString();

        return spaces;
    }

    /**
     * go through all of the lines and make sure they line up for the given work.
     */
    private void alignLinesOnWord(List<Integer> confLines, String[] lines, String alignmentWord) {

        // iterate through lines, get highest index.
        int maxIndex = 0;

        for (Integer confLine : confLines) {
            String line = lines[confLine];
            int index = line.indexOf(alignmentWord);
            maxIndex = Math.max(maxIndex, index);
        }

        // now go through them again and pad them out
        for (Integer confLine : confLines) {
            String line = lines[confLine];
            int index = line.indexOf(alignmentWord);

            if (index > 0) {
                String spaces = getLeadingSpaces(maxIndex - index);
                lines[confLine] = line.substring(0, index) + spaces + line.substring(index);
            }
        }
    }

    public static void main(String[] args) {
        IvyFormatterMainFrame mainFrame = new IvyFormatterMainFrame();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        contentPane.setLayout(new GridBagLayout());
        final JScrollPane scrollPane1 = new JScrollPane();
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(scrollPane1, gbc);
        textArea = new JTextArea();
        textArea.setFont(new Font("Courier New", Font.BOLD, textArea.getFont().getSize()));
        textArea.setToolTipText("Anything in this box or  the paste buffer will be used as inputy");
        scrollPane1.setViewportView(textArea);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(panel1, gbc);
        formatTextButton = new JButton();
        formatTextButton.setText("Format Ivy Text");
        formatTextButton.setToolTipText("Paste text into the box or just click this button");
        panel1.add(formatTextButton);
        quitButton = new JButton();
        quitButton.setText("Quit");
        panel1.add(quitButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
