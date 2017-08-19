#!/bin/bash
VERSION=$1
if [ -x ${VERSION} ];
then
	echo VERSION not defined
	exit 1
fi
PACKAGE=egl-client-${VERSION}
echo PACKAGE="${PACKAGE}"
CHANGELOG=egl-client-${VERSION}.changelog.txt
OBFUSCATE=$2

FILES="changelogs conf html lib resource contrib logs"
FILES="${FILES} egl.exe eglservice.exe"
FILES="${FILES} 3RD-PARTY-LICENSES.txt AUTHORS.txt LICENSE.txt"
FILES="${FILES} DEVELOPERS-GUIDE.md OPERATORS-GUIDE.md README.md README.txt USERS-GUIDE.md"
FILES="${FILES} mint.bat mint.sh run.bat run.sh run-tor.sh run-desktop.sh start.sh stop.sh compact.sh compact.bat sign.sh sign.bat passphraseRecovery.sh passphraseRecovery.bat"
FILES="${FILES} egl.policy egldesktop.policy EGL_Wallet.url Dockerfile"

# unix2dos *.bat
echo compile
./win-compile.sh
rm -rf html/doc/*
rm -rf egl
rm -rf ${PACKAGE}.jar
rm -rf ${PACKAGE}.exe
rm -rf ${PACKAGE}.zip
mkdir -p egl/
mkdir -p egl/logs
mkdir -p egl/addons/src

if [ "${OBFUSCATE}" == "obfuscate" ];
then
echo obfuscate
proguard.bat @egl.pro
mv ../egl.map ../egl.map.${VERSION}
mkdir -p egl/src/
else
FILES="${FILES} classes src JPL-NRS.pdf"
FILES="${FILES} compile.sh javadoc.sh jar.sh package.sh"
FILES="${FILES} win-compile.sh win-javadoc.sh win-package.sh"
echo javadoc
./win-javadoc.sh
fi
echo copy resources
cp installer/lib/JavaExe.exe egl.exe
cp installer/lib/JavaExe.exe eglservice.exe
cp -a ${FILES} egl
cp -a logs/placeholder.txt egl/logs
echo gzip
for f in `find egl/html -name *.gz`
do
	rm -f "$f"
done
for f in `find egl/html -name *.html -o -name *.js -o -name *.css -o -name *.json  -o -name *.ttf -o -name *.svg -o -name *.otf`
do
	gzip -9c "$f" > "$f".gz
done
cd egl
echo generate jar files
../jar.sh
echo package installer Jar
../installer/build-installer.sh ../${PACKAGE}
echo create installer exe
../installer/build-exe.bat ${PACKAGE}
echo create installer zip
cd -
zip -q -X -r ${PACKAGE}.zip egl -x \*/.idea/\* \*/.gitignore \*/.git/\* \*.iml egl/conf/egl.properties egl/conf/logging.properties egl/conf/localstorage/\*
rm -rf egl

echo creating change log ${CHANGELOG}
echo -e "Release $1\n" > ${CHANGELOG}
echo -e "https://www.jelurida.com/\n" >> ${CHANGELOG}
echo -e "sha256 checksums:\n" >> ${CHANGELOG}
sha256sum ${PACKAGE}.exe >> ${CHANGELOG}

sha256sum ${PACKAGE}.jar >> ${CHANGELOG}

if [ "${OBFUSCATE}" == "obfuscate" ];
then
echo -e "\n\nThis is an experimental release for testing only. Source code is not provided." >> ${CHANGELOG}
fi
echo -e "\n\nChange log:\n" >> ${CHANGELOG}

cat changelogs/${CHANGELOG} >> ${CHANGELOG}
echo >> ${CHANGELOG}
