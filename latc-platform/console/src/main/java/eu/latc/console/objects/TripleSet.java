/**
 * 
 */
package eu.latc.console.objects;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
@PersistenceCapable(detachable = "true", identityType = IdentityType.APPLICATION)
@DatastoreIdentity(strategy = IdGeneratorStrategy.UUIDHEX)
@PrimaryKey(name = "identifier")
public class TripleSet implements Serializable {
	// Serial
	private static final long serialVersionUID = 7163573278492727520L;

	// Logger instance
	protected static final Logger logger = LoggerFactory.getLogger(TripleSet.class);

	// The identifier for this triple set
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.UUIDHEX)
	@Column(jdbcType = "VARCHAR", length = 32)
	private String identifier;

	// The triple set, stored as raw text
	@Persistent
	@Column(jdbcType = "VARCHAR", length = 40000)
	private String triples = "";

	// The last modification date
	@Persistent
	private Date lastModificationDate = null;

	// The name
	@Persistent
	private String name = null;

	/**
	 * The identifier is an immutable name associated to the triple set when it
	 * is created
	 * 
	 * @return the identifier of the triple set (a UUID string)
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @return the lastModificationDate
	 */
	public Date getLastModificationDate() {
		return lastModificationDate;
	}

	/**
	 * @param lastModificationDate
	 *            the lastModificationDate to set
	 */
	public void setLastModificationDate(Date lastModificationDate) {
		this.lastModificationDate = lastModificationDate;
	}

	/**
	 * @return the triples
	 */
	public String getTriples() {
		return triples;
	}

	/**
	 * @param triples
	 *            the triples to set
	 */
	public void setTriples(String triples) {
		this.triples = triples;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name.toLowerCase();
	}
}
