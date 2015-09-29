#make clean
make android
mkdir -p brexx/bin
mkdir -p brexx/lib
cp src/rexx brexx/bin
cp lstring/liblstring.so brexx/lib
cp modules/*.so brexx/lib

rm brexx.zip
zip -r brexx.zip brexx/

rm -Rf brexx/bin
cp [A-Z]* brexx/
mkdir brexx/doc
mkdir brexx/lib
cp -Rdpvu doc/*.html brexx/doc
cp -Rdpvu lib/*.r brexx/lib

rm brexx_extras.zip
zip -r brexx_extras.zip brexx

rm -R brexx_scripts.zip brexx/*
mkdir brexx/ce
mkdir brexx/android
cp progs/*.r brexx/
cp progs/ce/*.r brexx/ce
cp progs/android/*.r brexx/android
zip -r brexx_scripts.zip brexx
rm -Rf brexx
