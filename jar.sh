#!/bin/sh
java -cp classes egl.tools.ManifestGenerator
/bin/rm -f egl.jar
jar cfm egl.jar resource/egl.manifest.mf -C classes . || exit 1
/bin/rm -f eglservice.jar
jar cfm eglservice.jar resource/eglservice.manifest.mf -C classes . || exit 1

echo "jar files generated successfully"