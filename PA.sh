#!/bin/bash
if [[ $# -ge 1 ]]; then
    PA=$2
    dir="PA$PA*"
    ( cd $dir
    sources="../../engr-cse-compiler-design-f23/PA$PA/*.txt"
    inputFiles="../../engr-cse-compiler-design-f23/PA$PA/*.in"
    for test in $sources; do
        testname=${test##*/}
        testname=${testname%.*}
        echo "testname = $testname"
        inputFile="dummy"
        for input in $inputFiles; do
            input=${input##*/}
            input=${input%.*}
            echo "input = $input"
            if [[ $input == $testname ]]; then
                inputFile=$input
                break
            fi
        done
        echo "inputFile = $inputFile"
        while getopts ":i:ac" option; do
            case $option in
                i)
                    make run TEST=$testname INPUT=$inputFile > ./output/$testname.out;;
                a)
                    make ast TEST=$testname > ./output/$testname.out;;
                c)
                    make clean
                    exit;;
                *)
                    make run TEST=$testname > ./output/$testname.out;;
            esac
        done
    done )
fi