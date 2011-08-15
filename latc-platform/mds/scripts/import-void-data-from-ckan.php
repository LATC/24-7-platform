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
define('lodThemes', LOD.'themes/');

$lodTopics = array(

  'geographic',
  'government',
  'media',
  'crossdomain',
  'lifesciences',
  'usergeneratedcontent',
  'ecommerce',
  'schemata',

);


$latcStore = new Store(LATC_STORE_URI, new Credentials('latc', LATC_STORE_PASSWORD));
$RequestFactory = new HttpRequestFactory();
$SparqlEndpoint = new SparqlService("http://semantic.ckan.net/sparql/");


$Prefixes = json_decode($RequestFactory->make('GET', 'http://prefix.cc/popular/all.file.json')->execute()->body, 1);
$Prefixes = array_filter($Prefixes, create_function('$a', 'return $a;'));


$pageSize = 500;
if(file_exists('last_imported_from_ckan')){
  $lastModTime = date('c', filemtime('last_imported_from_ckan'));
  $modifiedClause = 'OPTIONAL { ?s dc:modified ?mod . FILTER(?mod > "'.$lastModTime.'"^^xsd:dateTime ) . } OPTIONAL { ?s dc:created ?created . FILTER(?created > "'.$lastModTime.'"^^xsd:dateTime ) . } ';
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
   $graphURIs = array();
    foreach($graph->get_index() as $s => $ps){
      if(strpos($s, 'http://ckan.net/package/')===0 OR strpos($s, 'http://ckan.net/tag')===0) $graphURIs[]=$s;
      foreach($ps as $p => $os){
        foreach($os as $o){
          if($o['type']=='uri'){
            if(strpos($o['value'], 'http://ckan.net/package')===0 OR strpos($o['value'], 'http://ckan.net/tag')===0) $graphURIs[]=$o['value'];
          }
        }
      }
    }
  $graphURIs = array_unique($graphURIs);
    foreach($graphURIs as $oldUri){
      if(strpos($oldUri, 'http://ckan.net/package/')===0) $lodCloudUri = str_replace('http://ckan.net/package/', 'http://lod-cloud.net/', $oldUri);
      else if(strpos($oldUri, 'http://ckan.net/tag/')===0) $lodCloudUri = str_replace('http://ckan.net/tag/', 'http://lod-cloud.net/tag/', $oldUri);
      $graph->replace_resource($oldUri, $lodCloudUri);
      $graph->add_resource_triple($lodCloudUri, OWL_SAMEAS, $oldUri);
    }

    $packageName = str_replace('http://ckan.net/package/', '', $uri);

    $uri = LOD.$packageName;

    $graph->add_literal_triple($uri, OPENVOCAB.'shortName', $packageName);

       if(isset($ckanArray['extras'])){
      if(isset($ckanArray['extras']['uriSpace'])){
        $graph->add_literal_triple($uri, VOID.'uriSpace', $ckanArray['extras']['uriSpace']);
      }
      else if(isset($ckanArray['extras']['namespace'])){
        $graph->add_literal_triple($uri, VOID.'uriSpace', $ckanArray['extras']['namespace']);
      }

      foreach(array('classes', 'entities','triples', 'properties', 'distinctSubjects', 'distinctObjects', 'documents') as $statProp){
        if(isset($ckanArray['extras'][$statProp])){
          $graph->add_literal_triple($uri, VOID.$statProp, intval($ckanArray['extras'][$statProp]), '',xsd.'integer' );
        }
      }
 
    }
  if(isset($ckanArray['title'])){
        $graph->add_literal_triple($uri, RDFS_LABEL,  $ckanArray['title']);
    }

    if(isset($ckanArray['resources'])){
      foreach($ckanArray['resources'] as $resource){
        if($resource['format'] == 'meta/rdf-schema'){
          $graph->add_resource_triple($uri, VOID.'vocabulary', rtrim($resource['url'], '#'));
        }
        if($resource['format'] == 'meta/void'){
          $graph->add_resource_triple($uri, RDFS_SEEALSO, $resource['url']);
        }
      }
    }

    
    foreach($ckanArray['tags'] as $tag){
      if(strpos($tag, 'format-')===0){
        $prefix = str_replace('format-', '', $tag);
        if(isset($Prefixes[$prefix])){
          $vocabUri = rtrim($Prefixes[$prefix], '#');
          $graph->add_literal_triple($vocabUri, VANN.'preferredNamespacePrefix', $prefix);
          $graph->add_literal_triple($vocabUri, VANN.'preferredNamespaceUri', $Prefixes[$prefix]);
          $graph->add_resource_triple($uri, VOID.'vocabulary', $vocabUri);
        }
      } else if(in_array($tag, $lodTopics)) {
          $graph->add_resource_triple($uri, DCT.'subject', lodThemes.$tag);
      }
      
    }

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
      file_put_contents('failed_import.json', json_encode($response));
      die;
    } 
}


  if(count(array_keys($results)) < $pageSize){
    $continue = false;
  } else {
    $offset+=$pageSize;
  }


} //endwhile

require BASE_DIR.'/scripts/calculate_statistics.php';
//update last modifed time
//
touch('last_imported_from_ckan');
?>
