# Param(
#     [Parameter(Mandatory)]
#     $test
# )
$tests = (Get-ChildItem .\CSCE434-project\testcases\*.txt -Name)

foreach ($test in $tests) {
    # Write-Host $test
    $out = $test.Substring($test.length-11,$test.length-4)
    & 'C:\Program Files\Eclipse Adoptium\jdk-17.0.8.7-hotspot\bin\java.exe' '@C:\Users\acosk\AppData\Local\Temp\cp_dlbnw73ff7s2bc9yrj30ds1h3.argfile' 'coco.ScannerTester' -s .\CSCE434-project\testcases\$test > ".\CSCE434-project\output\$out.out"
}