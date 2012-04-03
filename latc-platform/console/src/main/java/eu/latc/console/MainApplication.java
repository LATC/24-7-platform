package eu.latc.console;

import java.io.IOException;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.latc.console.resources.APIKey;
import eu.latc.console.resources.Notifications;
import eu.latc.console.resources.Statistics;
import eu.latc.console.resources.TaskConfiguration;
import eu.latc.console.resources.TaskNotifications;
import eu.latc.console.resources.TaskResource;
import eu.latc.console.resources.TaskTripleSets;
import eu.latc.console.resources.Tasks;

public class MainApplication extends Application {
	// Logger instance
	protected final Logger logger = LoggerFactory.getLogger(MainApplication.class);

	// Instance of the manager for configuration files
	private ObjectManager manager = new ObjectManager();

	// Parameters for the console
	private Parameters parameters = new Parameters();

	/**
	 * Creates a root Restlet that will receive all incoming calls.
	 */
	@Override
	public Restlet createInboundRoot() {
		// Load the parameters
		try {
			parameters.loadFrom(getContext(), "war:///WEB-INF/configuration.properties");
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Create a router
		Router router = new Router(getContext());

		// Handler for login
		// GET returns an API key matching a given login/password combination
		router.attach("/api_key", APIKey.class);

		// Handler for the processing queue
		// GET returns the list of tasks
		// POST to create a new task
		router.attach("/tasks", Tasks.class);

		// GET returns the list of all notifications
		router.attach("/notifications", Notifications.class);

		// GET returns a bunch of statistics
		router.attach("/statistics", Statistics.class);

		// Handler for the configuration file associated to the task
		// GET to get the raw XML linking configuration
		// PUT to update the configuration file with a new version
		router.attach("/task/{ID}/configuration", TaskConfiguration.class);

		// Handler for the notifications
		// GET to get a sorted list of reports
		// POST to this address to save a new report
		router.attach("/task/{ID}/notifications", TaskNotifications.class);

		// Handler for the notifications
		// GET to get the content of the triple set named {NAME}
		// PUT to update or create a triple set named {NAME}
		router.attach("/task/{ID}/tripleset/{NAME}", TaskTripleSets.class);

		// Task resource
		// GET to get the description of the task
		// PUT to update the description of the task
		// DELETE to delete the task
		router.attach("/task/{ID}", TaskResource.class);

		// Activate content filtering based on extensions
		getTunnelService().setExtensionsTunnel(true);

		return router;
	}

	/**
	 * @return
	 */
	public ObjectManager getObjectManager() {
		return manager;
	}

	/**
	 * @return
	 */
	public Parameters getParameters() {
		return parameters;
	}
}
