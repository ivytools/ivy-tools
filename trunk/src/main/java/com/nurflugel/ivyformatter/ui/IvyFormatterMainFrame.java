package com.nurflugel.ivyformatter.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import static com.nurflugel.common.ui.Util.*;
import static com.nurflugel.common.ui.Version.VERSION;
import static java.awt.datatransfer.DataFlavor.stringFlavor;

/**  */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class IvyFormatterMainFrame extends JFrame
{
  /** Use serialVersionUID for interoperability. */
  private static final long serialVersionUID = -6797243387476820162L;
  private JButton           formatTextButton;
  private JButton           quitButton;
  private JTextArea         textArea;
  private JPanel            contentPane;
  private JButton           helpButton;

  public IvyFormatterMainFrame()
  {
    // $$$setupUI$$$();
    setContentPane(contentPane);
    setTitle("Ivy Formatter v. " + VERSION);
    setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", this);
    addListeners();
    pack();
    setSize(1000, 1000);
    centerApp(this);
    setVisible(true);
  }

  private void addListeners()
  {
    formatTextButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent event)
        {
          formatText(textArea.getText());
        }
      });
    quitButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent event)
        {
          System.exit(0);
        }
      });
    addWindowListener(new WindowAdapter()
      {
        @Override
        public void windowClosing(WindowEvent e)
        {
          super.windowClosing(e);
          System.exit(0);
        }
      });
    addHelpListener("ivyFormatterHelp.hs", helpButton, this);
  }

  private void formatText(String ivyText)
  {
    String text = ivyText;

    if (text.trim().isEmpty())
    {
      Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

      try
      {
        if ((t != null) && t.isDataFlavorSupported(stringFlavor))
        {
          text = (String) t.getTransferData(stringFlavor);
        }
      }
      catch (Exception e)
      {
        System.out.println("e = " + e);
      }
    }

    text = IvyFormatterProcess.formatIvyFileText(text);
    putLinesIntoBuffer(text);
    textArea.setText(text);
    JOptionPane.showMessageDialog(this, "Formatted text has been pasted into your buffer");
  }

  private void putLinesIntoBuffer(String text)
  {
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
  }

  public static void main(String[] args)
  {
    IvyFormatterMainFrame mainFrame = new IvyFormatterMainFrame();
  }

  {
    // GUI initializer generated by IntelliJ IDEA GUI Designer
    // >>> IMPORTANT!! <<<
    // DO NOT EDIT OR ADD ANY CODE HERE!
    $$$setupUI$$$();
  }

  /**
   * Method generated by IntelliJ IDEA GUI Designer >>> IMPORTANT!! <<< DO NOT edit this method OR call it in your code!
   *
   * @noinspection  ALL
   */
  private void $$$setupUI$$$()
  {
    contentPane = new JPanel();
    contentPane.setLayout(new GridBagLayout());

    final JScrollPane  scrollPane1 = new JScrollPane();
    GridBagConstraints gbc;

    gbc           = new GridBagConstraints();
    gbc.gridx     = 0;
    gbc.gridy     = 0;
    gbc.gridwidth = 4;
    gbc.weightx   = 1.0;
    gbc.weighty   = 1.0;
    gbc.fill      = GridBagConstraints.BOTH;
    contentPane.add(scrollPane1, gbc);
    textArea = new JTextArea();
    textArea.setFont(new Font("Courier New", Font.BOLD, textArea.getFont().getSize()));
    textArea.setToolTipText("Anything in this box or  the paste buffer will be used as inputy");
    scrollPane1.setViewportView(textArea);

    final JPanel panel1 = new JPanel();

    panel1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    gbc       = new GridBagConstraints();
    gbc.gridx = 3;
    gbc.gridy = 1;
    gbc.fill  = GridBagConstraints.BOTH;
    contentPane.add(panel1, gbc);
    helpButton = new JButton();
    helpButton.setText("Help");
    panel1.add(helpButton);
    formatTextButton = new JButton();
    formatTextButton.setText("Format Ivy Text");
    formatTextButton.setToolTipText("Paste text into the box or just click this button");
    panel1.add(formatTextButton);
    quitButton = new JButton();
    quitButton.setText("Quit");
    panel1.add(quitButton);
  }

  /** @noinspection  ALL */
  public JComponent $$$getRootComponent$$$()
  {
    return contentPane;
  }
}
