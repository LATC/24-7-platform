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
    $this->licenceGraph = new SimpleGraph(file_get_contents((dirname(__FILE__)).'/../documents/licenses.ttl'));
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

  function to_ckan_json($package_name=false){
    
    $struct = array(
      'resources' => array(),
      'relationships' => array(),
      'tags' => array('lod', 'lodcloud.candidate'),
      'groups' => array(),
      'extras' => array(),
      'state' => 'active',
    );


    /* resources */

    $struct['resources'][] = $this->make_json_resource($this->uri, 'meta/void', 'VoID Dataset URI');
    $struct['resources'][] = $this->make_json_resource($this->get_sparqlendpoint($this->uri), 'api/sparql', 'SPARQL endpoint' );
    foreach($this->get_resource_triple_values($this->uri, VOID.'exampleResource') as $exampleResource){
      $struct['resources'][] = $this->make_json_resource($exampleResource, 'example/rdf', 'Example Resource');
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

    /* voiD literals */

    foreach(array('classes', 'entities','triples', 'properties', 'distinctSubjects', 'distinctObjects', 'documents', 'uriRegexPattern') as $literalProp){
      if($val = $this->get_first_literal($this->uri, VOID.$literalProp)){
        $struct['extras'][$literalProp] = $val;
      }
    }
    


    /**
     *  extras : links
     **/
    $nolinks = true; 
    foreach($this->get_resource_triple_values($this->uri, VOID.'subset') as $subsetUri){
      if($this->has_resource_triple($subsetUri, RDF_TYPE, VOID.'Linkset')){
        if($target = $this->get_linkset_target($subsetUri)){
          $targetName = $this->get_dataset_package_name($target);
          $tripleCount = $this->get_first_literal($target, VOID.'triples');
          $struct['extras']['links:'.$targetName]=$tripleCount; 
          $nolinks = false;
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

    /* tags: provenance */

    $provenanceTag = $this->has_provenance_metadata()? 'provenance-metadata' : 'no-provenance-metadata';
    $struct['tags'][]=$provenanceTag;

    /* tags: license */

    $license = $this->get_license();
    if($license){
        $struct['tags'][]='license-metadata';
    } else {
      $struct['tags'][]='no-license-metadata';
    }

    /* links */
    if($nolinks){
      $struct['tags'][]='lodcloud.nolinks';
    }

   $struct['name'] = $package_name ? $package_name : $this->get_dataset_package_name($this->uri); 
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
    return $this->get_first_resource($this->uri, array(DCT.'creator', FOAF.'maker'));
  }

  function get_dataset_date(){
    return $this->get_first_literal($this->uri, array(
      DCT.'modified',
      DC.'modified',
      DCT.'date',
      DC.'date',
      DCT.'created',
      DC.'created',
    ));
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

  function get_license(){
    return $this->get_first_resource($this->uri, array(DCT.'license', DCT.'rights'));
  }

  function get_ckan_license(){

    if(
      $license = $this->get_license()
      AND
      $ckanLicenses = $this->licenceGraph->get_subjects_where_resource(OWL_SAMEAS, $license)
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
      } else {
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
