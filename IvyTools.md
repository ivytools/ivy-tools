# Introduction #

Like many people I use Ivy for library dependency management.  However, I found no good tools out there for helping you to manage and search the repository.  So I decided to write my own, and make them open source.


# Details #

There are three tools available in this code base:
  * **IvyBrowser** - Cruise your Ivy repository to find that library version you're looking for
  * **IvyBuilder** - For the repository administrator, a tool to help add items to the repository, generate the XML file, add dependencies, etc.
  * **IvyTracker** - Find out what libraries in your repository are actually in use, based on the projects in source control.