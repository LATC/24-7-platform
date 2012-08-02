/**
 * 
 */
package eu.latc.vocabularies;

import org.restlet.data.Reference;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class XSD {
	/** xsd: http://www.w3.org/2001/XMLSchema# */
	public static final String NAMESPACE = "http://www.w3.org/2001/XMLSchema#";

	/** xsd:dateTime */
	public static final Reference DATETIME;

	/** xsd:hexBinary */
	public static final Reference HEXBINARY;

	static {
		DATETIME = new Reference(XSD.NAMESPACE + "dateTime");
		HEXBINARY = new Reference(XSD.NAMESPACE + "hexBinary");
	}
}
