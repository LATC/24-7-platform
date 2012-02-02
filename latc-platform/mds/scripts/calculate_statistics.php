<?php
require_once 'inc.php';

$vocabGraph = new SimpleGraph();
$statsGraph = new SimpleGraph();
$vocabGraph->add_rdf(file_get_contents(BASE_DIR.'/documents/dsi.vocab.ttl'));

$latcStore = new Store(LATC_STORE_URI, new Credentials('latc', LATC_STORE_PASSWORD));

foreach($vocabGraph->get_subjects_of_type(RDF_PROPERTY) as $Property){

  if($sparqlQuery = $vocabGraph->get_first_literal($Property, DSI.'calculatedFromSPARQL')){
    $results = $latcStore->get_sparql_service()->select_to_array($sparqlQuery);
    if(!empty($results)){
      foreach($results as $row){
        if(isset($row['item'])){  
          $uri = trim($row['item']['value']);
          $object =  trim($row['count']['value']);
          $statsGraph->add_literal_triple($uri, $Property, $object, false, xsd.'integer');
        }
      }
    }
  }
}

$GraphUri = LOD.'properties-calculated-with-sparql';
$statsGraph->add_literal_triple($GraphUri, DCT.'modified', date('c'), false, xsd.'dateTime');
$turtle = $statsGraph->to_turtle();

$results = $latcStore->mirror_from_url($GraphUri, $turtle);
file_put_contents(BASE_DIR.'/documents/properties-calculated-with-sparql.ttl', $turtle);

if($results['success']==true){
  echo "\n Calculated Properties added to Metadata Store $GraphUri \n";
} else {
  echo "\n Something went wrong adding Calculated Properties to Metadata Store: \n";
  foreach($results as $k => $v){
    if(is_a($v, 'HttpResponse') AND !$v->is_success()){
      var_dump($k, $v);
    }
  }
}
$results = $latcStore->mirror_from_url(LOD.'themes', file_get_contents(BASE_DIR.'/documents/themes.ttl'));
$results = $latcStore->mirror_from_url(LOD.'licenses', file_get_contents(BASE_DIR.'/documents/licenses.ttl'));

?>
