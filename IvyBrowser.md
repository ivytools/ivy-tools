**IvyBrowser** is a tool used to cruise your web-based Ivy repository.  This tool can be used to:
  * Find a specific library, selecting by org, package,  library name, or even version number  (i.e, "apache", "commons", "commons-lang", "1.3")
  * Creates the line of XML to add to your ivy repository, specifying any excludes or force tags
    * Example: `<dependency org="com.ryangrier.ant"  name="version_tool"  rev="1.1.4"  conf="build,dist-ear,source,javadoc"/>`

**How to run**
  * First, compile the code.
    * Checkout the project,
    * Open a command line, and execute "build distribute".  That will get the libraries out of my Ivy repository and put them on your machine.
    * You can run this from Java WebStart (edit the .jnlp file to match your settings), or from your IDE.
      * The class to run is: `com.nurflugel.ivybrowser.ui.IvyBrowserMainFrame`

You will see the program's window, as shown below (shown with a sample repository):