#!/bin/sh
java -cp lib/h2*.jar org.h2.tools.Shell -url jdbc:h2:egl_db/egl -user sa -password sa
