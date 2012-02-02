<?php
require 'inc.php';

class VoiDGraph extends SimpleGraph {

  var $uri, $Sparql;
  var $lodTopics = array();
  var $licenseGraph = false;


  function __construct($uri,$rdf=false){
    $this->uri = $uri;
    if(!$rdf){
      $this->read_data($uri);
    }
    $this->licenceGraph = new SimpleGraph(file_get_contents('documents/licenses.ttl'));
    $this->Sparql = new SparqlService('http://mds.lod-cloud.net/sparql');
    $this->lodTopics = getLodTopics();
    parent::__construct($rdf);
  }

  function set_lodcloud_topic($lodcloudtopic){
    if(!in_array($lodcloudtopic, $this->lodTopics)){
      throw new Exception("\"{$lodcloudtopic}\" is not a topic used by the lodcloud group.");
    }
    $this->add_resource_triple($this->uri, DCT.'subject', lodThemes.$lodcloudtopic);
  }

  function get_document_url(){
    if($url = $this->get_first_resource($this->uri, FOAF.'isPrimaryTopicOf' )){
      return $url;
    } else if($docs = $this->get_subjects_of_type(FOAF.'Document')){
      return array_shift($docs);
    } else {
      return $this->uri;
    }
  }


  function get_linkset_target($linksetUri){
    if($target=$this->get_first_resource($linksetUri, VOID.'objectsTarget')){
      return $target;
    } else if($targets = $this->get_resource_triple_values($linksetUri, VOID.'target')) {
      foreach($targets as $target){
        if($target!=$this->uri){
          return $target;
        }
      }
    }
    return false;
  }

  function make_json_resource($url, $format, $description){
    return array(
      'format' => $format,
      'description' => $description,
      'url' => $url,
    );
  }


  function get_dataset_namespace(){
    if($ns = $this->get_first_literal($this->uri, VOID.'uriSpace')){
     return $ns ;
    } else { 
      if($uri = $this->get_first_resource($this->uri, VOID.'exampleResource')) {
        $uri = $uri;        
      } else {
        $uri = $this->uri;
      }

      $parts = parse_url($uri);
      extract($parts);
      if(isset($port)) $port = ':'.$port;
      else $port = '';
      return "{$scheme}://{$host}{$port}";
      
    }
  }

  function to_ckan_json(){
    
    $struct = array(
      'resources' => array(),
      'relationships' => array(),
      'tags' => array('lod'),
      'groups' => array('lodcloud'),
      'extras' => array(),
      'state' => 'active',
    );


    /* resources */

    $struct['resources'][] = $this->make_json_resource($this->uri, 'meta/void', 'VoID Dataset URI');
    $struct['resources'][] = $this->make_json_resource($this->get_sparqlendpoint($this->uri), 'api/sparql', 'SPARQL endpoint' );
    foreach($this->get_resource_triple_values($this->uri, VOID.'exampleResource') as $exampleResource){
      $struct['resources'][] = $this->make_json_resource($this->uri, 'example/rdf', 'Example Resource');
    }

    $prefixes = array_flip(getPrefixes());
    foreach($this->get_resource_triple_values($this->uri, VOID.'vocabulary') as $vocabUri){
      if(strpos($vocabUri, $this->get_dataset_namespace())){
        $struct['resources'][] = $this->make_json_resource($vocabUri, 'meta/rdf-schema', 'RDF Schema');
      } else {
        if(substr($vocabUri, -1)!='/'){
          $vocabUri.='#';
        }
        if(isset($prefixes[$vocabUri])) $struct['tags'][]='format-'.$prefixes[$vocabUri];
      }
    }

    /* extras */
    if($namespace = $this->get_first_literal($this->uri, VOID.'uriSpace')){
      $struct['extras']['namespace'] = $namespace;
    }
    if($triples = $this->get_first_literal($this->uri, VOID.'triples')){
      $struct['extras']['triples'] = $triples;
    }
    /**
     *  extras : links
     **/
    
    foreach($this->get_resource_triple_values($this->uri, VOID.'subset') as $subsetUri){
      if($this->has_resource_triple($subsetUri, RDF_TYPE, VOID.'Linkset')){
        if($target = $this->get_linkset_target($subsetUri)){
          $targetName = $this->get_dataset_package_name($target);
          $tripleCount = $this->get_first_literal($target, VOID.'triples');
          $struct['extras']['links:'.$targetName]=$tripleCount; 
        }
      }
    }

    /* tags */


    $subjects = $this->get_resource_triple_values($this->uri, DCT.'subject');
    $this->read_data($subjects);
    foreach($subjects as $subject){
      $label = $this->get_label($subject);
      $struct['tags'][]= preg_replace('/[^a-zA-Z_0-9-]/', '-', $label);
    }

   $struct['name'] = $this->get_dataset_package_name($this->uri); 
   $struct['title'] =  $this->get_label($this->uri);
   $struct['notes'] = $this->get_description($this->uri);
   $struct['url'] = $this->get_homepage($this->uri);
   if($date = $this->get_dataset_date()){
    $struct['version'] = $date;
   }
   $licenseUri = $this->get_ckan_license();
   $struct['license_id'] = $this->licenceGraph->get_first_literal($licenseUri, DCT.'identifier');
   if($creator = $this->get_first_resource($this->uri, DCT.'creator')){
     $struct['author'] = $this->get_label($creator);
     if($author_email = $this->get_first_resource($creator, FOAF.'mbox')){
       $struct['author_email'] = preg_replace('/^mailto:/','',$author_email);
     }
    
   }
   return json_encode($struct);

  }

