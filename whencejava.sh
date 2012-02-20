#!/bin/bash

#OS X
#set JAVA_HOME=/usr
#solaris
JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Home/
#set PATH=$PATH:$JAVA_HOME/bin
export PATH
export JAVA_HOME
export ANT_OPTS=-Xmx640m

set CLASSPATH=.
#java -cp . WhenceJava $*
java -cp webstart/lib/buildtasks-1.0-SNAPSHOT.jar:/nikeDev/java3rdpartylib/ant/1.8.0/lib/ant.jar com.nurflugel.buildtasks.WhenceJava $*
