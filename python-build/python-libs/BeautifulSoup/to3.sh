#!/bin/sh
mkdir python3
for i in BeautifulSoup*.py
do
    cp $i python3/
    2to3-3.0 -x next $i | patch -p0 python3/$i
    cp python3/$i python3/$i.orig
    patch -p0 python3/$i < $i.3.diff
done