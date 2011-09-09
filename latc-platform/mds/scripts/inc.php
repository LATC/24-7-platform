<?php

define('BASE_DIR', realpath(dirname(dirname(__FILE__))));
define('LIB_DIR', BASE_DIR.'/lib/');
define('MORIARTY_HTTP_CACHE_DIR', BASE_DIR.'/cache/');
define('MORIARTY_ARC_DIR', LIB_DIR.'arc/');
define('VOID', 'http://rdfs.org/ns/void#');
define('OPENVOCAB', 'http://open.vocab.org/terms/');
define('DCT', 'http://purl.org/dc/terms/');
define('VANN', 'http://purl.org/vocab/vann/');
define('xsd', 'http://www.w3.org/2001/XMLSchema#');
define('FOAF', 'http://xmlns.com/foaf/0.1/');
define('LATC_Linksets_Dataset_URI', 'http://lod-cloud.net/latc-linksets');



require_once LIB_DIR.'/moriarty/store.class.php';
require_once LIB_DIR.'/moriarty/credentials.class.php';
require_once LIB_DIR.'/moriarty/httprequestfactory.class.php';
require_once LIB_DIR.'/moriarty/sparqlservice.class.php';
require_once LIB_DIR.'/moriarty/simplegraph.class.php';

require BASE_DIR.'/talis-store-credentials.php';
define('LOD', 'http://lod-cloud.net/');
define('DSI', 'http://dsi.lod-cloud.net/vocab#');

?>
