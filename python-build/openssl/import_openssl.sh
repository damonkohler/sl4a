#!/bin/bash
#
# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#
# This script imports new versions of OpenSSL (http://openssl.org/source) into the
# Android source tree.  To run, (1) fetch the appropriate tarball from the OpenSSL repository,
# (2) check the gpg/pgp signature, and then (3) run:
#   ./import_openssl.sh openssl-*.tar.gz
#
# IMPORTANT: See README.android for additional details.

# turn on exit on error as well as a warning when it happens
set -e
trap  "echo WARNING: Exiting on non-zero subprocess exit code" ERR;

function die() {
  declare -r message=$1

  echo $message
  exit 1
}

function usage() {
  declare -r message=$1

  if [ ! "$message" = "" ]; then
    echo $message
  fi 
  echo "Usage:"
  echo "  ./import_openssl.sh import /path/to/openssl-*.tar.gz"
  echo "  ./import_openssl.sh regenerate patch/*.patch"
  exit 1
}

function main() {
  if [ ! -f openssl.config ]; then
    die "openssl.config not found"
  fi
  
  if [ ! -f openssl.version ]; then
    die "openssl.version not found"
  fi
  
  if [ ! -d patches ]; then
    die "OpenSSL patch directory patches/ not found"
  fi

  source openssl.config
  source openssl.version

  if [ "$CONFIGURE_ARGS" == "" ]; then
    die "Invalid openssl.config; see README.android for more information"
  fi

  declare -r command=$1
  shift || usage "No command specified. Try import or regenerate."
  if [ "$command" = "import" ]; then
    declare -r tar=$1
    shift || usage "No tar file specified."
    import $tar
  elif [ "$command" = "regenerate" ]; then
    declare -r patch=$1
    shift || usage "No patch file specified."
    regenerate $patch
  else   
    usage "Unknown command specified $command. Try import or regenerate."
  fi
}

function import() {
  declare -r OPENSSL_SOURCE=$1

  declare -r NEW_OPENSSL_VERSION=`expr match "$OPENSSL_SOURCE" '.*-\(.*\).tar.gz' || true`
  if [ "$NEW_OPENSSL_VERSION" == "" ]; then
    die "Invalid openssl source filename: $OPENSSL_SOURCE"
  fi

  # Remove old source
  if [ "$OPENSSL_VERSION" == "" ]; then
    die "OPENSSL_VERSION not declared in openssl.version"
  else
    rm -rf openssl-$OPENSSL_VERSION.orig/
    rm -rf openssl-$OPENSSL_VERSION/
  fi

  # Process new source
  OPENSSL_VERSION=$NEW_OPENSSL_VERSION
  rm -rf openssl-$OPENSSL_VERSION/     # remove stale files
  tar -zxf $OPENSSL_SOURCE
  mv openssl-$OPENSSL_VERSION openssl-$OPENSSL_VERSION.orig
  find openssl-$OPENSSL_VERSION.orig -type f -print0 | xargs -0 chmod a-w
  tar -zxf $OPENSSL_SOURCE
  cd openssl-$OPENSSL_VERSION

  # Apply appropriate patches
  for i in $OPENSSL_PATCHES; do
    echo "Applying patch $i"
    patch -p1 < ../patches/$i || die "Could not apply patches/$i. Fix source and run: $0 regenerate patches/$i"
  done

  # Cleanup patch output
  find . -type f -name "*.orig" -print0 | xargs -0 rm -f

  # Configure source (and print Makefile defines for review, see README.android)
  ./Configure $CONFIGURE_ARGS
  echo 
  echo BEGIN Makefile defines to compare with android-config.mk
  echo 
  grep -e -D Makefile | grep -v CONFIGURE_ARGS= | grep -v OPTIONS= | grep -v -e -DOPENSSL_NO_DEPRECATED
  echo 
  echo END Makefile defines to compare with android-config.mk
  echo 

  # TODO(): Fixup android-config.mk

  cp -f LICENSE ../NOTICE
  touch ../MODULE_LICENSE_BSD_LIKE

  # Avoid checking in symlinks
  for i in `find include/openssl -type l`; do
    target=`readlink $i`
    rm -f $i
    if [ -f include/openssl/$target ]; then
      cp include/openssl/$target $i
    fi
  done

  # Copy Makefiles
  cp ../patches/apps_Android.mk apps/Android.mk
  cp ../patches/crypto_Android.mk crypto/Android.mk
  cp ../patches/ssl_Android.mk ssl/Android.mk

  # Setup android.testssl directory
  mkdir android.testssl
  cat test/testssl | \
    sed 's#../util/shlib_wrap.sh ./ssltest#adb shell /system/bin/ssltest#' | \
    sed 's#../util/shlib_wrap.sh ../apps/openssl#adb shell /system/bin/openssl#' | \
    sed 's#adb shell /system/bin/openssl no-dh#[ `adb shell /system/bin/openssl no-dh` = no-dh ]#' | \
    sed 's#adb shell /system/bin/openssl no-rsa#[ `adb shell /system/bin/openssl no-rsa` = no-dh ]#' | \
    sed 's#../apps/server2.pem#/sdcard/android.testssl/server2.pem#' | \
    cat > \
    android.testssl/testssl
  chmod +x android.testssl/testssl
  cat test/Uss.cnf | sed 's#./.rnd#/sdcard/android.testssl/.rnd#' >> android.testssl/Uss.cnf
  cat test/CAss.cnf | sed 's#./.rnd#/sdcard/android.testssl/.rnd#' >> android.testssl/CAss.cnf
  cp apps/server2.pem android.testssl/
  cp ../patches/testssl.sh android.testssl/

  # Prune unnecessary sources
  rm -rf $UNNEEDED_SOURCES

  cd ..
  cp -af openssl-$OPENSSL_VERSION/include .
  rm -rf apps/
  mv openssl-$OPENSSL_VERSION/apps .
  rm -rf ssl/
  mv openssl-$OPENSSL_VERSION/ssl .
  rm -rf crypto/
  mv openssl-$OPENSSL_VERSION/crypto .
  rm -rf android.testssl/
  mv openssl-$OPENSSL_VERSION/android.testssl .
  rm -f e_os.h e_os2.h
  mv openssl-$OPENSSL_VERSION/e_os.h openssl-$OPENSSL_VERSION/e_os2.h .
  rm -rf openssl-$OPENSSL_VERSION.orig/
  rm -rf openssl-$OPENSSL_VERSION/
}

function regenerate() {
  declare -r patch=$1
  
  declare -r variable_name=OPENSSL_PATCHES_`basename $patch .patch | sed s/-/_/`_SOURCES
  # http://tldp.org/LDP/abs/html/ivr.html
  eval declare -r sources=\$$variable_name
  rm -f $patch
  touch $patch  
  for i in $sources; do
    diff -uap openssl-$OPENSSL_VERSION.orig/$i openssl-$OPENSSL_VERSION/$i >> $patch && die "ERROR: No diff for patch $path in file $i"
  done
}

main $@
