package eu.latc.linkqa;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * What structure do we want?
 *
 * -  x a eval . x refset y . y a Dataset . ?y location http://example.org (dataset uri is different from location uri)
 *
 * or
 * - x a eval . x refset y . (y is aready the location uri)
 * I think this option is ok?
 *
 * or
 * - x a eval . x (everything flat)
 *
 *
 */
class EvaluationResultFormatter
{
    public static Model toModel(Model result, DatasetDesc desc, Resource datasetUri)
    {
        if(true) {
            return  result;
        }

        /*
        if(!desc.getLocation().equals(datasetUri)) {
            result.add(datasetUri, Vocab.location, datasetUri);
        }*/

        result.add(datasetUri, Vocab.totalTripleCount, ResourceFactory.createTypedLiteral(desc.getTotalTripleCount()));
        result.add(datasetUri, Vocab.effectiveTripleCount, ResourceFactory.createTypedLiteral(desc.getEffectiveTripleCount()));
        result.add(datasetUri, Vocab.duplicateTripleCount, ResourceFactory.createTypedLiteral(desc.getDuplicateTripleCount()));

        return result;
    }

    public static Model toModel(Model result, PrecResult eval, Resource evalUri) {
        result.add(evalUri, Vocab.precision, ResourceFactory.createTypedLiteral(eval.getPrecision()));
        //result.add(evalUri, Vocab.recall, ResourceFactory.createTypedLiteral(eval.getRecall()));
        //result.add(evalUri, Vocab.fmeasure, ResourceFactory.createTypedLiteral(eval.getFMeasure()));

        result.add(evalUri, Vocab.linksetSize, ResourceFactory.createTypedLiteral(eval.getLinksetSize()));
        result.add(evalUri, Vocab.refsetSize, ResourceFactory.createTypedLiteral(eval.getRefsetSize()));
        result.add(evalUri, Vocab.overlapSize, ResourceFactory.createTypedLiteral(eval.getOverlapSize()));

        return result;
    }

    public static Model toModel(Model result, LinksetEvaluationResult eval, Resource evalUri) //, Resource linkset, Resource refset)
    {
        result.add(evalUri, Vocab.startDate, ResourceFactory.createTypedLiteral(eval.getStartDate()));
        result.add(evalUri, Vocab.endDate, ResourceFactory.createTypedLiteral(eval.getEndDate()));
        result.add(evalUri, Vocab.duration, ResourceFactory.createTypedLiteral(eval.getTimeTaken()));

        //result.add(evalUri, Vocab.duplicateCacheSize, ResourceFactory.createTypedLiteral(eval.getDuplicateCacheLimit()));
        //result.add(evalUri, Vocab.duplicateCacheUsage, ResourceFactory.createTypedLiteral(eval.getDuplicateCacheUsage()));
        //result.add(evalUri, Vocab., ResourceFactory.createTypedLiteral(eval..getTimeTaken()));

        toModel(result, eval.getPrecEvalResult(), evalUri);

        //result.add(evalUri, Vocab.linkset, linkset);
        //result.add(evalUri, Vocab.referenceset, refset);
        Resource locLinkset = ResourceFactory.createResource(eval.getLinkset().getLocation().toUri().toString());
        Resource locRefset = ResourceFactory.createResource(eval.getRefset().getLocation().toUri().toString());

        result.add(evalUri, Vocab.linkset, locLinkset);
        result.add(evalUri, Vocab.referenceset, locRefset);

        toModel(result, eval.getLinkset(), locLinkset);
        toModel(result, eval.getRefset(), locRefset);

        return result;
    }
}
