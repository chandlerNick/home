#! /bin/bash

# usage:
# ./textnormsh.sh inputFileNameGoesHere > outputFileNameGoesHere

cat $1  | sed 's/(APPLAUSE)//g' | sed 's/--/ -- /g' | sed 's/\s\s*/ /g' | sed 's/[\.\,\;\:\?“"]//g' | sed 's/’’//g' | sed "s/''//g" | sed 's/(/( /g' | sed 's/)/ )/g' | grep -v "^\s*[0-9]*\s*$"  | grep -v "^<<"  | tr 'A-Z' 'a-z' | sed 's/^ */<s> /g'  | sed 's/ *$/ <\/s>/g' | sed 's/``//g' | sed 's/  */ /g'
