#!/bin/bash
# ^ Yes, we really use bash features here - won't work with /bin/sh

# This script iterates all revisions of the git repository,
# and computes the quality-assurance metrics for each revision
# where either the linkset or one of its reference sets was changed.
# It thereby creates a reorganized directory structure.

# Issues:
# 1: If files were renamed, we get zero size files :/ Probably happens with removals too

branch="master"

outDir="$1"

if [ -z "$outDir" ]; then
	echo "Please specify a path on where to create the repo."
	exit 1
fi


for commit in $(git rev-list $branch)
do
	author=`git show -s --format="%aN" $commit`
	timestamp=`git show -s --format="%ci" $commit`
        #echo "Timestamp: $timestamp"
	#echo "$author"

#	echo -e "Commit: $commit\n"
	git diff-tree --name-only -r "$commit" | tail -n +2 | while read file; do

		# Get the directory of the current file
		rawBase=`dirname "$file"`

		# Remove the first directory (link_specifications) from the base
		base=${rawBase#*/}

		filename=`basename "$file"`

#		if [[ "$file" =~ 'links|positive|negative' ]]; then
		if [[ "$file" =~ 'links.' || "$file" =~ 'positive.' || "$file" =~ 'negative.' || "$file" =~ 'spec.' ]]; then
			dir="$outDir/$base/$commit"
			mkdir -p "$dir"

			echo "timestamp='$timestamp'" > "$dir/metadata.ini"
			echo "author='$author'" >> "$dir/metadata.ini"


			target="$dir/$filename"

                        echo "$target"
			git show "$commit:$file" > "$target"
		fi


#		echo "Line: $file"
	done


#	git ls-tree --name-only -r $commit
#    if git ls-tree --name-only -r $commit | grep -q '\.hbm\.xml$'; then
#        echo $commit
#        exit 0
#    fi

done


./update-evaluate-positive.sh "$outDir"
./update-evaluate-negative.sh "$outDir"

