#!/bin/sh
VERSION=$1
if [ -x ${VERSION} ];
then
	echo VERSION not defined
	exit 1
fi
PACKAGE=egl-client-${VERSION}.zip
echo PACKAGE="${PACKAGE}"

FILES="changelogs classes conf html lib src resource addons"
FILES="${FILES} egl.jar eglservice.jar"
FILES="${FILES} 3RD-PARTY-LICENSES.txt AUTHORS.txt LICENSE.txt JPL-NRS.pdf"
FILES="${FILES} DEVELOPERS-GUIDE.md OPERATORS-GUIDE.md README.md README.txt USERS-GUIDE.md"
FILES="${FILES} mint.bat mint.sh run.bat run.sh run-tor.sh run-desktop.sh start.sh stop.sh compact.sh compact.bat sign.sh sign.bat passphraseRecovery.sh passphraseRecovery.bat"
FILES="${FILES} egl.policy egldesktop.policy EGL_Wallet.url"
FILES="${FILES} compile.sh javadoc.sh jar.sh package.sh"
FILES="${FILES} win-compile.sh win-javadoc.sh win-package.sh"

echo compile
./compile.sh
echo jar
./jar.sh
echo javadoc
rm -rf html/doc/*
./javadoc.sh

rm -rf egl
rm -rf ${PACKAGE}
mkdir -p egl/
mkdir -p egl/logs
echo copy resources
cp -a ${FILES} egl
echo gzip
for f in `find egl/html -name *.gz`
do
	rm -f "$f"
done
for f in `find egl/html -name *.html -o -name *.js -o -name *.css -o -name *.json -o -name *.ttf -o -name *.svg -o -name *.otf`
do
	gzip -9c "$f" > "$f".gz
done
echo zip
zip -q -X -r ${PACKAGE} egl -x \*/.idea/\* \*/.gitignore \*/.git/\* \*/\*.log \*.iml egl/conf/egl.properties egl/conf/logging.properties egl/conf/localstorage/\*
rm -rf egl
echo done
