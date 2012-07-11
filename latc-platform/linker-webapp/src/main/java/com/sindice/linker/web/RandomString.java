package com.sindice.linker.web;

import java.math.BigInteger;
import java.security.SecureRandom;

public class RandomString {

	private SecureRandom random = new SecureRandom();
	
	public String getRandomString(){
		 return new BigInteger(130, random).toString(32);
	}
	
}
