/**
 * 
 */
package eu.latc.console.resource;

import java.util.Date;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

import eu.latc.console.MainApplication;
import eu.latc.console.ObjectManager;
import eu.latc.console.objects.Notification;
import eu.latc.console.objects.TripleSet;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class TaskTripleSets extends TaskResource {
	// The name of the triple set considered
	private String triplesetName;

	// The triple set associated to that name, for this task
	private TripleSet tripleSet;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.restlet.resource.UniformResource#doInit()
	 */
	@Override
	protected void doInit() throws ResourceException {
		// Initialise the task
		super.doInit();

		// Get the "NAME" attribute value taken from the URI template /{NAME}.
		triplesetName = (String) getRequest().getAttributes().get("NAME");

		// If no NAME has been given, return a 404
		if (triplesetName == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			setExisting(false);
		}

	}

	/**
	 * Return the triple set as a raw text file
	 * 
	 */
	@Override
	@Get
	public Representation get() {
		// Try to get the triple set
		ObjectManager manager = ((MainApplication) getApplication()).getObjectManager();
		try {
			tripleSet = manager.getTripleSetForTask(taskID, triplesetName);
		} catch (Exception e) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			setExisting(false);
		}

		// If no matching triple set has been found, return a 404
		if (tripleSet == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			setExisting(false);
		}

		try {
			// A specific configuration file has been asked
			logger.info("[GET] Return the triple set " + triplesetName + " of " + taskID);
			return new StringRepresentation(tripleSet.getTriples(), MediaType.TEXT_PLAIN);
		} catch (Exception e) {
			e.printStackTrace();

			// If anything goes wrong, just report back on an internal error
			setStatus(Status.SERVER_ERROR_INTERNAL);
			return null;
		}
	}

	/**
	 * Update an existing set of triples or create a new one with that name
	 * 
	 * @param form
	 *            the form with the request parameters
	 * @throws Exception
	 */
	@Put
	public Representation update(Form form) throws Exception {
		// Parse the identifier
		logger.info("[PUT] Update the triple set " + triplesetName + " of " + taskID);

		// Check credentials
		if (form.getFirstValue("api_key", true) == null || !form.getFirstValue("api_key", true).equals(APIKey.KEY)) {
			setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			return null;
		}

		// Get the triples to persist
		String triples = form.getFirstValue("triples");
		if (triples == null) {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return null;
		}

		// Try to get the triple set
		ObjectManager manager = ((MainApplication) getApplication()).getObjectManager();
		tripleSet = manager.getTripleSetForTask(taskID, triplesetName);

		if (tripleSet == null) {
			// Create the triple set
			tripleSet = new TripleSet();
			tripleSet.setLastModificationDate(new Date());
			tripleSet.setName(triplesetName);
			tripleSet.setTriples(triples);
			manager.addTripleSet(taskID, tripleSet);
		} else {
			// Update the value and persist
			tripleSet.setTriples(triples);
			manager.saveTripleSet(tripleSet);
		}

		// Add a notification
		Notification notification = new Notification();
		notification.setSeverity("warn");
		notification.setMessage("triple set modified");
		manager.addNotification(taskID, notification);

		setStatus(Status.SUCCESS_OK);
		return new StringRepresentation("updated", MediaType.TEXT_PLAIN);
	}
}
