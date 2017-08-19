#!/bin/sh
CP="conf/;classes/;lib/*;testlib/*"
SP="src/java/;test/java/"
TESTS="egl.crypto.Curve25519Test egl.crypto.ReedSolomonTest"

/bin/rm -f egl.jar
/bin/rm -rf classes
/bin/mkdir -p classes/

javac -encoding utf8 -sourcepath $SP -classpath $CP -d classes/ src/java/egl/*.java src/java/egl/*/*.java test/java/egl/*/*.java || exit 1

java -classpath $CP org.junit.runner.JUnitCore $TESTS

