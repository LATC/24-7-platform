/**
 * 
 */
package eu.latc.console.tests;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.PostMethod;

import eu.latc.console.resources.APIKey;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class PostTripleSet {
	// Where the application is deployed
	// static String HOST = "http://127.0.0.1:58080/LATC-console/";
	static String HOST = "http://latc-console.few.vu.nl/";

	// The identifier of the configuration to send a notification about
	// static String ID = "ff8081812e2e36ce012e2e36cf7c0002";
	static String ID = "ff80808130b290420130b290421e0000";

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// Prepare the message
		// "triples" is the set of triples as raw text
		// "api_key" is the API key
		NameValuePair[] request = { new NameValuePair("triples", "s p o. s2 p2 o2."),
				new NameValuePair("api_key", APIKey.KEY) };

		// Prepare the query
		String name = "good";

		String URI = HOST + "api/task/" + ID + "/tripleset/" + name;
		System.out.println(URI);
		PostMethod post = new PostMethod();
		post.setURI(new URI(URI, false));
		post.setRequestBody(request);

		// Issue the POST
		HttpClient clientService = new HttpClient();
		int status = clientService.executeMethod(post);

		// Check response code
		if (status != HttpStatus.SC_OK) {
			throw new Exception("Received error status " + status);
		}
	}
}
