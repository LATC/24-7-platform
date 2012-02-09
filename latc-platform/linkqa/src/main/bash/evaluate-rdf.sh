#!/bin/bash

# Usage:
#    Linkset.nt Referenceset.nt Subject-Uri [polarity] 
#

ls="$1"
rs="$2"
id="$3"
polarity="$4"

DIR="$( cd "$( dirname "$0" )" && pwd )"

if [ ! -f "$ls" -o ! -f "$rs" -o -z "$id" ]; then
        echo "Usage: Linkset.nt Referenceset.nt http://your.linkset.uri [polarity]"
        echo "Note: polarity can be either positive or negative. If polarity is not given, and refset filename contains pos or neg, polarity is automatically set accordingly."
        exit -1
fi


if [[ -z "$polarity" ]]; then

	if [[ "$rs" =~ pos ]]; then
	    polarity="positive"
	elif [[ "$rs" =~ neg ]]; then
	    polarity="negative"
	fi
fi

if [ -z "$polarity" ]; then
	echo "Polarity (positive/negative) either not given or it could not be determined from the refset name"
	exit -1
fi


# TODO The tempfile is somewhat ugly, maybe we can get rid of it
tmpFile=`tempfile`
"$DIR"/evaluate.sh "$1" "$2" > "$tmpFile"
"$DIR"/to-rdf.sh "$id" "$tmpFile" "$polarity"

rm "$tmpFile"

