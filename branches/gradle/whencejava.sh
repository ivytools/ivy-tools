#!/bin/bash

#OS X
#set JAVA_HOME=/usr
#solaris
JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/1.5/Home/
#set PATH=$PATH:$JAVA_HOME/bin
export PATH
export JAVA_HOME
export ANT_OPTS=-Xmx640m

set CLASSPATH=.
java -cp . WhenceJava $*
