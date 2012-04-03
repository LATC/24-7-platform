/**
 * 
 */
package eu.latc.vocabularies;

import org.restlet.data.Reference;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com> Adapted from from
 *         org.openrdf.model.vocabulary.RDF
 */
public class RDF {
	/** http://www.w3.org/1999/02/22-rdf-syntax-ns# */
	public static final String NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#type */
	public final static Reference TYPE;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#Property */
	public final static Reference PROPERTY;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral */
	public final static Reference XMLLITERAL;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#subject */
	public final static Reference SUBJECT;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate */
	public final static Reference PREDICATE;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#object */
	public final static Reference OBJECT;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement */
	public final static Reference STATEMENT;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag */
	public final static Reference BAG;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#Alt */
	public final static Reference ALT;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq */
	public final static Reference SEQ;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#value */
	public final static Reference VALUE;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#li */
	public final static Reference LI;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#List */
	public final static Reference LIST;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#first */
	public final static Reference FIRST;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#rest */
	public final static Reference REST;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#nil */
	public final static Reference NIL;

	/** http://www.w3.org/1999/02/22-rdf-syntax-ns#langString */
	public static final Reference LANGSTRING;

	static {
		TYPE = new Reference(RDF.NAMESPACE + "type");
		PROPERTY = new Reference(RDF.NAMESPACE + "Property");
		XMLLITERAL = new Reference(RDF.NAMESPACE + "XMLLiteral");
		SUBJECT = new Reference(RDF.NAMESPACE + "subject");
		PREDICATE = new Reference(RDF.NAMESPACE + "predicate");
		OBJECT = new Reference(RDF.NAMESPACE + "object");
		STATEMENT = new Reference(RDF.NAMESPACE + "Statement");
		BAG = new Reference(RDF.NAMESPACE + "Bag");
		ALT = new Reference(RDF.NAMESPACE + "Alt");
		SEQ = new Reference(RDF.NAMESPACE + "Seq");
		VALUE = new Reference(RDF.NAMESPACE + "value");
		LI = new Reference(RDF.NAMESPACE + "li");
		LIST = new Reference(RDF.NAMESPACE + "List");
		FIRST = new Reference(RDF.NAMESPACE + "first");
		REST = new Reference(RDF.NAMESPACE + "rest");
		NIL = new Reference(RDF.NAMESPACE + "nil");
		LANGSTRING = new Reference(RDF.NAMESPACE + "langString");
	}

}
