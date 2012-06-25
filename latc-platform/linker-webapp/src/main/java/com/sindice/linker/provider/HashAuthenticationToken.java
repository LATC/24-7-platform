package com.sindice.linker.provider;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class HashAuthenticationToken extends AbstractAuthenticationToken{

	private final Object credentials;
    private final Object principal;
	
	public HashAuthenticationToken(Object principal,Object credentials, Collection<GrantedAuthority> authorities){
		super(authorities);
		this.principal = principal;
		this.credentials = credentials;
		setAuthenticated(true);
	}
	@Override
	public Object getCredentials() {
		return this.credentials;
	}

	@Override
	public Object getPrincipal() {
		return this.principal;
	}

}
