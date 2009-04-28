#!/bin/bash

#OS X
#set JAVA_HOME=/usr
#solaris
JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Home/
#set PATH=$PATH:$JAVA_HOME/bin
export PATH
export JAVA_HOME
export ANT_OPTS=-Xmx640m

#apache-ant-1.7.0/bin/ant -buildfile build/build.xml  $*
ant -buildfile build/build.xml  $* 
