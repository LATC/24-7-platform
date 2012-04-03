package eu.latc.misc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MDSConnection {
	private final String host;
	private final String key;

	/**
	 * @param host
	 * @param key
	 */
	public MDSConnection(String host, String key) {
		this.host = host;
		this.key = key;
	}

	/**
	 * @param content
	 * @param uri
	 * @return
	 */
	public boolean put(String content, String uri) {
		boolean res = false;
		String URI = host + "?apiKey=" + key + "&graph=" + uri;
		try {
			URL url = new URL(URI);
			HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestProperty("Content-Type", "text/plain");
			httpCon.setRequestMethod("PUT");
			if (content != null) {
				OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
				out.write(content);
				out.close();
			}
			if (httpCon.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED)
				res = true;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	public boolean delete(String uri) {

		boolean res = false;
		String URI = host + "?apiKey=" + key + "&graph=" + uri;
		try {
			URL url = new URL(URI);
			HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoOutput(true);

			httpCon.setRequestMethod("DELETE");
			httpCon.connect();
			if (httpCon.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED)
				res = true;

		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return res;

	}

	public static void main(String[] args) throws Exception {

		String mds_host = "http://mds.lod-cloud.net/graphs";
		String mds_key = "7cf8f3c95e1c296f9b186c928c0ee88b";
		String URLs = mds_host
				+ "?apiKey="
				+ mds_key
				+ "&graph=http://demo.sindice.net/latctemp/2012-02-10/transportToordnancesurvey(trafficcountpoints)/void.ttl";

		URL url = new URL(URLs);
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);

		httpCon.setDoOutput(true);
		httpCon.setRequestProperty("Content-Type", "text/turtle");

		httpCon.setRequestMethod("PUT");
		System.out.print(httpCon.getResponseMessage());

	}
}
