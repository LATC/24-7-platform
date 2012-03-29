<?php
require '../scripts/inc.php';

//define('MORIARTY_ALWAYS_CACHE_EVERYTHING',1);
class VoiDGraph extends SimpleGraph {

  function getLinksetTargets($datasetUri){
    $return = array();
    foreach($this->get_subjects_of_type(VOID.'Linkset') as $subset){
      if($targets = $this->get_resource_triple_values($subset, VOID.'target')){
        foreach($targets as $target){
          if($target!=$datasetUri){
            $return[]=$target;
          }
        }
      }
    }
    return $return;
  }

}



if(isset($_GET['dataset_uri']) AND isset($_GET['example_resource'])){

  $datasetUri = trim($_GET['dataset_uri']);
  if(strpos($datasetUri, 'http://')!==0){
    header("HTTP/1.1 400 Bad Request");
    die("dataset_uri must be an http:// URI");
  }

  $exampleResource = trim($_GET['example_resource']);

  $potentialLinkTargets = getDatasetsWithSimilarEntitiesTo($datasetUri, $exampleResource);

  header("Content-type: application/json");
  echo json_encode($potentialLinkTargets);
  exit;
} else if(isset($_GET['text'])) {
  header("Content-type: application/json");
  $text = $_GET['text'];
  $output = findDatasetsMatchingText($text);  
  echo json_encode($output);
  exit;
} else {
  require 'linktarget-suggester.html';
}


function getDatasetsWithSimilarEntitiesTo($datasetUri, $exampleResource){
  $Sindice = new Sindice();
  $MDS = new MDS();
  
  $voidGraph = new VoiDGraph();
  $voidGraph->read_data($datasetUri);

  if(!$voidGraph->has_triples_about($datasetUri)){
    header("HTTP/1.1 400 Bad Request");
    die("RDF could not be fetched from {$datasetUri}");
  }
  
     $currentTargets  = $voidGraph->getLinksetTargets($datasetUri);
    $voidGraph->read_data($exampleResource);
    $label = $voidGraph->get_label($exampleResource);
    $domains = $Sindice->findDomainsMatchingSearch($label);
    $datasets = $MDS->findDatasetsMatchingDomains($domains);
    $newTargets =  array_diff(array_keys($datasets), $currentTargets); 
    $potentialLinkTargets = array(
      'example_resource' => $exampleResource,
      'dataset' => $datasetUri,
    );
    $potentialLinkTargets['new_target_suggestions'] = $datasets;
    $potentialLinkTargets['current_targets'] = $currentTargets;
    
    

  return $potentialLinkTargets;

}

?>
