if "%JTPL_ROOT%" == "" set JTPL_ROOT=d:\snapshots\java3rdpartylib\trunk
if not exist "%JTPL_ROOT%" set JTPL_ROOT=d:\snapshots\java3rdpartylib\
if not exist "%JTPL_ROOT%" set JTPL_ROOT=c:\snapshots\java3rdpartylib\trunk
if not exist "%JTPL_ROOT%" set JTPL_ROOT=c:\snapshots\java3rdpartylib\

set JAVAEXTERNALS=d:\snapshots\javaexternals\trunk
if not exist "%JAVAEXTERNALS%" set JAVAEXTERNALS=c:\snapshots\javaexternals\trunk
set IB_HOME=%JAVAEXTERNALS%\maintenance\IvyBrowser\webstart\lib

set JAVA_HOME=%JTPL_ROOT%\jdk\1.5.0_15

%JAVA_HOME%\bin\java -cp %IB_HOME%\swing-worker.jar;%IB_HOME%\ivyBrowser.jar;%IB_HOME%\jdom.jar;%IB_HOME%\glazedlists_jdk_1.5.jar net.nike.ivybrowser.ui.BuilderMainFrame

