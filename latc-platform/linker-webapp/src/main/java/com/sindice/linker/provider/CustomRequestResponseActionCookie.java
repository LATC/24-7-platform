package com.sindice.linker.provider;

import java.net.URI;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;

public class CustomRequestResponseActionCookie implements CustomRequestResponseAction{

	
	
	private static final String COOKIE_PREFIX = "workbench-";
	private static Logger logger = LoggerFactory.getLogger(CustomRequestResponseActionCookie.class);
	private String host;
	
	@Override
	public void success(HttpServletRequest request, HttpServletResponse response,Authentication authentication) {
		try{
			String baseUrl = (String) request.getSession(false).getServletContext().getAttribute("applicationBaseUrl");
			URI uri = new URI(baseUrl);
			host = uri.getHost();
			
			String email  = ((User) authentication.getPrincipal()).getUsername();
			List<com.sindice.linker.domain.User> list = com.sindice.linker.domain.User.findUsersByEmailAddress(email).getResultList();
        	if(list.size()==1){
        		com.sindice.linker.domain.User user = list.get(0);
    			Cookie cookie = new Cookie(COOKIE_PREFIX+host,user.getUsername());
    			cookie.setDomain(host);
    			cookie.setMaxAge(-1);
    			cookie.setPath("/");
    			response.addCookie(cookie);
    			logger.info("COOKIE SET");
        	}
		}catch(Exception e ){
			logger.error("Could not set cookie",e);
			failure(request, response);
		}
		
		
	}	

	@Override
	public void failure(HttpServletRequest request, HttpServletResponse response) {
		if(host==null){
			// try to get the host from cookie
			for(Cookie cookie:request.getCookies()){
				String name = cookie.getName();
				if(name.indexOf(COOKIE_PREFIX)==0){
					String h = name.substring(COOKIE_PREFIX.length());
					logger.info("Delete cooie with:"+name +" and host:"+h);
					deleteCookie(response,name,h);
				}
			}
		}else{
			deleteCookie(response,COOKIE_PREFIX+host,host);
		}
	}

	private void deleteCookie(HttpServletResponse response,String name,String host) {
		try {
			Cookie cookie = new Cookie(name, null);
			cookie.setDomain(host);
			cookie.setMaxAge(0);
			cookie.setPath("/");
			response.addCookie(cookie);
			logger.info("COOKIE DELETED");
		} catch (Exception e) {
			logger.error("Could not delete cookie",e);
		}
	}

	
}
