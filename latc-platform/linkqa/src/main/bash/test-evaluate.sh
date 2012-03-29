#!/bin/bash

#
# evaluate.sh <linksef-filename> <refset-filename>
#
# This script outputs data in an 'ini'-format. It is possible to use bash's "source" command on the output.
#
./evaluate.sh     ../../../../../link-specifications/geonames-linkedgeodata-shop/links.nt ../../../../../link-specifications/geonames-linkedgeodata-shop/positive.nt
./evaluate.sh     ../../../../../link-specifications/geonames-linkedgeodata-shop/links.nt ../../../../../link-specifications/geonames-linkedgeodata-shop/negative.nt

#
# evaluate.sh <linkset-filename> <refset-filename> <subject-uri> [<polarity>]
#
# Generates RDF output with the given subject-uri. 
# Depends on evalute.sh.
# Polarity specifies whether the refset is positive or negative and determines which vocabulary to use in the RDF output.
# If no polarity is specified, the refset-filename is checked whether it conains pos or neg.
# 
#
./evaluate-rdf.sh ../../../../../link-specifications/geonames-linkedgeodata-shop/links.nt ../../../../../link-specifications/geonames-linkedgeodata-shop/positive.nt 'http://linkset.org'
./evaluate-rdf.sh ../../../../../link-specifications/geonames-linkedgeodata-shop/links.nt ../../../../../link-specifications/geonames-linkedgeodata-shop/negative.nt 'http://linkset.org'

