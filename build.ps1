Write-Host "" -f blue
Write-Host "Building" -f blue
Write-Host "" -f blue

java -jar lib/antlr.jar -o antlrfiles -visitor -no-listener -Xexact-output-dir src/*.g4
javac -d bin -cp lib/* antlrfiles/*.java src/*.java

Write-Host "" -f blue
Write-Host "Building Done" -f blue
Write-Host "" -f blue