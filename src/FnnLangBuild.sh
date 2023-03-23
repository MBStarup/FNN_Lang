#!/bin/sh
fnnbuild() { java -jar lib/antlr.jar -o antlrfiles -visitor -no-listener -Xexact-output-dir src/*.g4 && javac -d bin -cp lib/* antlrfiles/*.java src/*.java }
fnnrun() { java -cp "bin:lib/*" Main $1 }
fnnbar() { fnnbuild && fnnrun $1 }


echo "you can now use fnnbuild, fnnrun x & fnnbar x" 