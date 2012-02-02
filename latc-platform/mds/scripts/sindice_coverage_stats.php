<?php
require 'inc.php';
$store = new Store(LATC_STORE_URI);

$results = $store->get_sparql_service()->select_to_array('SELECT DISTINCT ?dataset ?namespace { ?dataset <'.VOID.'uriSpace> ?namespace }');
foreach($results as $row){
  $namespace = $row['namespace']['value'];

}


?>
