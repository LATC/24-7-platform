package com.sindice.linker.provider.openid;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyBeforeOpenIdFilter implements Filter{

	static Logger logger = LoggerFactory.getLogger(MyBeforeOpenIdFilter.class);
	
	static class FilteredRequest extends HttpServletRequestWrapper {
		
		public FilteredRequest(HttpServletRequest request) {
			super(request);
		}
		
		@Override
		public java.lang.StringBuffer getRequestURL(){
			String baseUrl = (String) super.getSession().getServletContext().getAttribute("applicationBaseUrl");
			StringBuffer sb = super.getRequestURL();
			
			int index = sb.indexOf("/j_spring_openid_security_check");
			if(index != -1){
				// here replace the host etc with proper value
				if(baseUrl.endsWith("/")){
					baseUrl = baseUrl.substring(0, baseUrl.length()-1);
				}
				logger.debug("Changing the getRequestURL to inject the correct host so openid login could work behind proxy");
				logger.debug("Original getRequestURL: "+sb.toString());
				logger.debug("Replacing the baseUrl with: "+baseUrl);
				sb.replace(0, index, baseUrl);
				logger.debug("New getRequestURL: "+sb.toString());
			}
			return sb;
		}
		
	}

	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		//No need to init
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		chain.doFilter(new FilteredRequest((HttpServletRequest) request), response);
	}

	@Override
	public void destroy() {
		//No need to destroy
	}

}
