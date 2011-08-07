<?php
define('MORIARTY_ARC_DIR', '../lib/arc/');
define('MORIARTY_HTTP_CACHE_DIR', '../cache');

require '../lib/moriarty/simplegraph.class.php';
require '../lib/moriarty/sparqlservice.class.php';

if(isset($_GET['uri'])){
  $uri = trim($_GET['uri']);
$this_doc_uri = 'http://mds.lod-cloud.net/linksets/describer/?uri='.urlencode($_GET['uri']);
$this_resource_uri = $this_doc_uri.'#id';
  $SPARQL = <<<_SPARQL_

PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX ov: <http://open.vocab.org/terms/>
PREFIX spatial: <http://data.ordnancesurvey.co.uk/ontology/spatialrelations/>
PREFIX dct: <http://purl.org/dc/terms/>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>

CONSTRUCT {
  <{$this_resource_uri}> owl:sameAs <{$uri}> .
  <{$this_resource_uri}> ?p ?o .
  <{$this_resource_uri}> owl:sameAs ?sameAs .
  <{$this_resource_uri}> ov:near ?near .
  <{$this_resource_uri}> spatial:within ?contains .
  <{$this_resource_uri}> spatial:contains ?within .
  <{$uri}> ?p ?o .
  <{$uri}> owl:sameAs ?sameAs .
  <{$uri}> ov:near ?near .
  <{$uri}> spatial:within ?contains .
  <{$uri}> spatial:contains ?within .
  <{$this_doc_uri}> dct:source ?g ;
    foaf:primaryTopic <{$this_resource_uri}>
.
} WHERE {
GRAPH ?g {

{ <{$uri}> ?p ?o . }
  UNION
{ ?sameAs owl:sameAs <{$uri}> . 
    OPTIONAL {  
      ?sameAs ?p ?o .
      FILTER(?o!=<{$uri}>) .
  }
 } 
  UNION
{ ?contains spatial:contains <{$uri}> .  } 
  UNION
{ ?within spatial:within <{$uri}> .  } 
  UNION
{ ?near ov:near <{$uri}> .  }
UNION
#transitive
 { <{$uri}> owl:sameAs [ owl:sameAs ?sameAs ] . }
#UNION
#reverse-transitive
 #{ ?sameAs   owl:sameAs [ owl:sameAs <{$uri}> ] . }
}
 }
_SPARQL_;

  $endpoint = new SparqlService('http://db.cohodo.net/sparql/cdf4ccd3-5916-49c6-8c11-6356b5162cba?graph=default');
  $response = $endpoint->query($SPARQL);
  if($response->is_success()){

    $rdf = $response->body;
    $graph = new SimpleGraph();
    $graph->add_rdf($rdf);
   if(isset($_GET['augment'])){
      $httprequestfactory = new HttpRequestFactory();
      $request = $httprequestfactory->make('GET', $uri);
      $request->set_accept("text/turtle,application/rdf+xml,text/rdf+n3,*/*;q=0.8");    
      $response = $request->execute();
      $graph->add_rdf($response->body);
    }
    header("Content-type: application/rdf+xml");
    echo $graph->to_rdfxml();
    die;
  
  } else {

    header("HTTP/1.1 502 Bad Gateway");

    die("Bad response from SPARQL endpoint.");
  
  }
  
}

?>
