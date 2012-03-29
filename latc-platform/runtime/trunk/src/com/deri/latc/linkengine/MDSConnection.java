package com.deri.latc.linkengine;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


import com.deri.latc.utility.Parameters;


public class MDSConnection {
	
	public static String message=null;
		
	public static boolean put(String content, String uri ){
		boolean res = false;
			String URI = Parameters.MDS_HOST+"?apiKey="+Parameters.API_KEY_MDS+"&graph="+uri;
	     try {
	    	  URL url = new URL(URI);
	    		 HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
	    		 httpCon.setDoOutput(true);
	    		 httpCon.setRequestProperty(
	    				    "Content-Type", "text/turtle" );

	    		 httpCon.setRequestMethod("PUT");
	    		 if(content!=null)
	    		 {
	    			 OutputStreamWriter out = new OutputStreamWriter(
	    			  httpCon.getOutputStream());
	    			 BufferedReader id = new BufferedReader(new FileReader(content));
	    			 String line;
	    			 while ((line = id.readLine()) != null) {
	    				out.write(line);
	    			 }
	    			 out.close();
	    		 }
	    		 if(httpCon.getResponseCode()!=HttpURLConnection.HTTP_ACCEPTED)
	    			 res =true;
	    		 
		} catch (FileNotFoundException e) {
			message = e.getMessage();
		} catch (IOException e) {
			message = e.getMessage();
		}
		return res;
	}
	
	public static boolean delete (String uri)
	{
	
		boolean res = false;
		String URI = Parameters.MDS_HOST+"?apiKey="+Parameters.API_KEY_MDS+"&graph="+uri;
     try {
    	  URL url = new URL(URI);
    		 HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
    		 httpCon.setDoOutput(true);
    	
    		 httpCon.setRequestMethod("DELETE");
    		 httpCon.connect();
    		 if(httpCon.getResponseCode()!=HttpURLConnection.HTTP_ACCEPTED)
    			 res =true;
    		 
	} catch (FileNotFoundException e) {
		message = e.getMessage();
	} catch (IOException e) {
		message = e.getMessage();
	}
	return res;
		
	}
	
	
  public static void main(String[] args) throws Exception {
    
   String mds_host ="http://mds.lod-cloud.net/graphs";
   String mds_key = "7cf8f3c95e1c296f9b186c928c0ee88b";
	String URLs = mds_host+"?apiKey="+mds_key+"&graph=http://demo.sindice.net/latctemp/2012-02-10/transportToordnancesurvey(trafficcountpoints)/void.ttl";
	 
	 URL url = new URL(URLs);
	 HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
	 httpCon.setDoOutput(true);

	 httpCon.setDoOutput(true);
	 httpCon.setRequestProperty(
			    "Content-Type", "text/turtle" );

	 httpCon.setRequestMethod("PUT");
	 System.out.print(httpCon.getResponseMessage());
      

  }
}

