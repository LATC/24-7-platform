<?php

require 'scripts/void_to_json.php';

$graph = new VoiDGraph('http://data.kasabi.com/dataset/ecco-tcp-eighteenth-century-collections-online-texts');
//$graph->set_dataset_package_name('linkedscotland-sns');
$json = $graph->to_ckan_json();

echo $json;

?>
