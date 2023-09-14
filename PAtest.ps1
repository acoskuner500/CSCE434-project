Param(
    [Parameter(Mandatory)]
    $DIR,
    [Parameter(Mandatory)]
    $PA
)
$projectname = (Get-ChildItem $DIR\code\PA$PA* -Name)
$tester = 'coco.' + $projectname.Substring(4, $projectname.Length - 4) + 'Tester'
$java = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.8.7-hotspot\bin\java.exe'
Write-Host $tester
# $tests = (Get-Item $DIR\testcases\PA$PA\*.txt)

foreach ($test in $tests) {
    $filename = (Get-ChildItem $test -Name)
    $testname = $filename.Substring(0, $filename.Length - 4)
    & $java '@C:\Users\acosk\AppData\Local\Temp\cp_9teqvttycdu5qbm50i8pr2uhu.argfile' $tester -s $test > "$DIR\output\PA$PA\$testname.out"
}
Select-String -Path "$DIR\output\PA$PA\*.out" -Pattern "ERROR" > $DIR\output\PA$PA\errors.txt