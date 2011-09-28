#!/bin/bash


# Computes the 95% confidence interval of an experiment with boolean outcomes,
# e.g. heads or tails coin throws. It uses the very efficient, but still accurate
# Wald method.
# @param success Number of successes, e.g. number of times the coin shows head.
# @param total Total number of tries, e.g. total number of times the coin was thrown.
# @return A two element double array, where element 0 is the lower border and element
# 1 the upper border of the 95% confidence interval.
total=$1
success=$2

if [ "$success" -gt "$total" -o "$total" -lt 1 ]; then
	echo "Invalid Argument: 95% confidence interval for " + success + " out of " + total + " trials cannot be estimated."
	exit -1
fi

#echo "($success + 2) / ($total + 4)"

p1=`echo "($success + 2) / ($total + 4)" | bc -l`
p2=`echo "1.96 * sqrt($p1 * (1 - $p1) / ($total + 4))" | bc -l`

low=`echo "a = $p1 - $p2; if(a < 0) 0 else a;" | bc -l`
high=`echo "a= $p1 + $p2; if(a > 1) 1 else a;" | bc -l`

echo -e "$low\t$high"

#echo "here $low --- $high"
#	result["low"]="$low"
#        result["high"]="$high"
#}

#function getConfidenceInterval95WaldAverage() {
#	
#}


#getConfidenceInterval95Wald "$1" "$2"
#echo "$result"

