#!/bin/bash

DIR="$( cd "$( dirname "$0" )" && pwd )"

repoPath="$1"

if [ -z "$repoPath" ]; then
    echo "Path to repository not specified"
    exit 1
fi

scheme="http"
host="qa.linkeddata.org"

qao="$scheme://$host/ontology/"
qap="$scheme://$host/resource/linking-project/"
qal="$scheme://$host/resource/linkset/"
qae="$scheme://$host/resource/eval/"
qaa="$scheme://$host/resource/agent/"

baseFileUri="http://$host/file/"

#baseUri="http://qa.linkeddata.org/resource/linkset/"

cd "$repoPath"

for file in `find . -name "metadata.ini"`; do
    
    relativePath="${file:2}"
    dir=`dirname "$relativePath"`
    revision=`basename "$dir"`
    fileName=`basename "$relativePath"`
    

    metadataFile="$dir/metadata.ini"
    linksetFile="$dir/links.nt"
    posRef="$dir/eval-positive.ini"
    negRef="$dir/eval-negative.ini"

    if [ ! -f "$metadataFile" -o ! -f "$linksetFile" -o ! -f "$posRef" -o ! -f "$negRef" ]; then
        continue;
    fi

    source "$metadataFile"

    tmp=`dirname $dir`
    projectLabel=`basename $tmp`
    projectUri="${qap}$projectLabel"
    echo "<$projectUri> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <${qao}LinkingProject> ."
    echo "<$projectUri> <http://www.w3.org/2000/01/rdf-schema#label> \"$projectLabel\" ."

    linksetFileUri="${baseFileUri}${linksetFile}"
    posFileUri="${baseFileUri}${posRef}"
    negFileUri=${baseFileUri}${negRef}


    #Base path
    linksetUri="${qal}$dir"
    linksetLabel="Revision '$revision' of $projectLabel"

    echo "<$projectUri> <${qao}linkset> <$linksetUri> ."


    authorEnc=`urlencode "$author"`
    authorUri="${qaa}$authorEnc"


    echo "<$linksetUri> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <${qao}Linkset> ."
    echo "<$linksetUri> <http://www.w3.org/2000/01/rdf-schema#label> \"$linksetLabel\" ." 
    echo "<$linksetUri> <http://purl.org/dc/terms/created> \"$timestamp\"^^<http://www.w3.org/2001/XMLSchema#> ."
    echo "<$linksetUri> <${qao}dataFile> <$linksetFileUri> ."

    echo "<$linksetUri> <http://purl.org/dc/terms/creator> <$authorUri> . "
    echo "<$authorUri> <http://www.w3.org/2000/01/rdf-schema#label> \"$author\" ."


    # Metadata

    "$DIR"/metadata-to-rdf.sh "$url"


    # Positive eval
    if [ -f "$posRef" -a "$negRef" ]; then
        evalUri="${qae}$dir-1"
        echo "<$evalUri> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <${qao}RandomSampleEval> . "
        echo "<$evalUri> <http://purl.org/dc/terms/creator> <$authorUri> ."
	echo "<$linksetUri> <${qao}linkset> <$evalUri> ."
	
        echo "<$evalUri> <${qao}positiveFile> <$posFileUri> ."
        echo "<$evalUri> <${qao}positiveFile> <$negFileUri> ."
        



        # TODO HACK: We simply assume that this evaluation is the only one for the project
        # Actually if there are multiple evals, we have to create a new eval which is the average of all evals, and associate it with the project
	echo "<$projectUri> <${qao}assessment> <$evalUri> ."

        "$DIR"/to-rdf.sh "$evalUri" "$posRef" "positive"
#    fi

    # Negative eval
#    if [ -f "$negRef" ]; then
        "$DIR"/to-rdf.sh "$evalUri" "$negRef" "negative"
    fi



    #echo "$url"
    #echo "$posRef"
    #echo "$dir"
done

