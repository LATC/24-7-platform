/**
 * 
 */
package eu.latc.console.resources;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonConverter;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import eu.latc.console.MainApplication;
import eu.latc.console.ObjectManager;
import eu.latc.console.objects.Notification;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class Statistics extends ServerResource {
	// Logger instance
	protected final Logger logger = LoggerFactory.getLogger(Statistics.class);

	/**
	 * Return the list of tasks
	 */
	@Get("json")
	public Representation toJSON() {
		logger.info("[GET-JSON] Return statistics");

		try {
			// Get access to the entity manager stored in the app
			ObjectManager manager = ((MainApplication) getApplication()).getObjectManager();

			// Go through all the notifications to get stats for the latest run
			String runDate = "";
			long links = 0;
			long runtime = 0;
			int executed = 0;
			int totalRuns = 0;
			long totalTime = 0;
			int totalLinks = 0;

			// Count the internal notifications
			for (Notification notification : manager.getNotifications(0)) {
				// Skip notification without payload
				if (notification.getData().equals(""))
					continue;

				// Only consider notifications about run execution
				JSONObject data = new JSONObject(notification.getData());
				if (data.has("size") && data.has("executetime")) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(notification.getDate());

					// Get the time in second
					long execTime = 0;
					String[] s = data.getString("executetime").split(":");
					if (s.length == 4) {
						execTime += Integer.parseInt(s[0]) * 24 * 60 * 60;
						execTime += Integer.parseInt(s[1]) * 60 * 60;
						execTime += Integer.parseInt(s[2]) * 60;
						execTime += Integer.parseInt(s[3]);
					} else {
						logger.error("Invalid time " + data.getString("executetime"));
					}

					// Get the number of links created
					long execLinks = data.getLong("size");

					// Increase the counters for run executions, only count when
					// links where produced
					if (data.getLong("size") > 0) {
						totalRuns++;
						totalTime += execTime;
						totalLinks += execLinks;
					}
				}
			}

			// Query the MDS
			String req = "select (SUM(?t) as ?total) where {";
			req += "?s a <http://rdfs.org/ns/void#Linkset>.";
			req += "?s a <http://purl.org/net/provenance/ns#DataItem>.";
			req += "?s <http://rdfs.org/ns/void#triples> ?t.";
			req += "filter (?t > 0)}";

			// Send the query
			String sparqlQuery = URLEncoder.encode(req, "utf-8");
			StringBuffer urlString = new StringBuffer("http://mds.lod-cloud.net/sparql");
			urlString.append("?query=").append(sparqlQuery);
			URL url = new URL(urlString.toString());
			StringBuffer response = new StringBuffer();
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();

			// Parse the response
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(response.toString()));
			Document doc = db.parse(is);
			NodeList results = doc.getElementsByTagName("literal");
			Element element = (Element) results.item(0);
			int count = Integer.parseInt(element.getTextContent());
			totalLinks += count;

			// The object requested is the list of configuration files
			DecimalFormat format = new DecimalFormat("########.00");

			JSONObject json = new JSONObject();
			json.put("queue_size", manager.getTasks(0, true, true).size());
			json.put("tasks_size", manager.getTasks(0, false, false).size());
			json.put("total_runs", totalRuns);
			json.put("total_links", totalLinks);
			if (totalRuns > 0) {
				json.put("avg_time_per_run", format.format((double) (totalTime) / (double) (totalRuns)));
				json.put("avg_links_per_run", format.format((double) (totalLinks) / (double) (totalRuns)));
			} else {
				json.put("avg_time_per_run", 0);
				json.put("avg_links_per_run", 0);
			}
			json.put("last_run_size", links);
			json.put("last_run_time", runtime);
			json.put("last_run_date", runDate);
			json.put("last_executed", executed);
			JsonConverter conv = new JsonConverter();
			return conv.toRepresentation(json, null, null);
		} catch (Exception e) {
			e.printStackTrace();

			// If anything goes wrong, just report back on an internal error
			setStatus(Status.SERVER_ERROR_INTERNAL);
			return null;
		}
	}

}
