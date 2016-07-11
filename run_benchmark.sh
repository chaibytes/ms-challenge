#!/bin/bash

#echo > result.log
#for i in $(seq 1 99999); do 
#    java MinesConsole --gridSize 10 --mines 10 --noconsole | grep "Result" >> result.log
# done

wins=`cat result.log | grep WON | wc -l`
total=`cat result.log | wc -l`
echo Wins = $wins out of $total 
echo Average time in milliseconds when winning: `cat result.log | grep WON | cut -d '=' -f 3 | awk 'BEGIN{sum=0}{sum += $1}END{print sum/NR}'`
echo Average time in milliseconds when losing: `cat result.log | grep LOST | cut -d '=' -f 3 | awk 'BEGIN{sum=0}{sum += $1}END{print sum/NR}'`
