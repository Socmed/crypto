#!/bin/sh
CP=conf/:classes/:lib/*:testlib/*
SP=src/java/:test/java/

if [ $# -eq 0 ]; then
TESTS="egl.crypto.Curve25519Test egl.crypto.ReedSolomonTest egl.peer.HallmarkTest egl.TokenTest egl.FakeForgingTest
egl.FastForgingTest egl.ManualForgingTest"
else
TESTS=$@
fi

/bin/rm -f egl.jar
/bin/rm -rf classes
/bin/mkdir -p classes/

javac -encoding utf8 -sourcepath ${SP} -classpath ${CP} -d classes/ src/java/egl/*.java src/java/egl/*/*.java test/java/egl/*.java test/java/egl/*/*.java || exit 1

for TEST in ${TESTS} ; do
java -classpath ${CP} org.junit.runner.JUnitCore ${TEST} ;
done



