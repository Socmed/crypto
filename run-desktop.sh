#!/bin/sh
if [ -x jre/bin/java ]; then
    JAVA=./jre/bin/java
else
    JAVA=java
fi
${JAVA} -cp classes:lib/*:conf:addons/classes:addons/lib/* -Degl.runtime.mode=desktop -Degl.runtime.dirProvider=egl.env.DefaultDirProvider egl.Eagle
