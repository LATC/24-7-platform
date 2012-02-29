package eu.latc.linkqa;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.sun.corba.se.impl.orbutil.StackImpl;
import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.log4j.lf5.util.Resource;

import javax.naming.ldap.SortResponseControl;
import java.io.*;

/**
 * @author Claus Stadler
 *         Date: 2/28/12
 *         Time: 4:17 PM
 */
public class LinksetEvaluatorBashWrapper {

    public static void main(String[] args)
        throws Exception
    {
        String resource = "http://test.org/";
        File linksFile = new File("../../link-specifications/geonames-linkedgeodata-shop/links.nt");
        File refsFile = new File("../../link-specifications/geonames-linkedgeodata-shop/positive.nt");

        // Polarity indicates the polarity of the reference set, and can be 'positive' or 'negative'
        // If it is omitted, the bash script will check the refset file name whether it contains
        // these strings.
        String polarity = "";

        Model model = evaluate(resource, linksFile, refsFile, polarity);

        model.write(System.out, "N-TRIPLES");
    }

    public static Model evaluate(String resource, FileSystem fs, Configuration config, Path linksPath, Path refsPath, String polarity) throws IOException, InterruptedException {
        File linksFile = File.createTempFile("links",".nt.tmp");
        File refsFile = File.createTempFile("refs",".nt.tmp");

        try {
            FileUtil.copy(fs, linksPath, linksFile, false, config);
            FileUtil.copy(fs, refsPath, refsFile, false, config);

            Model result = evaluate(resource, linksFile, refsFile, polarity);

            return result;
        } finally {
            linksFile.delete();
            refsFile.delete();
        }
    }

    public static Model evaluate(String resource, File linksFile, File refsFile, String polarity)
            throws IOException, InterruptedException
    {
        if(polarity == null) {
            polarity = "";
        }

        String cmd = "src/main/bash/evaluate-rdf.sh";
        Process process = Runtime.getRuntime().exec(new String[] {cmd, linksFile.getAbsolutePath(), refsFile.getAbsolutePath(), resource, polarity});

        InputStream in = process.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int n;
        while((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }

        process.waitFor();
        if(process.exitValue() != 0) {
            throw new RuntimeException(out.toString());
        }

        String str = out.toString();
        Model model = ModelFactory.createDefaultModel();
        model.read(new ByteArrayInputStream(str.getBytes()), "http://dummy.org/", "N-TRIPLES");

        return model;
    }

}
