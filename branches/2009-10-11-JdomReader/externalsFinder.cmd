
set JPTL_ROOT=d:\snapshots\java3rdpartylib\
set JAVAEXTERNALS=d:\snapshots\javaexternals\trunk

set IB_HOME=%JAVAEXTERNALS%\maintenance\IvyBrowser\webstart\lib

set JAVA_HOME=%JPTL_ROOT%\jdk\1.5.0_15

%JAVA_HOME%\bin\java -cp %IB_HOME%\swing-worker.jar;%IB_HOME%\ivyBrowser.jar;%IB_HOME%\xstream.jar;%IB_HOME%\svnkit.jar com.nurflugel.externalsreporter.ui.MainFrame

