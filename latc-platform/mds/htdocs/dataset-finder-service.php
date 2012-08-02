<?php 
require '../scripts/inc.php';

if(isset($_GET['text'])) {
  header("Content-type: application/json");
  header("Access-Control-Allow-Origin: *");
  $text = $_GET['text'];
  $output = findDatasetsMatchingText($text);  
  echo json_encode($output);
  exit;
} else {
  require 'dataset-finder.html';
}

?>
