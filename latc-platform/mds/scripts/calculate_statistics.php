<?php
define('BASE_DIR', realpath(dirname(dirname(__FILE__))));
define('LIB_DIR', BASE_DIR.'/lib/');
define('MORIARTY_HTTP_CACHE_DIR', BASE_DIR.'/cache/');
define('MORIARTY_ARC_DIR', LIB_DIR.'arc/');
define('VOID', 'http://rdfs.org/ns/void#');
define('OPENVOCAB', 'http://open.vocab.org/terms/');
define('DCT', 'http://purl.org/dc/terms/');
define('VANN', 'http://purl.org/vocab/vann/');
define('xsd', 'http://www.w3.org/2001/XMLSchema#');

require_once LIB_DIR.'/moriarty/store.class.php';
require_once LIB_DIR.'/moriarty/credentials.class.php';
require_once LIB_DIR.'/moriarty/httprequestfactory.class.php';
require_once LIB_DIR.'/moriarty/sparqlservice.class.php';
require_once LIB_DIR.'/moriarty/simplegraph.class.php';

require BASE_DIR.'/talis-store-credentials.php';
define('LOD', 'http://lod-cloud.net/');
define('DSI', 'http://dsi.lod-cloud.net/vocab#');

$vocabGraph = new SimpleGraph();
$statsGraph = new SimpleGraph();
$vocabGraph->add_rdf(file_get_contents(BASE_DIR.'/documents/dsi.vocab.ttl'));

$latcStore = new Store(LATC_STORE_URI, new Credentials('latc', LATC_STORE_PASSWORD));

foreach($vocabGraph->get_subjects_of_type(RDF_PROPERTY) as $Property){

  if($sparqlQuery = $vocabGraph->get_first_literal($Property, DSI.'calculatedFromSPARQL')){
    $results = $latcStore->get_sparql_service()->select_to_array($sparqlQuery);
    if(!empty($results)){
      foreach($results as $row){
        $uri = $row['item']['value'];
        $object =  $row['count']['value'];
        $statsGraph->add_literal_triple($uri, $Property, $object, false, xsd.'integer');
      }
    }
  }
}

$GraphUri = LOD.'properties-calculated-with-sparql';
$statsGraph->add_literal_triple($GraphUri, DCT.'modified', date('c'), false, xsd.'dateTime');
$turtle = $statsGraph->to_turtle();

$results = $latcStore->mirror_from_url($GraphUri, $turtle);
file_put_contents(BASE_DIR.'/documents/properties-calculated-with-sparql.ttl', $turtle);
if($results['success']){
  echo "\n Calculated Properties added to Metadata Store \n";
} else {
  echo "\n Something went wrong adding Calculated Properties to Metadata Store: \n";
  foreach($results as $k => $v){
    if(is_a($v, 'HttpResponse') AND !$v->is_success()){
      var_dump($k, $v);
    }
  }
}
?>
