<?php
define('MORIARTY_ARC_DIR', '../lib/arc/');
require '../lib/moriarty/store.class.php';
require '../lib/moriarty/credentials.class.php';
require 'talis-store-credentials.php';

class MDSHttpRequest {


  var $body=false;
  var $contentType = 'text/turtle';
  var $method = false;
  var $acceptableContentTypes = array('text/turtle');

  function __construct(){
    $this->body = file_get_contents('php://input');
    @$this->contentType = $_SERVER['CONTENT_TYPE'];
    $this->method = $_SERVER['REQUEST_METHOD'];
  }

  function isAcceptable(){
    
    return (in_array($this->contentType, $this->acceptableContentTypes) AND !empty($this->body));
  
  }

}

$request =  new MDSHttpRequest();

if($request->method=='POST'){
  if($request->isAcceptable()){
    $store = new Store('http://api.talis.com/stores/latc-mds', new Credentials('latc', LATC_STORE_PASSWORD));
     if($request->contentType == 'text/turtle'){
       $response = $store->get_metabox()->submit_turtle($request->body);
     } else {
       header("HTTP/1.1 400 Bad Request");
       die;
     }
    if($response->is_success()){
      $cohodoSparqlURI = 'http://db.cohodo.net/updates/direct/713afe73-09da-4603-bff1-f6dc84315828';
      $updateLinksetURI = $cohodoSparqlURI.'?graph='.urlencode(GRAPH_URI);
      $requestFactory = new HttpRequestFactory();
      $postRequest = $requestFactory->make('POST', $updateLinksetURI);
      $postRequest->set_body($request->body);
      $postRequest->set_content_type('text/turtle');
      $postResponse = $postRequest->execute();
      if( $postResponse->is_success()){
          header("HTTP/1.1 {$postResponse->status_code}");
          header("Content-type: application/json");
          echo $postResponse->body;
          die;
      } else {
        header("HTTP/1.1 {$postResponse->status_code}");
        echo ($postResponse->body);
        die;
      }
}
 
    header("HTTP/1.1 {$response->status_code}");
    echo $response->body;
    die;
    
  } else {
    header("HTTP/1.1 406 Not Acceptable");
    die("Content-type not acceptable. Acceptable content types are: ". implode(',', $request->acceptableContentTypes));
  }

} else {
  header("HTTP/1.1 405 Method Not Allowed");
  echo "At this time, only the HTTP POST method is supported.";
  die;
}

?>
