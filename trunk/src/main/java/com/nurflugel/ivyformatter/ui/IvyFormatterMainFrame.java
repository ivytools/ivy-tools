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
}
