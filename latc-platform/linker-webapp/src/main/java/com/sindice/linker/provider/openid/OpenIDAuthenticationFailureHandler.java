package com.sindice.linker.provider.openid;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationStatus;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import com.sindice.linker.domain.User;



public class OpenIDAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	Logger logger = LoggerFactory.getLogger(OpenIDAuthenticationFailureHandler.class);
	@Override
	public void onAuthenticationFailure(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException exception)
			throws IOException, ServletException {
		
		if (exception instanceof UsernameNotFoundException
				&& exception.getAuthentication() instanceof OpenIDAuthenticationToken
				&& ((OpenIDAuthenticationToken) exception.getAuthentication())
						.getStatus().equals(OpenIDAuthenticationStatus.SUCCESS)) {
			
			DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
			request.getSession(true).setAttribute("USER_OPENID_CREDENTIAL",((UsernameNotFoundException) exception).getExtraInformation());
			
			OpenIDAuthenticationToken openIdAuth = (OpenIDAuthenticationToken)exception.getAuthentication();
					
			String openIdIdentifier  = openIdAuth.getName();
			String nick = null;
			String email = null;
			String firstName = null;
			String lastName = null;
			for(OpenIDAttribute attr : openIdAuth.getAttributes()) {
					  //System.out.printf("@@@@ AX Attribute: %s, Type: %s, Count: %d\n", attr.getName(), attr.getType(), attr.getCount());
					  if(attr.getName().equals("email-google") && attr.getValues().size()>0 ){
					    	email = attr.getValues().get(0);
					  }
					  if(attr.getName().equals("nick") && attr.getValues().size()>0 ){
					    	nick = attr.getValues().get(0);
					  }
					  
					  if(attr.getName().equals("email") && attr.getValues().size()>0 ){
					    	email = attr.getValues().get(0);
					  }
					  if(attr.getName().equals("firstname") && attr.getValues().size()>0 ){
					    	firstName = attr.getValues().get(0);
					  }
					  if(attr.getName().equals("lastname") && attr.getValues().size()>0 ){
					    	lastName = attr.getValues().get(0);
					  }
					  
					  /*
					  for(String value : attr.getValues()) {
					      System.out.printf("@@@@@ Value: %s\n", value);
					  }
					  */
			}
			
			// if email taken from openid match any user in the db 
			if(email!=null){
				List<User> users  = User.findUsersByEmailAddress(email).getResultList();
				if(users.size()==1){
					logger.info("Detected user with an email ["+email+"]");
					User user = users.get(0);
					// redirect to a page where user can add an openid to his existing accout
					user.setOpenIdIdentifier(openIdIdentifier);
		            
					request.getSession(true).setAttribute("USER",user);
		            // redirect to add openID to existing account
		            redirectStrategy.sendRedirect(request, response,"/oidadd/?form");
		            return;
				}
			}	
			
			// no user with matching email 
			// create a user and redirect to oidsignup 
			User user = new User();
            user.setEmailAddress(email);
			user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setOpenIdIdentifier(openIdIdentifier);
            user.setPassword("");
            request.getSession(true).setAttribute("USER",user);
            
            // redirect to oidsignup page
            redirectStrategy.sendRedirect(request, response,"/oidsignup/?form");
		} else {
			super.onAuthenticationFailure(request, response, exception);
		}
	}
}