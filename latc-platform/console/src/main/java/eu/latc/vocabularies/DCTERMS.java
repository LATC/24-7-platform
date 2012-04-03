/**
 * 
 */
package eu.latc.vocabularies;

import org.restlet.data.Reference;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class DCTERMS {
	/** dct: http://purl.org/dc/terms/ */
	public static final String NAMESPACE = "http://purl.org/dc/terms/";

	/** dct:created */
	public final static Reference CREATED;

	/** dct:creator */
	public final static Reference CREATOR;

	static {
		CREATED = new Reference(SILKSPEC.NAMESPACE + "created");
		CREATOR = new Reference(SILKSPEC.NAMESPACE + "creator");
	}

}
