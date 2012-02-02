<?php

define('BASE_DIR', realpath(dirname(dirname(__FILE__))));
define('LIB_DIR', BASE_DIR.'/lib/');
define('MORIARTY_HTTP_CACHE_DIR', BASE_DIR.'/cache/');
define('MORIARTY_ARC_DIR', LIB_DIR.'arc/');
define('VOID', 'http://rdfs.org/ns/void#');
define('OPENVOCAB', 'http://open.vocab.org/terms/');
define('DCT', 'http://purl.org/dc/terms/');
define('DC', 'http://purl.org/dc/elements/1.1/');
define('VANN', 'http://purl.org/vocab/vann/');
define('xsd', 'http://www.w3.org/2001/XMLSchema#');
define('FOAF', 'http://xmlns.com/foaf/0.1/');
define('MOAT', 'http://moat-project.org/ns#');
define('DSI', 'http://dsi.lod-cloud.net/vocab#');
define('LATC_Linksets_Dataset_URI', 'http://lod-cloud.net/latc-linksets');
define('CC', 'http://creativecommons.org/ns#');
define('OKFN', 'http://purl.org/okfn/licenses/');
define('DOAP', 'http://usefulinc.com/ns/doap#');
define('SSD', 'http://www.w3.org/ns/sparql-service-description#');

require_once LIB_DIR.'/moriarty/store.class.php';
require_once LIB_DIR.'/moriarty/credentials.class.php';
require_once LIB_DIR.'/moriarty/httprequestfactory.class.php';
require_once LIB_DIR.'/moriarty/sparqlservice.class.php';
require_once LIB_DIR.'/moriarty/simplegraph.class.php';

require BASE_DIR.'/talis-store-credentials.php';
define('LOD', 'http://lod-cloud.net/');

define('lodThemes', LOD.'themes/');


function getLodTopics(){

    return   array(
  'geographic',
  'government',
  'media',
  'crossdomain',
  'lifesciences',
  'usergeneratedcontent',
  'ecommerce',
  'schemata',
);


}
function getPrefixes(){
  $RequestFactory = new HttpRequestFactory();
  $Prefixes = json_decode($RequestFactory->make('GET', 'http://prefix.cc/popular/all.file.json')->execute()->body, 1);
  $Prefixes = array_filter($Prefixes, create_function('$a', 'return $a;'));
  return $Prefixes;
}

