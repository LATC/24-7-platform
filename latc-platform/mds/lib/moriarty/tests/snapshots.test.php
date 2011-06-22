<?php
require_once dirname(dirname(__FILE__)) . DIRECTORY_SEPARATOR . 'constants.inc.php';
require_once MORIARTY_DIR . 'snapshots.class.php';
require_once MORIARTY_DIR . 'credentials.class.php';

class SnapshotsTest extends PHPUnit_Framework_TestCase {

	function test_get_item_uris(){

		$snapshots = new Snapshots(dirname(__FILE__).DIRECTORY_SEPARATOR.'documents/snapshots.rdf');
		$expected = array("http://api.talis.com/stores/schema-cache/snapshots/20071129173353.tar");
		$actual = $snapshots->get_item_uris();
		$this->assertEquals($expected, $actual);
	}


}
?>