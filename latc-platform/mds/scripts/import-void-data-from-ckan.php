<?php
define('BASE_DIR', realpath(dirname(dirname(__FILE__))));
define('LIB_DIR', BASE_DIR.'/lib/');
define('MORIARTY_HTTP_CACHE_DIR', BASE_DIR.'/cache/');
define('MORIARTY_ARC_DIR', LIB_DIR.'arc/');
define('VOID', 'http://rdfs.org/ns/void#');
define('OPENVOCAB', 'http://open.vocab.org/terms/');

require_once LIB_DIR.'/moriarty/store.class.php';
require_once LIB_DIR.'/moriarty/credentials.class.php';
require_once LIB_DIR.'/moriarty/httprequestfactory.class.php';
require_once LIB_DIR.'/moriarty/sparqlservice.class.php';
require_once LIB_DIR.'/moriarty/simplegraph.class.php';

require BASE_DIR.'/talis-store-credentials.php';

$latcStore = new Store(LATC_STORE_URI, new Credentials('latc', LATC_STORE_PASSWORD));
$RequestFactory = new HttpRequestFactory();
$SparqlEndpoint = new SparqlService("http://semantic.ckan.net/sparql/");

$pageSize = 500;
if(file_exists('last_imported_from_ckan')){
  $lastModTime = date('c', filemtime('last_imported_from_ckan'));
  $modifiedClause = '?s dc:modified ?mod . FILTER(?mod > "'.$lastModTime.'"^^xsd:dateTime ) .';
} else {
  $modifiedClause = '';
}

$licenseIDs = array();

$continue = true;
$offset = 0;
while($continue){

$query = <<<_SPARQL_
PREFIX void: <http://rdfs.org/ns/void#>
PREFIX dc: <http://purl.org/dc/terms/>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>

select distinct ?s
WHERE {
  ?s dc:isPartOf <http://ckan.net/group/lodcloud> .
   {$modifiedClause}
}
LIMIT {$pageSize} OFFSET {$offset}
_SPARQL_;
$requests = array();
$results = $SparqlEndpoint->select_to_array($query);

foreach($results as $row){
  $uri = $row['s']['value'];
  $request = $RequestFactory->make("GET", $uri);
  $request->set_accept('application/rdf+xml');
  $response = $request->execute();
    $graph = new SimpleGraph();
    $graph->add_rdf($response->body);
    $graph->add_resource_triple($uri, RDF_TYPE, 'http://rdfs.org/ns/void#Dataset');
    $graph->add_literal_triple($uri, OPENVOCAB.'canonicalUri', $uri);
    $ckanJSON = $graph->get_first_literal($uri, 'http://semantic.ckan.net/schema#json');
    $graph->remove_literal_triple($uri,  'http://semantic.ckan.net/schema#json', $ckanJSON);
    $ckanArray = json_decode($ckanJSON, true);
   if(isset($ckanArray['extras'])){
      if(isset($ckanArray['extras']['uriSpace'])){
        $graph->add_literal_triple($uri, VOID.'uriSpace', $ckanArray['extras']['uriSpace']);
      }
    }

    $graphURIs = array();
    foreach($graph->get_index() as $s => $ps){
      if(strpos($s, 'http://ckan.net/package/')===0) $graphURIs[]=$s;
      foreach($ps as $p => $os){
        foreach($os as $o){
          if($o['type']=='uri'){
            if(strpos($o['value'], 'http://ckan.net/package')===0) $graphURIs[]=$o['value'];
            if(strpos($o['value'], 'http://ckan.net/tag')===0) $graphURIs[]=$o['value'];
          }
        }
      }
    }
  $graphURIs = array_unique($graphURIs);
    foreach($graphURIs as $oldUri){
      $lodCloudUri = str_replace('http://ckan.net/package/', 'http://lod-cloud.net/', $oldUri);
      $graph->replace_resource($oldUri, $lodCloudUri);
      $graph->add_resource_triple($lodCloudUri, OWL_SAMEAS, $oldUri);
    }

    $packageName = str_replace('http://ckan.net/package/', '', $uri);

    $graph->skolemise_bnodes('http://lod-cloud.net/'.$packageName.'/');
    for ($i = 0; $i < 5; $i++) {
      // try five times
      $response =   $latcStore->mirror_from_url($uri, $graph->to_json());
      if($response['success']){
        echo "\n {$uri} mirrored to triple store. \n";
        break;
      }  
    }
    if(!$response['success']){
      error_log(date('c')."\t{$uri} was not mirrored to the triple store.\n", 3, 'import_errors.log');
      file_put_contents('import.json', json_encode($response));
    }
    
}


  if(count(array_keys($results)) < $pageSize){
    $continue = false;
  } else {
    $offset+=$pageSize;
  }


} //endwhile

//update last modifed time
//
touch('last_imported_from_ckan');
?>
