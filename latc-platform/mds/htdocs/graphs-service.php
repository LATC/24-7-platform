<?php
define('MORIARTY_ARC_DIR', '../lib/arc/');
require '../lib/moriarty/store.class.php';
require '../lib/moriarty/credentials.class.php';
require '../talis-store-credentials.php';


class MDSHttpRequest {


  var $body=false;
  var $contentType = 'text/turtle';
  var $method = false;
  var $acceptableContentTypes = array('text/turtle');
  var $params;

  function __construct(){
    $this->body = file_get_contents('php://input');
    @$this->contentType = $_SERVER['CONTENT_TYPE'];
    $this->method = $_SERVER['REQUEST_METHOD'];
    $this->params = array_merge($_GET, $_POST);
  }

  function isAcceptable(){
    
    return (in_array($this->contentType, $this->acceptableContentTypes) );
  
  }

  function isAuthenticated(){
    require '../api-keys.php';
    return (isset($this->params['apiKey']) and in_array($this->params['apiKey'], $apiKeys));
  }

  function getGraphUri(){
    if(isset($this->params['graph'])){
      return $this->params['graph'];
    } else {
      return false;
    }
  }

  function hasDefault(){
    return isset($this->params['default']);
  }

  function getBody(){
    return $this->body;
  }

}

$request =  new MDSHttpRequest();

if(!$request->isAuthenticated()){
  header("HTTP/1.1 401");
  echo "?apiKey= parameter is required";
  exit;
}
if($request->hasDefault()){
  header("HTTP/1.1 403 Forbidden");
  echo "You do not have permission for the default graph";
  exit;
}
if(!$graphUri = $request->getGraphUri()){
  header("HTTP/1.1 400 Bad Request");
  echo "Please specify a ?graph={uri} parameter";
  exit;
}

$store = new Store(LATC_STORE_URI, new Credentials('latc', LATC_STORE_PASSWORD));


if($request->method=='PUT'){
  if($request->isAcceptable()){
     if($request->contentType == 'text/turtle'){
       $response = $store->mirror_from_url($graphUri, $request->body);
       if($response['success']){
        header("HTTP/1.1 202 Accepted");
        exit;
       } else {
        header("HTTP/1.1 500 Internal Server Error");
        exit;
       }

     } else {
       header("HTTP/1.1 400 Bad Request");
       exit;
     }
   
  } else {
    header("HTTP/1.1 406 Not Acceptable");
    echo("Content-type not acceptable. Acceptable content types are: ". implode(',', $request->acceptableContentTypes));
    exit;
  }
} else if($request->method=='GET'){
  header("location: ".LATC_STORE_URI.'/items/mirrors/'.$graphUri, true, 302);
  exit;
} else if($request->method=='DELETE'){
  $response = $store->mirror_from_url($graphUri, '#empty RDF');
  if(!$response['get_copy']->is_success()){
    header("HTTP/1.1 404 Not Found");
  } else if($response['success']){
    header("HTTP/1.1 204 No Content");
    exit;
  } else {
    header("HTTP/1.1 500 Internal Server Error");
    exit;
  }
} else {
  header("HTTP/1.1 405 Method Not Allowed");
  echo "At this time, only HTTP PUT, DELETE and GET are supported.";
  exit;
}

?>
