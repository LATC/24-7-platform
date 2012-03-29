<?php 
require '../scripts/inc.php';

if(isset($_GET['text'])) {
  header("Content-type: application/json");
  $text = $_GET['text'];
  $output = findDatasetsMatchingText($text);  
  echo json_encode($output);
  exit;
} else {
  require 'dataset-finder.html';
}

?>
