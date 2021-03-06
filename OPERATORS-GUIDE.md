----
# OPERATORS GUIDE #

----
## How to verify the ERS? ##
  Releases are signed by Jean-Luc using [GPG](https://en.wikipedia.org/wiki/GNU_Privacy_Guard). It is **highly** recommended to verify the signature every time you download new version. [There are some notes](https://bitcointalk.org/index.php?topic=345619.msg4406124#msg4406124) how to do this. [This script](https://github.com/egl-ext/egl-kit/blob/master/distrib/safe-egl-download.sh) automates this process on Linux.

----
## How to configure the ERS? ##

  - config files under `conf/`
  - options are described in config files
  - **do not edit** `conf/egl-default.properties` **nor** `conf/logging-default.properties`
  - use own config file instead: `conf/egl.properties` or `conf/logging.properties`
  - only deviations from default config

----
## How to update the ERS? ##

  - **if configured as described above**, just unpack a new version over the existing installation directory
  - next run of ERS will upgrade database if necessary
  
----

## How to manage multiple ERS-nodes? ##
  Check [Eagle-Kit's homepage](https://github.com/egl-ext/egl-kit) for more information.

----