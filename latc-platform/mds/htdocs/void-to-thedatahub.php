<?php
//set_include_path(get_include_path().':../../');
require '../scripts/void_to_json.php';

if(isset($_POST['voidUri']) && isset($_POST['apikey'])){

  $voidUri = trim($_POST['voidUri']);
  $package = !empty($_POST['name'])? $_POST['name'] : false;
  $g = new VoiDGraph($voidUri);
  $package_name = $g->get_dataset_package_name($g->uri);
  $json = $g->to_ckan_json($package);

 /* 
   $fromvoid = json_decode($json,1);
   $current = json_decode(file_POST_contents('http://thedatahub.org/api/rest/package/'.$package_name),1);
   $json = json_encode(array_merge_recursive($fromvoid,$current));
   header("Content-type: application/json");
   die($json);
*/

  $base = 'http://thedatahub.org/api/rest/dataset';
  $datasetUri = $base.'/'.$package_name;
  if($existingJson = file_get_contents($datasetUri)){
    $targetUri = $datasetUri;
  } else {
    $targetUri = $base;
  }
  $http = new HttpRequestFactory();
  $post = $http->make('POST', $targetUri);
  $post->headers["Authorization"] = $_POST['apikey'];
  $post->set_body($json);
  $post->set_content_type('application/json');
  $response = $post->execute();
  if($response->is_success()){
    header("Location: http://thedatahub.org/dataset/{$package_name}");
    exit;
  } else {
    echo $response->body;
    echo "<hr> <h1> JSON sent was: </h1> <pre>";
    echo htmlentities($json);
    echo "</pre>";
    }
  
} else {
  $licenseGraph = new SimpleGraph(file_get_contents('../documents/licenses.ttl'));
  $licenses = array();
  foreach($licenseGraph->get_subjects() as $s){
      $rs = $licenseGraph->get_resource_triple_values($s,OWL_SAMEAS);
      $licenses = array_merge( $rs, $licenses);
    
  }
?>
<!DOCTYPE HTML>
<html>
<head>
  <meta http-equiv="content-type" content="text/html; charset=utf-8">
  <title>Import VoID data to thedatahub.org </title>
  <link rel="stylesheet" href="/css/bootstrap.css" type="text/css" media="screen" charset="utf-8">
  <link rel="stylesheet" href="/css/latc.css" type="text/css" media="screen" charset="utf-8">
</head>
<body>
  <div class="container">
    <h1>VoID Importer for TheDataHub.Org</h1>
    <form action="" method="post" accept-charset="utf-8">
      <div>
        <div>
          <label for="voidUri">VoID URI </label>
          <input type="url" name="voidUri" value="">
          <p>The dereferencable URI of your VoID Dataset</p>
        </div>
       <div>
          <label for="apikey">API Key</label>
          <input type="text" name="apikey" value="" id="apikey">
          <p>You can see your API key when you log in to <a href="http://thedatahub.org/en/user/me">your account on TheDataHub</a> </p>
        </div>
        
         <div>
          <label for="name">Dataset short name</label>
          <input type="text" name="name" id="name" value="">
          <p>
            (optional, but recommended: this service will autogenerate a short name, but will not check that it does not already exist. If the shortname exists, the dataset record will be overwritten.)
          </p>
        </div>
 
        <input type="submit" value="Go &rarr;">
      </div>
    </form>
    <div>
      <h2>Tips for creating a TheDataHub-friendly VoID document for your Dataset</h2>

<p>
  (Examples below are given as SPARQL patterns - in your own document, replace the variables with the appropriate URIs)
 </p>

      <h3>Link between the Dataset and the VoID Document</h3>
        <code>
        <pre> 
 ?datasetUri a void:Dataset ;
  foaf:isPrimaryTopicOf ?documentUrl .

?documentUrl a foaf:Document .  
        </pre>
        </code>        

        <h3>Give your Dataset a Compatible License</h3>

<code><pre>
  ?datasetUri dct:license ?licenseUri .
</pre></code>
          <p> One of:</p>
        <ul>
        <?php foreach($licenses as $licenseUri):?>
        <li><code><?php echo $licenseUri?></code></li>
        <?php endforeach?>
        </ul>

  <h3>Categorise your Dataset</h3>

<code>
<pre>
  ?datasetUri dct:subject ?topicUri .
</pre>
</code>
<p> Where ?topicUri is one of:</p>
<ul>
<?php
  $topics = getLodTopics();
  foreach($topics as $topic):?>
    <li> <code>http://lod-cloud.net/themes/<?php echo $topic ?></code></li>
<?php endforeach ?>
</ul>

<h3>Short Name</h3>
<p>To determine the short name TheDataHub uses for your dataset, you can add to your VoID document:</p>

<code><pre>?datasetUri &lthttp://open.vocab.org/terms/shortName&gt ?shortName . </pre></code>

<p>(Where <code>?shortName</code> might be <strong><samp>dbpedia</samp></strong>).</p>

    </div>
  </div>
</body>
</html>

<?php

}

?>
