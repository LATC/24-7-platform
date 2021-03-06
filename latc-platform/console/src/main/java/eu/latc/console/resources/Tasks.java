package eu.latc.console.resources;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.atom.Feed;
import org.restlet.ext.atom.Text;
import org.restlet.ext.json.JsonConverter;
import org.restlet.ext.rdf.Graph;
import org.restlet.ext.rdf.Literal;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.latc.console.MainApplication;
import eu.latc.console.ObjectManager;
import eu.latc.console.objects.Notification;
import eu.latc.console.objects.Task;
import eu.latc.misc.MDSConnection;
import eu.latc.vocabularies.DCTERMS;
import eu.latc.vocabularies.RDF;
import eu.latc.vocabularies.SILKSPEC;
import eu.latc.vocabularies.XSD;

public class Tasks extends BaseResource {
	// Logger instance
	protected final Logger logger = LoggerFactory.getLogger(Tasks.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.restlet.resource.UniformResource#doInit()
	 */
	@Override
	protected void doInit() throws ResourceException {
		Set<Method> methods = new HashSet<Method>();
		methods.add(Method.ALL);
		this.setAllowedMethods(methods);
		// logger.info(this.getRequest().toString());
		// logger.info(this.getQuery().toString());
		// logger.info(this.getRequestEntity().toString());
		// logger.info(this.getMethod().toString());
		// logger.info(this.getRequestAttributes().toString());
		getVariants().add(new Variant(MediaType.MULTIPART_FORM_DATA));
		getVariants().add(new Variant(MediaType.MULTIPART_ALL));
		// logger.info(this.getVariants().toString());
		logger.info("" + this.getRequest().getChallengeResponse());
		logger.info("" + this.getRequest().getAttributes());
		logger.info("" + getReference().getQueryAsForm());
	}

	/**
	 * Add a new task
	 * 
	 * @throws Exception
	 * 
	 */
	@Post
	public Representation addForm(Form form) throws Exception {
		if (form == null) {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return null;
		}

		logger.info("[POST] Received a new task " + form.toString());

		logger.info(form.getNames().toString());

		// Load the query parameters
		String api_key = form.getFirstValue("api_key", true);
		String specification = form.getFirstValue("specification", true);
		String title = form.getFirstValue("title", true);
		String description = form.getFirstValue("description", true);
		String author = form.getFirstValue("author", true);

		// Check credentials
		if (api_key == null || !api_key.equals(APIKey.KEY)) {
			logger.warn("Invalid key " + api_key);
			setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			return null;
		}

		// We need at least a specification and a title
		if (specification == null || title == null) {
			logger.warn("Invalid request");
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return null;
		}

		// Get the entity manager
		ObjectManager manager = ((MainApplication) getApplication()).getObjectManager();

		try {
			// Save the task, an exception may be raised if the XML is not valid
			String taskID = manager.addTask(specification);

			// Set the title and persist the task
			Task task = manager.getTaskByID(taskID);
			task.setTitle(title == null ? "No title" : title);
			task.setDescription(description == null ? "No description" : description);
			task.setAuthor(author == null ? "Unknown" : author);
			task.setCreationDate(new Date());
			task.setExecutable(true);
			manager.saveTask(task);

			// Add an upload report
			Notification report = new Notification();
			report.setMessage("Task created");
			report.setSeverity("info");
			report.setData("");
			manager.addNotification(taskID, report);

			// Generate a bunch of triples to be sent to the MDS
			Reference r = new Reference(getRequest().getOriginalRef().getHostIdentifier() + "/task/" + taskID);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ");
			Graph graph = new Graph();
			graph.add(r, RDF.TYPE, SILKSPEC.SPEC);
			graph.add(r, SILKSPEC.ID, new Literal(taskID, XSD.HEXBINARY));
			graph.add(r, DCTERMS.CREATOR, new Literal(author == null ? "Unknown" : author));
			graph.add(r, DCTERMS.CREATED, new Literal(sdf.format(task.getCreationDate()), XSD.DATETIME));
			String graphText = graph.getRdfNTriplesRepresentation().getText();

			// Send this to the MDS
			String mdsHost = ((MainApplication) getApplication()).getParameters().get("MDS_HOST");
			String mdsKey = ((MainApplication) getApplication()).getParameters().get("API_KEY_MDS");
			MDSConnection mds = new MDSConnection(mdsHost, mdsKey);
			mds.put(graphText, r.toUrl().toString());

			// Set the return code and return the identifier
			setStatus(Status.SUCCESS_CREATED);

			// Return the reference information
			JSONObject json = new JSONObject();
			json.put("id", taskID);
			json.put("href", getReference() + "/" + taskID);
			logger.info("[POST] Reply " + json);
			JsonConverter conv = new JsonConverter();
			return conv.toRepresentation(json, null, null);
		} catch (Exception e) {
			logger.warn("Exception while saving the task");
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return null;
		}
	}

	/**
	 * Handler for suffix based content negotiation
	 * 
	 * @param variant
	 * @return
	 * @throws Exception
	 */
	@Get("json|atom")
	public Representation toSomething(Variant variant) throws Exception {
		if (variant.getMediaType().equals(MediaType.APPLICATION_ATOM))
			return toAtom();
		return toJSON();
	}

	/**
	 * Return the list of tasks
	 * 
	 * @throws Exception
	 */
	@Get("json")
	public Representation toJSON() throws Exception {
		Form params = getReference().getQueryAsForm();

		// Handle the "limit" parameter
		int limit = 0;
		if (params.getFirstValue("limit", true) != null)
			limit = Integer.parseInt(params.getFirstValue("limit", true));

		// Handle the "executable" filter
		boolean filterExecutable = false;
		boolean executable = false;
		if (params.getFirstValue("executable", true) != null) {
			filterExecutable = true;
			executable = Boolean.parseBoolean(params.getFirstValue("executable", true));
		}

		logger.info("[GET-JSON] Return a list of tasks " + limit);

		// Get access to the entity manager stored in the app
		ObjectManager manager = ((MainApplication) getApplication()).getObjectManager();

		// The object requested is the list of configuration files
		JSONObject json = new JSONObject();
		JSONArray array = new JSONArray();
		for (Task task : manager.getTasks(limit, filterExecutable, executable))
			array.put(task.toJSON());
		json.put("task", array);

		JsonConverter conv = new JsonConverter();
		return conv.toRepresentation(json, null, null);
	}

	/**
	 * @param tasks
	 * @return
	 * @throws Exception
	 */
	@Get("atom")
	public Feed toAtom() throws Exception {
		logger.info("[GET-ATOM] Return a list of tasks");

		// Get access to the entity manager stored in the app
		ObjectManager manager = ((MainApplication) getApplication()).getObjectManager();
		Feed result = new Feed();
		result.setTitle(new Text("Tasks created for LATC"));
		Entry entry;

		for (Task task : manager.getTasks(5, false, false)) {
			entry = new Entry();
			entry.setTitle(new Text(task.getTitle()));
			StringBuffer summary = new StringBuffer();
			summary.append("Description: " + task.getDescription()).append('\n');
			summary.append("Creation date:" + task.getCreationDate()).append('\n');
			entry.setSummary(summary.toString());
			result.getEntries().add(entry);
		}
		return result;
	}

}
