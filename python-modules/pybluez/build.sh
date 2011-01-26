#!/bin/bash

VERSION="r1"

ndk-build
rm -rf out
cp -r python out
cp libs/armeabi/_bluetooth.so out/_bluetooth.so
pushd out
zip -r ../pybluez-${VERSION}.zip .
popd
