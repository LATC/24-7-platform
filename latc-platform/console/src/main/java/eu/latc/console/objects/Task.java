package eu.latc.console.objects;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.StringTokenizer;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import eu.latc.misc.DateToXSDateTime;

/**
 * @author cgueret
 * 
 */
/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 *
 */
/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
@PersistenceCapable(detachable = "true", identityType = IdentityType.APPLICATION)
@DatastoreIdentity(strategy = IdGeneratorStrategy.UUIDHEX)
@PrimaryKey(name = "identifier")
public class Task implements Serializable {
	// Logger instance
	protected static final Logger logger = LoggerFactory.getLogger(Task.class);

	// Serialization ID
	private static final long serialVersionUID = -8292316878407319874L;

	// The identifier for this task
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.UUIDHEX)
	@Column(name = "TASK_ID", jdbcType = "VARCHAR", length = 32)
	private String identifier;

	// The author of the task
	@Persistent
	private String author = "Administrator";

	// The configuration in its text serialised form
	@Persistent
	@Column(jdbcType = "VARCHAR", length = 20000)
	private String configuration = "";

	// The creation date
	@Persistent
	private Date creationDate = null;

	// A short description of what this task does
	@Persistent
	@Column(jdbcType = "VARCHAR", length = 1000)
	private String description = "";

	// The configuration file, as an XML document
	@NotPersistent
	private Document document = null;

	// Flag for the maturity of the task.
	// The idea is that results of non vetted configuration runs should not be
	// published through the API.
	@Persistent
	private boolean isVetted = false;

	// Flag for the possible execution of the task.
	@Persistent
	private boolean isExecutable = true;

	// The last modification date
	@Persistent
	private Date lastModificationDate = null;

	// Collection of notifications
	@Persistent
	@Element(types = Notification.class, column = "TASK_ID", dependent = "true", mappedBy = "task")
	private final Collection<Notification> notifications = new ArrayList<Notification>();

	// Collection of triple sets
	@Persistent
	@Element(types = TripleSet.class, column = "TASK_ID", dependent = "true", mappedBy = "task")
	private final Collection<TripleSet> triplesets = new ArrayList<TripleSet>();

	// A title
	@Persistent
	@Column(jdbcType = "VARCHAR", length = 125)
	private String title = "";

	/**
	 * Add a new notification to the list
	 * 
	 * @param report
	 */
	public void addNotification(Notification notification) {
		notifications.add(notification);
	}

	/**
	 * Get all the notifications associated with that task
	 * 
	 * @return
	 */
	public Collection<Notification> getNotifications() {
		return notifications;
	}

	/**
	 * @param tripleSet
	 */
	public void addTripleSet(TripleSet tripleSet) {
		triplesets.add(tripleSet);
	}

	/**
	 * Get all the triple sets associated with that task
	 * 
	 * @return the triple sets
	 */
	public Collection<TripleSet> getTripleSets() {
		return triplesets;
	}

	/**
	 * @return
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @return
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @return
	 */
	public String getConfiguration() {
		return configuration;
	}

	/**
	 * Assign a new configuration file to this task
	 * 
	 * @param configuration
	 *            The configuration file expressed in the XML format used by
	 *            SiLK
	 * @throws Exception
	 *             If <code>configuration</code> is null of if it is not a
	 *             proper XML file
	 * 
	 */
	public void setConfiguration(String configuration) throws Exception {
		// Die if the parameter is equal to null
		if (configuration == null)
			throw new Exception();

		// Try to parse the new document to see if it's valid
		Document d = parseLinkingConfiguration(configuration);
		if (d == null)
			throw new Exception();

		// Set the new configuration file
		this.configuration = configuration;
	}

	/**
	 * @return
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
		this.lastModificationDate = this.creationDate;
	}

	/**
	 * @return
	 */
	public Date getLastModificationDate() {
		return lastModificationDate;
	}

	/**
	 * @param lastModificationDate
	 */
	public void setLastModificationDate(Date lastModificationDate) {
		this.lastModificationDate = lastModificationDate;
	}

	/**
	 * Get the description of the configuration file
	 * 
	 * @return
	 */
	public String getDescription() {
		if (description == null)
			return "No description";
		return description;
	}

	/**
	 * Set the description of the configuration file
	 * 
	 * @param description
	 * @return
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Return the XML document of the configuration file stored for this object
	 * 
	 * @return an XML document or <code>null</code> of the current configuration
	 *         file is not valid (should never happen, this is checked at
	 *         assignment time)
	 */
	public Document getDocument() {
		// If the document has not been parsed yet, do it now
		if (this.document == null) {
			try {
				this.document = parseLinkingConfiguration(this.configuration);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return this.document;
	}

	/**
	 * The identifier is an immutable name associated to the task when it is
	 * created
	 * 
	 * @return the identifier of the task (a UUID string)
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * The title is a free text string used to shortly describe the task
	 * 
	 * @return the title associated to the task
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Create a slug from the name of the task. Code grabbed from
	 * ListTranslator.java in the Runtime to ensure consistent creation of names
	 * 
	 * @return a slug for the task
	 */
	public String getSlug() {
		StringTokenizer st = new StringTokenizer(this.getTitle(), " ", false);
		String slug = "";
		while (st.hasMoreElements())
			slug += st.nextElement();
		slug = slug.replace("->", "To");
		return slug;
	}

	/**
	 * @param isVetted
	 */
	public void setVetted(boolean isVetted) {
		this.isVetted = isVetted;
	}

	/**
	 * @return
	 */
	public boolean isVetted() {
		return isVetted;
	}

	/**
	 * To blacklist a task, set this flag to false. It won't be returned when a
	 * call on the list of tasks will be is.sued
	 * 
	 * @param isExecutable
	 *            the isExecutable value to set
	 */
	public void setExecutable(boolean isExecutable) {
		this.isExecutable = isExecutable;
	}

	/**
	 * @return true if the task can be executed, false otherwise
	 */
	public boolean isExecutable() {
		return isExecutable;
	}

	/**
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toJSON() throws JSONException {
		JSONObject entry = new JSONObject();
		entry.put("identifier", this.getIdentifier());
		entry.put("title", this.getTitle());
		entry.put("description", this.getDescription());
		entry.put("author", this.getAuthor());
		entry.put("executable", this.isExecutable());
		entry.put("vetted", this.isVetted());
		entry.put("slug", this.getSlug());
		if (creationDate != null)
			entry.put("created", DateToXSDateTime.format(creationDate));
		if (lastModificationDate != null)
			entry.put("modified", DateToXSDateTime.format(lastModificationDate));
		return entry;
	}

	/**
	 * @param linkingConfiguration
	 * @return
	 * @throws Exception
	 */
	private Document parseLinkingConfiguration(String linkingConfiguration) {
		Document doc = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			StringReader reader = new StringReader(linkingConfiguration);
			InputSource inputSource = new InputSource(reader);
			doc = builder.parse(inputSource);
			reader.close();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return doc;
	}
}

// Serialize the document object into a string
/*
 * try { TransformerFactory transfac = TransformerFactory.newInstance();
 * transfac.setAttribute("indent-number", 4); Transformer t =
 * transfac.newTransformer();
 * t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
 * t.setOutputProperty(OutputKeys.INDENT, "yes");
 * t.setOutputProperty(OutputKeys.METHOD, "xml");
 * 
 * // create string from xml tree StringWriter sw = new StringWriter();
 * t.transform(new DOMSource(configuration), new StreamResult(sw));
 * this.configuration = sw.getBuffer().toString();
 * System.out.println(this.configuration); } catch (TransformerException e) {
 * this.configuration = ""; e.printStackTrace(); }
 */
