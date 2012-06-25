package com.sindice.linker.web;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.sindice.linker.web.RandomString;

public class RandomStringTest {

	@Test
	public void test(){
		RandomString rs = new RandomString();
		String s1 = rs.getRandomString();
		String s2 = rs.getRandomString();
		assertFalse(s1.equals(s2));
	}
}
