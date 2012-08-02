package com.sindice.linker.servlet;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sindice.linker.cache.MemcachedClientWrapper;

public class AppServletConfigurationContextListener extends ServletConfigurationContextListener  {

		public static final String APPLICATION_BASE_URL = "applicationBaseUrl";
		public static final String MDS_QUERY_LAST_USER_JOBS = "mdsQueryLastUserJobs";
		public static final String MDS_QUERY_LAST_JOBS = "mdsQueryLastJobs";
		public static final String MDS_URL = "mdsUrl";

		private static final Logger LOGGER = LoggerFactory.getLogger(AppServletConfigurationContextListener.class);
		
	    public static final String USER_AGENT_STRING = "Linker.sindice.com (http://linker.sindcie.com)";
	    public static final String ACCEPT_STRING = "application/json";
		private static final int DEFAULT_TOTAL_CONNECTIONS = 5;
		
		private int clientTimeout = 20;
		private MultiThreadedHttpConnectionManager manager;
		
		@Override
		public void contextDestroyed(ServletContextEvent servletContextEvent) {
			LOGGER.info("invoke contextDestroyed");
			ServletContext servletContext = servletContextEvent.getServletContext();
			
			if(manager!=null){
			    manager.shutdown();
			}
			super.contextDestroyed(servletContextEvent);
		}

		
		public void contextInitialized(ServletContextEvent servletContextEvent) {
			super.contextInitialized(servletContextEvent);
			LOGGER.info("invoke contextInitialized");
			ServletContext servletContext =servletContextEvent.getServletContext();
			XMLConfiguration appConfig = (XMLConfiguration) servletContext.getAttribute("config");
			
			LOGGER.info("Start initialization");
				try {
					//set up httpclient
				    manager = new MultiThreadedHttpConnectionManager();
			        HttpClient httpClient = new HttpClient(manager);
			        HttpConnectionManager connectionManager = httpClient.getHttpConnectionManager();
			        HttpConnectionManagerParams params = connectionManager.getParams();
			        params.setConnectionTimeout(clientTimeout*1000);
			        params.setSoTimeout(clientTimeout*1000);
			        params.setMaxTotalConnections(DEFAULT_TOTAL_CONNECTIONS);

			        HostConfiguration hostConf = httpClient.getHostConfiguration();
			        List<Header> headers = new ArrayList<Header>();
			        // NOTE do not specify default accept as it will be specified later 
			        // as user my force a specyfic content type
			        headers.add(new Header("User-Agent", USER_AGENT_STRING));
			        headers.add(new Header("Accept-Language", "en-us,en-gb,en,*;q=0.3"));
			        headers.add(new Header("Accept-Charset",
			                "utf-8,iso-8859-1;q=0.7,*;q=0.5"));
			        // headers.add(new Header("Accept-Encoding", "x-gzip, gzip"));
			        hostConf.getParams().setParameter("http.default-headers", headers);
			        
			        servletContext.setAttribute(HttpClient.class.getName(), httpClient);
	                
			        String applicationBaseUrl = getParameterWithLogging(appConfig , APPLICATION_BASE_URL, "");
			        String mdsUrl = getParameterWithLogging(appConfig , MDS_URL, "");
			        String mdsQueryLastJobs = getParameterWithLogging(appConfig , MDS_QUERY_LAST_JOBS, "");
			        String mdsQueryLastUserJobs = getParameterWithLogging(appConfig , MDS_QUERY_LAST_USER_JOBS, "");
			        
			        servletContext.setAttribute(APPLICATION_BASE_URL, applicationBaseUrl);
			        servletContext.setAttribute(MDS_URL, mdsUrl);
			        servletContext.setAttribute(MDS_QUERY_LAST_JOBS, mdsQueryLastJobs);
			        servletContext.setAttribute(MDS_QUERY_LAST_USER_JOBS, mdsQueryLastUserJobs);
			        
			        
			        String useMemcached = getParameterWithLogging(appConfig,"USE_MEMCACHED","false");
					String memcachedHost = getParameterWithLogging(appConfig,"MEMCACHED_HOST","localhost");
					String memcachedPort = getParameterWithLogging(appConfig,"MEMCACHED_PORT","11211");
					
					List<InetSocketAddress> addresses = AddrUtil.getAddresses(memcachedHost+":"+memcachedPort);
					MemcachedClientWrapper mc = null;
					if( "true".equals(useMemcached)){
						mc = new MemcachedClientWrapper(new MemcachedClient(addresses),true);
					}
			        servletContext.setAttribute(MemcachedClientWrapper.class.getName(),mc);
			        
	                LOGGER.info("initailization finished");
				} catch (Exception e) {
					LOGGER.error("Could not initialise AppListener", e);
				}
		  }
		
	}
