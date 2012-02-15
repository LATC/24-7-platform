/**
 * 
 */
package eu.latc.console.resource;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import eu.latc.console.MainApplication;
import eu.latc.console.ObjectManager;
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

		try {
			// A specific configuration file has been asked
			logger.info("[GET] Return the triple sets " + triplesetName + " of " + taskID);
			return new StringRepresentation(tripleSet.getTriples(), MediaType.TEXT_PLAIN);
		} catch (Exception e) {
			e.printStackTrace();

			// If anything goes wrong, just report back on an internal error
			setStatus(Status.SERVER_ERROR_INTERNAL);
			return null;
		}
	}
}
