package com.deri.latc.linkengine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PutMethod;

import com.deri.latc.utility.Parameters;

public class MDSConnection {

	private String message = null;

	public boolean putVOID(String voidfile, String linkset) {
		boolean res = false;
		HttpClient client = new HttpClient();
		String URL = Parameters.MDS_HOST + "?apiKey=" + Parameters.API_KEY_MDS + "&graph=" + linkset;
		PutMethod pm = new PutMethod(URL);
		File f = new File(voidfile);
		try {
			pm.setRequestBody(new FileInputStream(f));
			pm.setRequestHeader("Content-type", "text/turtle");
			int statusCode = client.executeMethod(pm);
			if (statusCode == HttpStatus.SC_ACCEPTED)
				res = true;
		} catch (FileNotFoundException e) {
			this.message = e.getMessage();
		} catch (HttpException e) {
			this.message = e.getMessage();
		} catch (IOException e) {
			this.message = e.getMessage();
		}
		return res;
	}

	public String getMessage() {
		return this.message;
	}

	public static void main(String[] args) throws Exception {

	}
}