  function get_dataset_author(){
    if($author = $this->get_first_resource($this->uri, DCT.'creator')){
      return $author;
    } else if($maker = $this->get_first_resource($this->uri, FOAF.'maker')) {
      return $maker;
    } else {
      return false;
    }

  }

  function get_dataset_date(){
    return $this->get_first_literal();
  }

  function has_provenance_metadata(){
    return (
      $this->get_dataset_author() ||
      $this->get_dataset_date()
    );
  }

  function set_dataset_package_name($name){
    $this->remove_property_values($this->uri, OPENVOCAB.'shortName');
    $this->add_literal_triple($this->uri, OPENVOCAB.'shortName', $name);
  }

  function get_ckan_license(){

    if(
      $license = $this->get_first_resource($this->uri, DCT.'license')
      AND
      $ckanLicenses = $this->get_subjects_where_resource(OWL_SAMEAS, $license)
      AND 
      !empty($ckanLicenses)
    ){
      return $ckanLicenses[0];
    } else {
      return OKFN.'notspecified';
    }

  }

  function create_package_name($uri){
    $host = parse_url($uri, PHP_URL_HOST);
    $host_parts = explode('.', $host);
    array_pop($host_parts);
    $host = array_pop($host_parts);
    return str_replace('.','-',$host).'-'.preg_replace('/\W/i','-', strtolower(trim($this->get_label($uri))));
  }

  function get_dataset_package_name($uri){
      if($packageName = $this->get_first_literal($uri, OPENVOCAB.'shortName')){
        return $packageName;
      } 
      $query = "PREFIX void: <".VOID.">
        PREFIX ov: <http://open.vocab.org/terms/> \n 
        PREFIX foaf: <http://xmlns.com/foaf/0.1/>
        SELECT ?shortname WHERE {
       \n";
      if(strpos($uri, 'http://lod-cloud.net/')){
        $query.= "<{$uri}> ov:shortName ?shortName .";
      } else if($homepage = $this->get_homepage($uri)){
        $query.=" ?dataset ov:shortName ?shortName ; foaf:homepage <{$homepage}>. ";
      } else if($endpoint = $this->get_sparqlendpoint($uri)){
        $query.="?dataset ov:shortName ?shortName ; void:sparqlEndpoint <{$endpoint}> . ";
      } else {
        return $this->create_package_name($uri);
      }
       $query .= "} LIMIT 1";
       var_dump($query); 
       die;
       $response = $this->Sparql->select($query, 'json');
       if($response->is_success()
       ){
         $results = json_decode($response->body,true);
         if(isset($results['bindings'][0])){
            return $results['bindings'][0]['shortName']['value'];
         } else {
           return $this->create_package_name($uri);
         }
       } else {
         var_dump($query, $response->body);
        return $this->create_package_name($uri);
      }
    }
  
  

  function get_homepage($uri){
    return $this->get_first_resource($uri, FOAF.'homepage');
  }

  function get_sparqlendpoint($uri){
    return $this->get_first_resource($uri, VOID.'sparqlEndpoint');
  }

  function get_first_resource($uri, $property){
    if(is_array($property)){
      foreach($property as $p){
        if($v = parent::get_first_resource($uri, $p)){
          return $v;
        }
      }
    } else {
      return parent::get_first_resource($uri,  $property);
    }
    return false;
  }


  function get_first_literal($uri, $property, $l=false, $d=false){
    if(is_array($property)){
      foreach($property as $p){
        if($v = parent::get_first_literal($uri, $p, $l, $d)){
          return $v;
        }
      }
    } else {
      return parent::get_first_literal($uri,  $property, $l, $d);
    }
    return false;
  }


}



?>
