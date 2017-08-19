#!/bin/sh
if [ -e ~/.egl/egl.pid ]; then
    PID=`cat ~/.egl/egl.pid`
    ps -p $PID > /dev/null
    STATUS=$?
    echo "stopping"
    while [ $STATUS -eq 0 ]; do
        kill `cat ~/.egl/egl.pid` > /dev/null
        sleep 5
        ps -p $PID > /dev/null
        STATUS=$?
    done
    rm -f ~/.egl/egl.pid
    echo "Eagle server stopped"
fi

