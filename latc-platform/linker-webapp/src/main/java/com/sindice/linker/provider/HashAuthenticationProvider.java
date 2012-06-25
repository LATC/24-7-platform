package com.sindice.linker.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;


public class HashAuthenticationProvider implements AuthenticationProvider {
	
	Logger logger = LoggerFactory.getLogger(HashAuthenticationProvider.class);
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		//String hash = (String) authentication.getPrincipal();
		//User.findUsersByActivationKeyAndEmailAddress(activationKey, emailAddress)
		// TODO here move the logic from signup controler 
		
		//if(hash == null) {
		//	throw new BadCredentialsException("The user with hash " + hash + " could not be found.");
		//}
		return new UsernamePasswordAuthenticationToken(authentication.getPrincipal(),authentication.getCredentials(),authentication.getAuthorities());
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean supports(Class authentication) {
		return HashAuthenticationToken.class.equals(authentication);
	}

}