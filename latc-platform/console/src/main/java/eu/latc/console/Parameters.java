/**
 * 
 */
package eu.latc.console;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class Parameters {
	// Logger instance
	protected final Logger logger = LoggerFactory.getLogger(Parameters.class);

	private final Properties properties = new Properties();

	/**
	 * @param context
	 * @param string
	 * @throws IOException
	 */
	public void loadFrom(Context context, String location) throws IOException {
		// Get the internal client
		Client client = context.getClientDispatcher();

		// Load the properties file
		Response response = client.handle(new Request(Method.GET, location));
		properties.load(IOUtils.toInputStream(response.getEntityAsText()));
	}

	/**
	 * @param string
	 * @return
	 */
	public String get(String key) {
		return properties.getProperty(key);
	}
}
