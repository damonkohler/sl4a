#!/bin/bash

VERSION=2.6.2
export NDK_MODULE_PATH=$(pwd)
set -ex
CWD=$(pwd)
DEBUG=no

RELEASE_VERSION=r11
rm -rf python*${RELEASE_VERSION}.zip

if [ ! -f ${CWD}/Python-${VERSION}.tar.bz2 ]; then
    URL="http://www.python.org/ftp/python/${VERSION}/Python-${VERSION}.tar.bz2"
    wget -O ${CWD}/Python-${VERSION}.tar.bz2  $URL
fi

if [ ! -d host ]; then
    # we need to build the host python and host pgen so we can
    # generate the correct grammar and some other stuff
    mkdir -p ${CWD}/host
    tar -xvjf ${CWD}/Python-${VERSION}.tar.bz2
    pushd Python-${VERSION}
    ./configure --prefix=${CWD}/host/
    make
    make install
    cp Parser/pgen ${CWD}/host/
    popd
    rm -rf Python-${VERSION}
fi

PYTHONSRC=${CWD}/python-src

if [ ! -d ${PYTHONSRC} ]; then
    tar -xvjf ${CWD}/Python-${VERSION}.tar.bz2
    mv Python-${VERSION} ${PYTHONSRC}
    pushd ${PYTHONSRC}
    patch -p1 < ${CWD}/Python-${VERSION}-android.patch
    popd
fi

rm -rf output*
mkdir -p output
OUT=${CWD}/output

mkdir -p ${OUT}/usr/bin
mkdir -p ${OUT}/usr/lib/python2.6
mkdir -p ${OUT}/usr/lib/python2.6/lib-dynload
mkdir -p ${OUT}/usr/include/python2.6

LIBS='site-packages encodings compiler hotshot
    email email/mime
    json
    sqlite3
    logging bsddb csv wsgiref
    ctypes ctypes/macholib idlelib idlelib/Icons
    distutils distutils/command
    multiprocessing multiprocessing/dummy
    lib-old
    plat-linux2
    xml xml/dom xml/etree xml/parsers xml/sax'
    
for lib in $LIBS; do
    if [ -n "$(find ${PYTHONSRC}/Lib/${lib} -maxdepth 1 -type f)" ]; then
	mkdir -p ${OUT}/usr/lib/python2.6/${lib}
	cp $(find ${PYTHONSRC}/Lib/${lib} -maxdepth 1 -type f) ${OUT}/usr/lib/python2.6/${lib}
    fi
done

cp $(find ${PYTHONSRC}/Lib/ -maxdepth 1 -type f) ${OUT}/usr/lib/python2.6/
cp -r ${PYTHONSRC}/Include/* ${OUT}/usr/include/python2.6/
cp ${PYTHONSRC}/pyconfig.h ${OUT}/usr/include/python2.6/

# build the android needed libraries
pushd ${CWD}/python
${CWD}/host/pgen ${CWD}/python-src/Grammar/Grammar \
	${CWD}/python-src/Include/graminit.h \
	${CWD}/python-src/Python/graminit.c
ndk-build
# copy out all the needed files
mv obj/local/armeabi/python	${OUT}/usr/bin
mv obj/local/armeabi/lib*.so	${OUT}/usr/lib
mv obj/local/armeabi/*.so	${OUT}/usr/lib/python2.6/lib-dynload
popd

${CWD}/host/bin/python ${OUT}/usr/lib/python2.6/compileall.py ${OUT}/usr/lib/python2.6
${CWD}/host/bin/python build.py ${RELEASE_VERSION}

if [ "$DEBUG" != "yes" ]; then
    rm -rf output*
fi
echo "Done"
