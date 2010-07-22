#!/bin/bash

package=$1
package_dir=$(echo src/$package|sed 's/\./\//g')
mkdir -p $package_dir
mv src/com/dummy/fooforandroid/* $package_dir/
rmdir src/com/dummy/fooforandroid
rmdir src/com/dummy
rmdir src/com
source_files=$package_dir/*
for filename in $source_files AndroidManifest.xml build.properties;
do
	sed 's/com\.dummy\.fooforandroid/'$package'/g' $filename > tmp; mv tmp $filename;
done
