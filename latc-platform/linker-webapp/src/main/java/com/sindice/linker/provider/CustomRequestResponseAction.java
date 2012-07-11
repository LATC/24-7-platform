package com.sindice.linker.provider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;

public interface CustomRequestResponseAction {

	public void success(HttpServletRequest request, HttpServletResponse response, Authentication authentication);
	public void failure(HttpServletRequest request, HttpServletResponse response);
}
