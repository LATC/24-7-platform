/**
 * 
 */
package eu.latc.vocabularies;

import org.restlet.data.Reference;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class SILKSPEC {
	/** silkspec: http://vocab.deri.ie/LinkSpec# */
	public static final String NAMESPACE = "http://vocab.deri.ie/LinkSpec#";

	/** silkspec:SilkSpec */
	public final static Reference SPEC;

	/** silkspec:ID */
	public final static Reference ID;

	/** silkspec:Title */
	public final static Reference TITLE;

	static {
		SPEC = new Reference(SILKSPEC.NAMESPACE + "SilkSpec");
		ID = new Reference(SILKSPEC.NAMESPACE + "ID");
		TITLE = new Reference(SILKSPEC.NAMESPACE + "Title");
	}
}
