/**
 * 
 */
package eu.latc.console.resource;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonConverter;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			int lastYear = 1900;
			int lastDay = 1;
			String runDate = "";
			long links = 0;
			long runtime = 0;
			int executed = 0;
			int totalRuns = 0;
			long totalTime = 0;
			int totalLinks = 0;
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
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
						logger.info("Invalid time " + data.getString("executetime"));
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

					// If we find a more recent report, reset the counters
					if (cal.get(Calendar.DAY_OF_YEAR) > lastDay && cal.get(Calendar.YEAR) >= lastYear) {
						lastDay = cal.get(Calendar.DAY_OF_YEAR);
						lastYear = cal.get(Calendar.YEAR);
						runDate = sdf.format(notification.getDate());
						links = 0;
						executed = 0;
						runtime = 0;
					}

					// If that notification corresponds to our current
					// aggregator, count it
					if (cal.get(Calendar.DAY_OF_YEAR) == lastDay && cal.get(Calendar.YEAR) == lastYear
							&& data.getLong("size") > 0) {
						links += execLinks;
						runtime += execTime;
						executed++;
					}
				}
			}

			// The object requested is the list of configuration files
			DecimalFormat format = new DecimalFormat("########.00");

			JSONObject json = new JSONObject();
			json.put("queue_size", manager.getTasks(0, true).size());
			json.put("tasks_size", manager.getTasks(0, false).size());
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
