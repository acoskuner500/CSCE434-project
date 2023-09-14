Param(
    [Parameter(Mandatory)]
    $DIR,
    [Parameter(Mandatory)]
    $PA
)
# $projectname = (Get-ChildItem $DIR\code\PA$PA* -Name)

$java = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.8.7-hotspot\bin\java.exe'
$argfile = '@C:\Users\acosk\AppData\Local\Temp\cp_bv1jy5sjwrjt3mu83t88k2jxg.argfile'
$tester = 'coco.InterpreterTester'

$tests = (Get-Item $DIR\testcases\PA$PA\*.txt)
$fileInputs = (Get-Item $DIR\testcases\PA$PA\*.in)

foreach ($test in $tests) {
    $filename = $test.Name
    $testname = $filename.Substring(0, $filename.Length - 4)
    $fileInput = $fileInputs -match $testname
    if (!$fileInput) {
        $fileInput = Get-ChildItem "$DIR\testcases\PA$PA\dummy.in"
    }
    & $java $argfile $tester -i $fileInput -s $test | Out-File -FilePath "$DIR\output\PA$PA\$testname.out"
}
Select-String -Path "$DIR\output\PA$PA\*.out" -Pattern "ERROR" > $DIR\output\PA$PA\errors.txt