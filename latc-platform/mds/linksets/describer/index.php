<?php
define('MORIARTY_ARC_DIR', '../../lib/arc/');
define('MORIARTY_HTTP_CACHE_DIR', '../../cache');

require '../../lib/moriarty/simplegraph.class.php';
require '../../lib/moriarty/sparqlservice.class.php';

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
OPTIONAL{ <{$uri}> ?p ?o . }

OPTIONAL{ ?sameAs owl:sameAs <{$uri}> .  }

OPTIONAL{ ?contains spatial:contains <{$uri}> .  }
OPTIONAL{ ?within spatial:within <{$uri}> .  }
OPTIONAL{ ?near ov:near <{$uri}> .  }

}
 }
_SPARQL_;

  $endpoint = new SparqlService('http://db.cohodo.net/sparql/cdf4ccd3-5916-49c6-8c11-6356b5162cba?graph=default');
  $response = $endpoint->query($SPARQL);
  if($response->is_success()){

    $rdf = $response->body;

    $httprequestfactory = new HttpRequestFactory();
    $request = $httprequestfactory->make('GET', $uri);
    $request->set_accept("text/turtle,application/rdf+xml,text/rdf+n3,*/*;q=0.8");    
    $response = $request->execute();
    $graph = new SimpleGraph();
    $graph->add_rdf($rdf);
    $graph->add_rdf($response->body);
    header("Content-type: application/rdf+xml");
    echo $graph->to_rdfxml();
    die;
  
  } else {

    header("HTTP/1.1 502 Bad Gateway");

    die("Bad response from SPARQL endpoint.");
  
  }
  
}

?>
