#!/bin/bash
find . -type d | while read line; do if [ ! -f "$line/positive.nt" ]; then echo "$line"; fi; done;
