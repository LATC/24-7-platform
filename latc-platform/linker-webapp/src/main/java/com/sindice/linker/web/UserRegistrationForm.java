/**
 * 
 */
package com.sindice.linker.web;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * @author rohit
 * 
 */
public class UserRegistrationForm {

	
	@NotNull
	@Size(min = 1)
	@Pattern(regexp = "^[A-Za-z0-9_-]+$")
    private String username;
	
	@NotNull
	@Size(min = 1)
	private String firstName;
	@NotNull
	@Size(min = 1)
	private String lastName;
	@NotNull
	@Size(min = 1)
	private String emailAddress;
	@NotNull
	@Size(min = 1)
	private String password;
	@NotNull
	@Size(min = 1)
	private String repeatPassword;

	private String recaptcha_challenge_field;

	private String recaptcha_response_field;
	
	private String openIdIdentifier;
	
	
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getOpenIdIdentifier() {
		return openIdIdentifier;
	}

	public void setOpenIdIdentifier(String openIdIdentifier) {
		this.openIdIdentifier = openIdIdentifier;
	}

	/**
	 * @return the emailAddress
	 */
	public String getEmailAddress() {
		return emailAddress;
	}

	/**
	 * @param emailAddress
	 *            the emailAddress to set
	 */
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the repeatPassword
	 */
	public String getRepeatPassword() {
		return repeatPassword;
	}

	/**
	 * @param repeatPassword the repeatPassword to set
	 */
	public void setRepeatPassword(String repeatPassword) {
		this.repeatPassword = repeatPassword;
	}
	
	public String getRecaptcha_challenge_field() {

		return recaptcha_challenge_field;

	}

	public void setRecaptcha_challenge_field(String recaptcha_challenge_field) {

		this.recaptcha_challenge_field = recaptcha_challenge_field;

	}

	public String getRecaptcha_response_field() {

		return recaptcha_response_field;

	}

	public void setRecaptcha_response_field(String recaptcha_response_field) {

		this.recaptcha_response_field = recaptcha_response_field;

	}
}
