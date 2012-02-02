<?php
require 'inc.php';

define('UPDATE_URI', 'http://db.cohodo.net/updates/direct/cdf4ccd3-5916-49c6-8c11-6356b5162cba');

exec('ls  /Users/keithalexander/dev/LATC/24-7-platform/link-specifications/*/links.nt', $dirs);

$httpreqfac = new HttpRequestFactory();

$req = $httpreqfac->make('GET', 'http://ckan.net/api/1/rest/group/lodcloud');

$response = $req->execute();

$ckanLodCloudGroup = json_decode($response->body, 1);
array_push($ckanLodCloudGroup['packages'], 'bluk-bnb');
array_push($ckanLodCloudGroup['packages'], 'data-incubator-metoffice');
$kasabiStoreUri = 'http://api.kasabi.com/dataset/latc-linksets/store?apikey=7248c9620ded77ac99c2befdcaec0dd934bd7eda';
$KasabiStore = new Graph($kasabiStoreUri);

$ckanpackageNameRemappings = array(

  'geonames' => 'geonames-semantic-web',
  'climb' => 'data-incubator-climb',
  'os' => 'ordnance-survey-linked-data',
  'ordnance-survey' => 'ordnance-survey-linked-data',
  'ordnancesurvey' => 'ordnance-survey-linked-data',
  'citeseer' => 'rkb_explorer_data',
  'musicbrainz' => 'data-incubator-musicbrainz',
  'transport' => 'transport-data-gov-uk',
  'metoffice' => 'data-incubator-metoffice',
);

$metaGraph = new SimpleGraph();

