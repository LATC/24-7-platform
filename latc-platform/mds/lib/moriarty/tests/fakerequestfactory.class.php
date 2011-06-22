<?php
require_once dirname(dirname(__FILE__)) . DIRECTORY_SEPARATOR . 'constants.inc.php';
require_once MORIARTY_DIR . 'httprequestfactory.class.php';

class FakeRequestFactory extends HttpRequestFactory {
  var $_requests;
  var $_received = array();
  function __construct() {
    $this->_requests = array();
    $this->_received = array();
  }

  function register($method, $uri, $request ) {
    $this->_requests[$method . ' ' . $uri] = $request;
  }

  function make( $method, $uri, $credentials = null) {
    $this->_received[] = $method . ' ' . $uri;
    
    if (array_key_exists( $method . ' ' . $uri, $this->_requests) ) {
      $request = $this->_requests[$method . ' ' . $uri];
      if ( $credentials != null) {
        $request->set_auth( $credentials->get_auth());
      }
      return $request;
    }

    $response = new HttpResponse();
    $response->status_code = 404;
      
    return new FakeHttpRequest( $response );
  }

  function dump_received() {
    foreach ($this->_received as $received) {
      echo $received, "\n"; 
    } 
  }
}
?>
