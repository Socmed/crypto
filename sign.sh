#!/bin/sh
java -cp "classes:lib/*:conf" egl.tools.SignTransactionJSON $@
exit $?
