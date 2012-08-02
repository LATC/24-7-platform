package com.sindice.linker.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.sindice.linker.provider.DatabaseAuthenticationProvider;

/*
 * only applicationContext.xml is different 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(locations = {"classpath:/META-INF/spring/applicationContext-security.xml",
								   "classpath:/META-INF/spring-test/applicationContext.xml"})
public class DatabaseAuthenticationProviderTest {
	
	@Autowired
	DatabaseAuthenticationProvider databaseAuthenticationProvider;
	
	private String username = "john";
	private String email = "john@example.com";
	private String password = "password";
	private String lastName = "Smith";
	private String firstName = "John";
	// password "password" salted with username john@example.com
	private String saltedEncodedPassword  = "427fc1746f4f9a6580854dc96425f2dcea198675af62b86ee8c36675e341b2b8";
	
	@Transactional
	@Test
	public void testCreateUserUserDetails() {
		createEnabledUserInDB();
		// now check that this user is can be successfully authenticated
		assertUserDetails();

		databaseAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(email, password));
		try {
			databaseAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(email, "wrong_password"));
			fail("Login with wrong password happens");
		} catch (BadCredentialsException e) {
			// Expected
		}
		
	}

		private void assertUserDetails() {
			// here check that user exist and the details are ok 
			com.sindice.linker.domain.User user = com.sindice.linker.domain.User.findUsersByEmailAddress(email).getSingleResult();
			assertEquals(email,user.getEmailAddress());
			assertEquals(saltedEncodedPassword,user.getPassword());
		}

		protected void createEnabledUserInDB() {
			com.sindice.linker.domain.User user = new com.sindice.linker.domain.User();
			user.setUsername(username);
			user.setEmailAddress(email);
			user.setPassword(saltedEncodedPassword);
			user.setFirstName(firstName);
			user.setLastName(lastName);
			user.setEnabled(true);
			user.persist();
		}
		
	
}