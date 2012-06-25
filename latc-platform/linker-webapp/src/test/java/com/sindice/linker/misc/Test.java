package com.sindice.linker.misc;

import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;

public class Test {

	
	public static void main(String[] args){
		MessageDigestPasswordEncoder messageDigestPasswordEncoder = new org.springframework.security.authentication.encoding.ShaPasswordEncoder(256);
		String p = messageDigestPasswordEncoder.encodePassword("admin", null);
		System.out.println(p);
		
		p = messageDigestPasswordEncoder.encodePassword("admin", "admin");
		System.out.println(p);
		

		p = messageDigestPasswordEncoder.encodePassword("password", "john@example.com");
		System.out.println(p);

	}
}
