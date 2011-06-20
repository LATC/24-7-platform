274629 links
sample size 250
250 positive links
0 negative links 
0 unsure links

precision 1.0

Spec created done by MultiMatching.java, Evaluation manually with EvaluationTool.java, both in SAIM (https://sourceforge.net/projects/saim/)  
Konrad Höffner konrad.hoeffner@uni-leipzig.de Fri Jun 17 18:47:33 CEST 2011

Created by Amrapali J. Zaveri (zaveri@informatik.uni-leipzig.de) and Konrad Höffner 2011
Uses one of our local SPARQL endpoints (http://lgd.aksw.org:8900/sparql), which is not guaranteed to run/keep this data in the future so
you may have to use another SPARQL endpoint.

The matching is a bit complicated because it is "LinkedCT facility is located in GHO country".

Now as an example let's take the facility "http://linkedct.org/data/facility/29cc7e9f45f9c3284df7d7483aa1e9f2/" which has <linkedct:facility_facility_name>Fred Hutchinson Cancer Research Center</linkedct:facility_facility_name>.
It also has <linkedct:facility_address rdf:resource="http://data.linkedct.org/resource/address/seattle-washington-98109-united-states"/>.
This address resource has <linkedct:address_country rdf:resource="http://data.linkedct.org/resource/country/united-states"/>.
Now the country resources has a label of "United States".
The problem is that Silk is not made for this type of "around the corners" - matching, so we use another approach.
