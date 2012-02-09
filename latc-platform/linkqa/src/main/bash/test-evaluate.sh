#!/bin/bash

./evaluate.sh     ../../../../../link-specifications/geonames-linkedgeodata-shop/links.nt ../../../../../link-specifications/geonames-linkedgeodata-shop/positive.nt
./evaluate-rdf.sh ../../../../../link-specifications/geonames-linkedgeodata-shop/links.nt ../../../../../link-specifications/geonames-linkedgeodata-shop/positive.nt 'http://linkset.org'

