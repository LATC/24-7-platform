<?php

require 'scripts/void_to_json.php';

$graph = new VoiDGraph('http://data.kasabi.com/dataset/eumida');

$json = $graph->to_ckan_json();

echo $json;

?>
