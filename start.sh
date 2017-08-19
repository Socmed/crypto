#!/bin/sh
if [ -e ~/.egl/egl.pid ]; then
    PID=`cat ~/.egl/egl.pid`
    ps -p $PID > /dev/null
    STATUS=$?
    if [ $STATUS -eq 0 ]; then
        echo "Eagle server already running"
        exit 1
    fi
fi
mkdir -p ~/.egl/
DIR=`dirname "$0"`
cd "${DIR}"
if [ -x jre/bin/java ]; then
    JAVA=./jre/bin/java
else
    JAVA=java
fi
nohup ${JAVA} -cp classes:lib/*:conf:addons/classes:addons/lib/* -Degl.runtime.mode=desktop egl.Eagle > /dev/null 2>&1 &
echo $! > ~/.egl/egl.pid
cd - > /dev/null
