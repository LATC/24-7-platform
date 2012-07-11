package com.sindice.linker.provider;

import java.io.IOException;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

public class AuthenticationLogoutSuccessHandler extends  SimpleUrlLogoutSuccessHandler  {
	
	@Autowired
	CustomRequestResponseAction action;

	
	Logger logger = LoggerFactory.getLogger(AuthenticationLogoutSuccessHandler.class);
	private String targetUrl;
	
	public String getTargetUrl() {
		return targetUrl;
	}

	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}

	@Override
	public void onLogoutSuccess(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, Authentication authentication) throws IOException, ServletException{
		
		action.failure(request, response);
		
		if (authentication != null) {
            // TODO:  do something it is very bad that user is still authenticated
			
        }
        setDefaultTargetUrl(targetUrl);
        super.onLogoutSuccess(request, response, authentication);     
	} 

}
