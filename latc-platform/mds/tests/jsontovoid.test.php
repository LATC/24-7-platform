<?php

require '../scripts/inc.php';
$json = file_get_contents('../documents/sample-ckan.json');

echo ckanJsonToRdf($json);

?>
