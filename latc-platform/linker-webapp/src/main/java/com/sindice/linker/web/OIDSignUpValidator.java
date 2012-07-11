package com.sindice.linker.web;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

	/**
	 * @author rohit
	 * 
	 */
	@Service("oIDsignupValidator")
	public class OIDSignUpValidator implements Validator {

		@Autowired
		ReCaptcha reCaptcha;
        
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.springframework.validation.Validator#supports(java.lang.Class)
		 */
		@Override
		public boolean supports(Class<?> clazz) {
			return UserOpenIdRegistrationForm.class.equals(clazz);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.springframework.validation.Validator#validate(java.lang.Object,
		 * org.springframework.validation.Errors)
		 */
		@Override
		public void validate(Object target, Errors errors) {
			UserOpenIdRegistrationForm form = (UserOpenIdRegistrationForm) target;
			
	        String challenge = form.getRecaptcha_challenge_field();
	        String uresponse = form.getRecaptcha_response_field();
	        ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer("localhost", challenge, uresponse);
	        if (!reCaptchaResponse.isValid()) {
	        	errors.reject("recaptcha.mismatch");
	        }
		}

	}
