package eu.latc.linkqa;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.*;
import org.aksw.commons.collections.CacheSet;
import org.aksw.commons.collections.IteratorIterable;
import org.aksw.commons.reader.NTripleIterator;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.lf5.util.StreamUtils;

import java.io.InputStream;
import java.util.*;


/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/24/11
 *         Time: 5:17 PM
 */
public class LinksetEvaluator extends Configured implements Tool
{
    @Override
    public int run(String[] strings) throws Exception {
        GenericOptionsParser parser = new GenericOptionsParser(this.getConf(), strings);
        String[] otherArgs = parser.getRemainingArgs();

        if (otherArgs.length != 3) {
            System.err.println("Usage: <process-uri> <linkset> <referenceset>");
            System.exit(2);
        }

        InputStream linksetIn = null;
        InputStream refsetIn = null;


        long startTime = System.currentTimeMillis();

        //Path a = new Path("hdfs://localhost:54310/user/raven/a.txt");

        FileSystem fs = FileSystem.get(this.getConf());
        String processUri = otherArgs[0];
        Path linkset = new Path(otherArgs[1]);
        Path refset = new Path(otherArgs[2]);

        Resource evalUri = ResourceFactory.createResource(processUri);


        LinksetEvaluationResult eval = run(fs, linkset, refset);

        Model model = ModelFactory.createDefaultModel();
        EvaluationResultFormatter.toModel(model, eval, evalUri);

        model.write(System.out, "TTL");

        return 0;
    }


    public LinksetEvaluationResult run(FileSystem fs, Path linkset, Path refset)
        throws Exception
    {
        InputStream linksetIn = null;
        InputStream refsetIn = null;

        //long startTime = System.currentTimeMillis();
        Calendar startDate = new GregorianCalendar();

        try {
            linksetIn = fs.open(linkset);
            refsetIn = fs.open(refset);

            /*
            StreamUtils.copy(linksetIn, System.out);
            if(true) {
                System.exit(-666);
            }
            */


            // We load the reference set into memory
            // Note: If that doesn't work out like that, then we can either
            // use the other hadoop class, or partition the files on a blocking key.
            int referenceDuplicateCount = 0;
            Set<Triple> references = new HashSet<Triple>();
            for(Triple triple : new IteratorIterable<Triple>(new NTripleIterator(linksetIn, null))) {
                if(references.contains(triple)) {
                    ++referenceDuplicateCount;
                    continue;
                }

                references.add(triple);
            }


            // Generally we assume that there are no duplicates, however
            // just to make sure we keep a cache of seen resources
            int duplicateCacheLimit = 1000000;
            CacheSet<Triple> duplicateCache = new CacheSet<Triple>(duplicateCacheLimit, true);
            int linksetTotalSize = 0;
            int linksetEffectiveSize = 0;
            int matchCount = 0;
            int duplicateCount = 0;
            for(Triple triple : new IteratorIterable<Triple>(new NTripleIterator(refsetIn, null))) {
                //System.out.println(triple);
                if(!duplicateCache.add(triple)) {
                    ++duplicateCount;
                    ++linksetTotalSize;
                    continue;
                }

                if(references.contains(triple)) {
                    ++matchCount;
                }

                ++linksetEffectiveSize;
            }

            //long endTime = System.currentTimeMillis();
            Calendar endDate = new GregorianCalendar();
            //long duration = endDate.getTime() - startDate.getTime();
            //int duration = (int)(endTime - startTime);

            DatasetDesc linksetDesc = new DatasetDesc(linkset, linksetTotalSize, duplicateCount, linksetEffectiveSize);
            DatasetDesc refsetDesc = new DatasetDesc(refset, references.size() + referenceDuplicateCount, referenceDuplicateCount, references.size());
            PrecResult stats = PrecResult.create(linksetEffectiveSize, references.size(), matchCount);

            LinksetEvaluationResult result = new LinksetEvaluationResult(stats, linksetDesc, refsetDesc, startDate, endDate, duplicateCacheLimit, duplicateCache.size());


            return result;
        } finally {
            if(linksetIn != null) {
                linksetIn.close();
            }

            if(refsetIn != null) {
                refsetIn.close();
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        int ret = ToolRunner.run(new LinksetEvaluator(), args);

        System.exit(ret);
    }

}