function ckanJsonToRDF( $json){
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

    $graph = new SimpleGraph();
  
    $ckanArray = json_decode($json, true);
    $packageName = $ckanArray['name'];
    $uri = LOD.'dataset/'.$packageName;

    $graph->add_resource_triple($uri, RDF_TYPE, VOID.'Dataset');
    $graph->add_literal_triple($uri, OPENVOCAB.'shortName', $packageName);
    $graph->add_literal_triple($uri, RDFS_LABEL, $ckanArray['title'], 'en');
    $graph->add_literal_triple($uri, DCT.'description', $ckanArray['notes'], 'en');
    $graph->add_resource_triple($uri, FOAF.'page', $ckanArray['ckan_url']);
    $graph->add_resource_triple($uri, FOAF.'homepage', $ckanArray['url']);
    $graph->add_literal_triple($ckanArray['ckan_url'], DCT.'modified', substr($ckanArray['metadata_modified'],0,18), false, xsd.'dateTime');
    if(isset($ckanArray['download_url'])) $graph->add_resource_triple($uri, VOID.'dataDump', $ckanArray['download_url']);

    if(isset($ckanArray['author_email'])){
      $authorUri = LOD.'agents/'.sha1($ckanArray['author_email']);
      $graph->add_resource_triple($authorUri, OWL_SAMEAS, 'http://rdfize.com/people/'.sha1($ckanArray['author_email']));
      $graph->add_resource_triple($authorUri, FOAF.'mbox', 'mailto:'.$ckanArray['author_email']);
    } else {
      $authorUri = $uri.'/creator';
    }
    if(isset($ckanArray['author'])){
      $graph->add_literal_triple($authorUri, RDFS_LABEL, $ckanArray['author']);
    }
    $graph->add_resource_triple($uri, DCT.'creator', $authorUri);

  if(isset($ckanArray['maintainer_email'])){
      $maintainerUri = LOD.'agents/'.sha1($ckanArray['maintainer_email']);
      $graph->add_resource_triple($maintainerUri, OWL_SAMEAS, 'http://rdfize.com/people/'.sha1($ckanArray['author_email']));
      $graph->add_resource_triple($maintainerUri, FOAF.'mbox', 'mailto:'.$ckanArray['author_email']);
    } else {
      $maintainerUri = $uri.'/maintainer';
    }

    if(isset($ckanArray['maintainer'])){
      $graph->add_literal_triple($authorUri, RDFS_LABEL, $ckanArray['maintainer']);
    }
    $graph->add_resource_triple($uri, DSI.'maintainer', $maintainerUri);

    $graph->add_literal_triple($uri, DSI.'ckanID', $ckanArray['id']);

    if(isset($ckanArray['extras'])){
      if(isset($ckanArray['extras']['uriSpace'])){
        $graph->add_literal_triple($uri, VOID.'uriSpace', $ckanArray['extras']['uriSpace']);
      }
      else if(isset($ckanArray['extras']['namespace'])){
        $graph->add_literal_triple($uri, VOID.'uriSpace', $ckanArray['extras']['namespace']);
      }

      if(isset($ckanArray['extras']['sparql_graph_name'])){
        $graph->add_literal_triple($uri, SSD.'name', $ckanArray['extras']['sparql_graph_name']);
      }
    

      foreach(array('classes', 'entities','triples', 'properties', 'distinctSubjects', 'distinctObjects', 'documents') as $statProp){
        if(isset($ckanArray['extras'][$statProp])){
          $graph->add_literal_triple($uri, VOID.$statProp, intval($ckanArray['extras'][$statProp]), '',xsd.'integer' );
        }
      }

      foreach($ckanArray['extras'] as $key => $value){
        if(strpos($key, 'links:')===0){
          $targetDatasetName = substr($key, 6);
          $linksetUri = $uri.'/links-'.$targetDatasetName;
          $graph->add_resource_triple($uri, VOID.'subset', $linksetUri);
          $graph->add_resource_triple($linksetUri, RDF_TYPE, VOID.'Linkset');
          $graph->add_resource_triple($linksetUri, VOID.'target', LOD.$targetDatasetName);
          $graph->add_resource_triple($linksetUri, VOID.'target', $uri);
          $graph->add_literal_triple($linksetUri, VOID.'triples', $value, 0, xsd.'integer');
        }
      }
 
    }
  if(isset($ckanArray['title'])){
//        $graph->add_literal_triple($uri, RDFS_LABEL,  $ckanArray['title']);
    }

    if(isset($ckanArray['resources'])){
      foreach($ckanArray['resources'] as $resource){
        if($resource['format'] == 'meta/rdf-schema'){
          $graph->add_resource_triple($uri, VOID.'vocabulary', rtrim($resource['url'], '#'));
        }
        if($resource['format'] == 'meta/void'){
          $graph->add_resource_triple($uri, RDFS_SEEALSO, $resource['url']);
        }
        if($resource['format'] == 'api/sparql'){
          $graph->add_resource_triple($uri, VOID.'sparqlEndpoint', $resource['url']);
        }
         if(strpos($resource['format'],'example/')==0){
          $graph->add_resource_triple($uri, VOID.'exampleResource', $resource['url']);
        }

        foreach(array('text/turtle','application/rdf+xml') as $contentType){
          if($resource['format']==$contentType){
            $graph->add_resource_triple($uri, VOID.'dataDump', $resource['url']);
            $graph->add_literal_triple($resource['url'], DC.'format', $contentType);
            $graph->add_literal_triple($resource['url'], DCT.'description', $resource['description']);
          }      
        }
 
      }
    }

    
    foreach($ckanArray['tags'] as $tag){
      if(strpos($tag, 'format-')===0){
        $prefix = str_replace('format-', '', $tag);
        if(isset($Prefixes[$prefix])){
          $vocabUri = rtrim($Prefixes[$prefix], '#');
          $graph->add_literal_triple($vocabUri, RDFS_LABEL, $prefix);
          $graph->add_literal_triple($vocabUri, VANN.'preferredNamespacePrefix', $prefix);
          $graph->add_literal_triple($vocabUri, VANN.'preferredNamespaceUri', $Prefixes[$prefix]);
          $graph->add_resource_triple($uri, VOID.'vocabulary', $vocabUri);
        }
      } else if(in_array($tag, $lodTopics)) {
          $graph->add_resource_triple($uri, DCT.'subject', lodThemes.$tag);
      } else {
        $tagUri = LOD.'tag/'.$tag;
        $graph->add_resource_triple($uri, MOAT.'taggedWith', $tagUri);
        $graph->add_literal_triple($tagUri, RDFS_LABEL, $tag);
        $graph->add_literal_triple($tagUri, MOAT.'name', $tag);
        $graph->add_resource_triple($tagUri, RDF_TYPE, MOAT.'Tag');
      }
      
    }

    if(isset($ckanArray['license_id'])){
      $licenseUri = LOD.'licenses/'.$ckanArray['license_id'];
      $graph->add_resource_triple($licenseUri, RDF_TYPE, CC.'License');
      $graph->add_resource_triple($uri, DCT.'rights', $licenseUri);
      if(empty($ckanArray['license'])){
        $ckanArray['license']='No License';
      }
      $graph->add_literal_triple($licenseUri, RDFS_LABEL, $ckanArray['license']);

    }

  return $graph->to_turtle();

}


?>
