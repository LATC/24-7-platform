<?php
require 'inc.php';


$lodTopics = getLodTopics();
  


$latcStore = new Store(LATC_STORE_URI, new Credentials('latc', LATC_STORE_PASSWORD));

$SparqlEndpoint = new SparqlService("http://semantic.ckan.net/sparql/");


$Prefixes = getPrefixes();

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
  {
    ?s <http://moat-project.org/ns#taggedWithTag> <http://ckan.net/tag/lod> .
  } UNION 
  {
    ?s <http://moat-project.org/ns#taggedWith> <http://ckan.net/tag/lod> .
  }
   {$modifiedClause}
}
LIMIT {$pageSize} OFFSET {$offset}
_SPARQL_;

echo "Running SPARQL query for recently changed datasets \n";
$requests = array();
$results = $SparqlEndpoint->select_to_array($query);

$count = count($results); 

echo "Got $count results \n";

$RequestFactory = new HttpRequestFactory();

$vocabGraph = new SimpleGraph();
foreach($results as $row){
  $uri = $row['s']['value'];
  $jsonUrl = str_replace('/dataset/','/api/rest/package/',$uri);
  $json = file_get_contents($jsonUrl);
  $rdf = ckanJsonToRDF($json);
  $packageName = array_pop(explode('/',$uri));
  $graphName = 'http://lod-cloud.net/'. $packageName;

   for ($i = 0; $i < 5; $i++) {
      // try five times
      $response =   $latcStore->mirror_from_url($graphName, $rdf);
      if($response['get_copy']->status_code=='200'){
        echo "\n Copy of {$uri} found in content box. ";
      } else if($response['get_copy']->status_code=='404') {
        echo "\n Copy of {$uri} not found in content box";
      } else {
        echo "\n there was a problem retrieving a copy of {$uri} from the content box";
      }
      if($response['update_data']){
        switch($response['update_data']->is_success()){
          case true:
            echo "\n triples from {$uri} were modified successfully";
            break;
          case false:
            echo "\n there was a problem modifying the triples from {$uri}";
            var_dump($response['update_data']);
        }
      }

      if($response['success']){
        echo "\n {$uri} mirrored to triple store. \n";
        break;
      }  
    }
    if(!$response['success']){
      error_log(date('c')."\t{$uri} was not mirrored to the triple store.\n", 3, 'import_errors.log');
      file_put_contents('failed_import.json', json_encode($response));
    } 
}

  




  if(count(array_keys($results)) < $pageSize){
    $continue = false;
  } else {
    $offset+=$pageSize;
  }
}
?>