foreach($dirs as $filename){
  var_dump($filename);
  
  $spec_filename = str_replace('links.nt', 'spec.xml', $filename);

  $dom = new DomDocument();
  $dom->load($spec_filename);
  $xpath = new DomXpath($dom);
  preg_match('@([^/]+?)/links.nt$@',$filename, $m);
  $linksetID = $m[1];
  $linksetURI = 'http://lod-cloud.net/latc-linksets/'.$linksetID;

  
  /*
  $kasabiResponses = $KasabiStore->submit_ntriples_in_batches_from_file($filename,5000,true);
  //  var_dump($kasabiResponse);
  foreach($kasabiResponses as $kasabiResponse){
   var_dump(
    array('status' =>  $kasabiResponse->status_code,
    'body' => $kasabiResponse->body,
  )
  );
  }
   
   */

/*
  $Graph = new Graph(UPDATE_URI.'?graph='.urlencode($linksetURI));
  $response =  $Graph->submit_turtle(file_get_contents($filename));
  var_dump(
    array('status' =>  $response->status_code,
    'body' => $response->body,
  )
  );
 */
/*
   $Graph = new Graph('http://api.talis.com/stores/latc-mds/meta', new Credentials('latc', LATC_STORE_PASSWORD));
  $response =  $Graph->submit_turtle(file_get_contents($filename));
  var_dump(
    array('status' =>  $response->status_code,
    'body' => $response->body,
  )
);

 */


  $sourceName = $xpath->query('//SourceDataset[@dataSource]')->item(0)->getAttribute('dataSource');
  $targetName = $xpath->query('//TargetDataset[@dataSource]')->item(0)->getAttribute('dataSource');
  $sourceEndpoint = $xpath->query('//DataSource[@id="'.$sourceName.'"]/Param[@name="endpointURI"]')->item(0)->getAttribute('value');
  $targetEndpoint = $xpath->query('//DataSource[@id="'.$targetName.'"]/Param[@name="endpointURI"]')->item(0)->getAttribute('value');

  $specChanged = false;
  if(isset($ckanpackageNameRemappings[$sourceName])){
    $ckanName = $ckanpackageNameRemappings[$sourceName];

    foreach($xpath->query('//SourceDataset[@dataSource="'.$sourceName.'"]') as $el){
      $el->setAttribute('dataSource', $ckanName);
    }
    foreach( $xpath->query('//DataSource[@id="'.$sourceName.'"]') as $el){
      $el->setAttribute('id', $ckanName);
    }

    $sourceName = $ckanName;
    $specChanged = true;
//   echo $dom->saveXML();
  //  exit;
  }

  if(isset($ckanpackageNameRemappings[$targetName])){
    $ckanName = $ckanpackageNameRemappings[$targetName];    
    foreach($xpath->query('//TargetDataset[@dataSource="'.$targetName.'"]') as $el){
      $el->setAttribute('dataSource', $ckanName);
    }
    foreach( $xpath->query('//DataSource[@id="'.$targetName.'"]') as $el){
      $el->setAttribute('id', $ckanName);
    }
    $targetName = $ckanName;
    $specChanged = true;
  }

  //if($specChanged){
   // $changedSpec = $dom->saveXML();
    //file_put_contents('modified_specs/'.array_pop(explode('/',dirname($spec_filename))).'.xml', $changedSpec);
  //}
  $sourceDatasetUri  = $linksetURI.'/datasets/'.$sourceName;

  if(in_array($sourceName, $ckanLodCloudGroup['packages'])){
    $sourceDatasetUri = 'http://lod-cloud.net/dataset/'.$sourceName;
  }

  $targetDatasetUri  = $linksetURI.'/datasets/'.$targetName;

  if(in_array($targetName, $ckanLodCloudGroup['packages'])){
    $targetDatasetUri = 'http://lod-cloud.net/dataset/'.$targetName;
  }



  $lineCount = system('wc -l '.$filename);
  preg_match('/\d+/', $lineCount, $lcm);
  $noOfTriples = $lcm[0];

  $metaGraph->add_resource_triple($linksetURI, RDF_TYPE, VOID.'Linkset');
  $metaGraph->add_resource_triple($linksetURI, VOID.'subjectsTarget', $sourceDatasetUri);
  $metaGraph->add_resource_triple($linksetURI, VOID.'objectsTarget', $targetDatasetUri);
  $metaGraph->add_literal_triple($linksetURI, VOID.'triples', $noOfTriples, null, 'http://www.w3.org/2001/XMLSchema#integer');
  $metaGraph->add_resource_triple($linksetURI, FOAF.'page', 'https://github.com/LATC/24-7-platform/tree/master/link-specifications/'.$linksetID);
  $metaGraph->add_resource_triple($linksetURI, DCT.'source', 'https://github.com/LATC/24-7-platform/tree/master/link-specifications/'.$linksetID.'/links.nt');
  $metaGraph->add_resource_triple($linksetURI, DCT.'isPartOf', LATC_Linksets_Dataset_URI);

  $lastTriple = system('tail '.$filename);
  $exampleGraph = new SimpleGraph($lastTriple);
  $exampleIndex = $exampleGraph->get_index();
  foreach($exampleIndex as $s => $ps){
    foreach($ps as $p => $os){
      $metaGraph->add_resource_triple($linksetURI, VOID.'exampleResource', $s);
      $metaGraph->add_resource_triple($linksetURI, VOID.'linkPredicate', $p);
      $metaGraph->add_resource_triple($linksetURI, VOID.'exampleResource', $os[0]['value']);
      break;
    }
    break;
  }

  $metaGraph->add_literal_triple($linksetURI, RDFS_LABEL, $linksetID);
//  $metaGraph->add_literal_triple($sourceDatasetUri, RDFS_LABEL, $sourceName);
//  $metaGraph->add_literal_triple($targetDatasetUri, RDFS_LABEL, $targetName);
  $metaGraph->add_resource_triple($sourceDatasetUri, VOID.'sparqlEndpoint', $sourceEndpoint);
  $metaGraph->add_resource_triple($sourceDatasetUri, RDF_TYPE, VOID.'Dataset');
  $metaGraph->add_resource_triple($targetDatasetUri, RDF_TYPE, VOID.'Dataset');
  $metaGraph->add_resource_triple($targetDatasetUri, VOID.'sparqlEndpoint', $targetEndpoint);

}

//$RemoteMetaGraph = new Graph(UPDATE_URI.'?graph='.urlencode('http://lod-cloud.net/latc-linksets'));
//$response = $RemoteMetaGraph->submit_turtle($metaGraph->to_turtle());

//$RemoteMetaGraph = new Store('http://api.talis.com/stores/latc-mds', new Credentials('latc', LATC_STORE_PASSWORD));
//$response = $RemoteMetaGraph->mirror_from_url(LATC_Linksets_Dataset_URI, $metaGraph->to_turtle());


file_put_contents('latc_linksets.ttl', $metaGraph->to_turtle());
//var_dump($response['update_data'], $response['success']);

//$KasabiStore = new Graph($kasabiStoreUri);
//$kasabiResponse = $KasabiStore->submit_turtle($metaGraph->to_turtle());
//var_dump($kasabiResponse->body);

?>
