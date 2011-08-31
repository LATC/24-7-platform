# Example of Void 

`@prefix rdf:	<http://www.w3.org/1999/02/22-rdf-syntax-ns#> .

@prefix rdfs:	<http://www.w3.org/2000/01/rdf-schema#> .

@prefix owl:	<http://www.w3.org/2002/07/owl#> .

@prefix void:   <http://rdfs.org/ns/void#> .  

@prefix prv:    <http://purl.org/net/provenance/ns#> .  

@prefix xsd:    <http://www.w3.org/2001/XMLSchema#> .

@prefix foaf:   <http://xmlns.com/foaf/0.1> .

@prefix doap:   <http://usefulinc.com/ns/doap#> .

@prefix dc:	<http://purl.org/dc/terms/#> .

@prefix silkspec:	<http://vocab.deri.ie/LinkSpec#> .

@prefix prvTypes:	<http://purl.org/net/provenance/types#> .

@prefix : <#> .

@prefix dbpedia: <http://dbpedia.org/ontology/>. 


:dbpedia a void:Dataset;
	void:sparqlEndpoint <http://sparql.sindice.com/sparql> .

:geolinkeddata a void:Dataset;
	void:sparqlEndpoint <http://sparql.sindice.com/sparql> .

:dbpedia2geolinkeddata a void:Linkset ;
	void:linkPredicate dbpedia:Place;
    void:subjectsTarget :dbpedia;
	void:objectsTarget :geolinkeddata;
	void:triples  0;
	void:dataDump <http://demo.sindice.net/latctemp/2011-08-26/DBpediaToGeoLinkedData(Place)/links.nt>;
    	void:feature <http://www.w3.org/ns/formats/Turtle>;
   	a prv:DataItem ;
    	prv:createdBy [	a prv:DataCreation ;
                	prv:performedAt "2011-08-26T10:18:07+0100"^^xsd:dateTime ;
			prv:usedData :dbpedia ;
			prv:usedData :geolinkeddata ;
                	prv:usedGuideline :linkspec;
			prv:performedBy :SilkMapReduce
			] ;
	.

:linkspec a silkspec:SilkSpec ;
	silkspec:ID "ff8081812e2e36ce012e2e36cec90000"^^xsd:hexBinary ;
	silkspec:accessedResource <http://latc-console.few.vu.nl/api/task/ff8081812e2e36ce012e2e36cec90000/configuration>;
	silkspec:Title "ff8081812e2e36ce012e2e36cec90000";
	dc:created "2011-02-16T12:24:27"^^xsd:dateTime;
	dc:modified "2011-06-22T12:43:54"^^xsd:dateTime;
	prv:createdBy [	a prv:DataCreation ;
			prv:performedBy <Unknown>;
                   	prv:performedAt "2011-02-16T12:24:27"^^xsd:dateTime
	          	];
	prv:retrievedBy [	a prv:DataAccess ;
                        	prv:performedAt "2011-08-26T10:17:05+0100"^^xsd:dateTime ;
				prv:accessedResource   <http://demo.sindice.net/latctemp/2011-08-26/DBpediaToGeoLinkedData(Place)/spec.xml>;      
		        	prv:accessedService :console;
				prv:performedBy :SilkMapReduce
        		 ];
.     

:console a prv:DataProvidingService;
	foaf:homepage  <http://latc-console.few.vu.nl>.

:SilkMapReduce a  prvTypes:DataCreatingService ;
	prv:deployedSoftware :silkmr .

:silkmr a doap:Version;
    doap:revision "2.3" .

:silkmrProject a doap:Project;
    doap:release :silkmr;
    doap:homepage <http://www4.wiwiss.fu-berlin.de/bizer/silk> .`

