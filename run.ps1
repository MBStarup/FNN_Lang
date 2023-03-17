Write-Host "" -f blue
Write-Host "Running" -f blue
Write-Host @args -f red
Write-Host "" -f blue

java -cp "bin;lib/*" Main @args
Write-Host "" -f blue
Write-Host "Running Done" -f blue
Write-Host "" -f blue