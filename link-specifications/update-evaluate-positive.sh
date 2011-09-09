#!/bin/bash

# Scans all link specifications, and whenever both files, positive.nt and links.nt, exist, performs an evaluation. The results are stored in files named "eval-positive.ini" in the respective linkset folders.

# Usage: ./update-eval-positive.sh

# TODO Combine eval-positive with eval-negative.
# TODO Ini4j doesn't like the format, as it requires a section heading

cmd="../latc-platform/linkqa/src/main/bash/evaluate.sh"
basePath="."

for a in `find "$basePath" -type d`; do

	rs="$a/positive.nt"
	ls="$a/links.nt"

	# Check if a positive.nt file exists
	if [ -f "$ls" -a -f "$rs" ]; then
		"$cmd" "$ls" "$rs" > "$a/eval-positive.ini"
	fi

done

