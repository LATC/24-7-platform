package com.sindice.linker.provider.openid;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.sindice.linker.provider.CustomRequestResponseAction;



public class OpenIdAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler{

	@Autowired
	CustomRequestResponseAction action;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws ServletException, IOException {
	
		
		action.success(request, response,  authentication);
		
		boolean hasAdminRole = false;
		for(GrantedAuthority a: authentication.getAuthorities()) {
			if("ROLE_ADMIN".equals(""+a)){
				hasAdminRole = true;
				break;
			}
		}
		
		if(hasAdminRole){
			getRedirectStrategy().sendRedirect(request, response, "/admin/users?page=1&size=10");
		}else{
			getRedirectStrategy().sendRedirect(request, response, "/member/index");
		}
	}
}
	