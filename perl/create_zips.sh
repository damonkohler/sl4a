#!/bin/bash

# create the perl zip files for shipping

echo "-> Deleting existing zip files";
rm -f perl.zip perl_extras.zip perl_scripts.zip

echo "-> Zipping interpreter (perl.zip)"
cd interpreter
zip -r -q perl.zip perl
mv perl.zip ../
cd ../

echo "-> Zipping extra modules (perl_extras.zip)"
cd extras
zip -r -q perl_extras.zip perl
mv perl_extras.zip ..
cd ..

echo "-> Zipping scripts (perl_scripts.zip)"
cd scripts
zip -r -q perl_scripts.zip *
mv perl_scripts.zip ..
cd ..
