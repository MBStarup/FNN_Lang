function rmf {
    param (
        $FolderName
    )
    if (Test-Path $FolderName) {
        Remove-Item $FolderName -Force -Recurse
    }
}

Write-Host "" -f blue
Write-Host "Building" -f blue
Write-Host "" -f blue

rmf .\antlrfiles
rmf .\bin
rmf .\a.exe
java -jar lib/antlr.jar -o antlrfiles -visitor -no-listener -Xexact-output-dir src/*.g4
javac -d bin -cp lib/* antlrfiles/*.java src/*.java
jar cmf .\MANIFEST.MF FNNC.jar lib/antlr.jar -C bin . 

Write-Host "" -f blue
Write-Host "Building Done" -f blue
Write-Host "" -f blue

exit 0