package eu.latc.linkqa;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/25/11
 *         Time: 12:21 AM
 */ /*
This is so annoying doing it like that...

*/
public class Vocab
{
    public static final String ns = "http://example.org/";

    public static final Property linksetSize = ResourceFactory.createProperty(ns + "linksetSize");
    public static final Property refsetSize = ResourceFactory.createProperty(ns + "refsetSize");
    public static final Property overlapSize = ResourceFactory.createProperty(ns + "overlapSize");

    public static final Property precision = ResourceFactory.createProperty(ns + "precision");
    public static final Property recall = ResourceFactory.createProperty(ns + "recall");
    public static final Property fmeasure = ResourceFactory.createProperty(ns + "fmeasure");

    public static final Property totalTripleCount = ResourceFactory.createProperty(ns + "totalTripleCount");
    public static final Property effectiveTripleCount = ResourceFactory.createProperty(ns + "effectiveTripleCount");
    public static final Property duplicateTripleCount = ResourceFactory.createProperty(ns + "duplicateTripleCount");

    public static final Property duplicateCacheSize = ResourceFactory.createProperty(ns + "duplicateCacheSize");
    public static final Property duplicateCacheUsage = ResourceFactory.createProperty(ns + "duplicateCacheUsage");

    public static final Property referenceset = ResourceFactory.createProperty(ns + "referenceset");
    public static final Property linkset = ResourceFactory.createProperty(ns + "linkset");

    public static final Property duration = ResourceFactory.createProperty(ns + "duration");
    public static final Property startDate = ResourceFactory.createProperty(ns + "startDate");
    public static final Property endDate = ResourceFactory.createProperty(ns + "endDate");
    public static final Property location = ResourceFactory.createProperty(ns + "location");
    //private static final Resource duration = ResourceFactory.createResource(ns + "duration");


    private static final Resource evaluation = ResourceFactory.createResource(ns + "Evaluation");

}
