Param(
    [Parameter(Mandatory)]
    $PA
)

$dir = 'C:\Users\acosk\Documents\GitHub\CSCE434\CSCE434-project'
$java = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.8.7-hotspot\bin\java.exe'
$argfile = '@C:\Users\acosk\AppData\Local\Temp\cp_7q766cslebnboxfwgs9v43we0.argfile'
$tester = 'coco.InterpreterTester'

$tests = (Get-Item $dir\testcases\PA$PA\*.txt)
$fileInputs = (Get-Item $dir\testcases\PA$PA\*.in)

Set-Location $dir
foreach ($test in $tests) {
    $filename = $test.Name
    $testname = $filename.Substring(0, $filename.Length - 4)
    $fileInput = $fileInputs | Select-String -Pattern $testname
    # $fileInput = $fileInputs -match $testname
    if (!$fileInput) {
        $fileInput = Get-ChildItem "$dir\testcases\PA$PA\dummy.in"
    }
    & $java $argfile $tester -i $fileInput -s $test | Out-File -FilePath "$dir\output\PA$PA\$testname.out"
}
Select-String -Path "$dir\output\PA$PA\*.out" -Pattern "ERROR" > $dir\output\PA$PA\errors.txt