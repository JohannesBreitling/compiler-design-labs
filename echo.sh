#!/usr/bin/env sh
# Script to automatically print the exit code of an executable
executeable=$1
./$executeable
echo $?