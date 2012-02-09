#!/bin/bash

ls=$1
rs=$2
id=$3

DIR="$( cd "$( dirname "$0" )" && pwd )"

if [ ! -f "$ls" -o ! -f "$rs" -o -z "$id" ]; then
        echo "Usage: Linkset.nt Referenceset.nt http://your.linkset.uri"
        exit -1
fi

tmpFile=`tempfile`
"$DIR"/evaluate.sh "$1" "$2" > "$tmpFile"
"$DIR"/to-rdf.sh "$id" "$tmpFile"

rm "$tmpFile"

