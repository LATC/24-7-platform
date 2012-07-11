package com.sindice.linker.web;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sindice.linker.domain.User;

@RequestMapping("/changepassword/**")
@Controller
public class ChangePasswordController {
	@Autowired
	private ChangePasswordValidator validator;

	@Autowired
	private MessageDigestPasswordEncoder messageDigestPasswordEncoder;

	@ModelAttribute("changePasswordForm")
	public ChangePasswordForm formBackingObject() {
		return new ChangePasswordForm();
	}

	@RequestMapping(value = "/changepassword/index")
	public String index(Model uiModel) {
		uiModel.addAttribute("contextPrefix", "../");
    	
		if (SecurityContextHolder.getContext().getAuthentication()
				.isAuthenticated()) {
			return "changepassword/index";
		} else {
			return "login";
		}
	}

	@RequestMapping(value = "/changepassword/update", method = RequestMethod.POST)
	public String update(Model uiModel,
			@ModelAttribute("changePasswordForm") ChangePasswordForm form,
			BindingResult result) {
		uiModel.addAttribute("contextPrefix", "../");
    	
		validator.validate(form, result);
		if (result.hasErrors()) {
			return "changepassword/index"; // back to form
		} else {
			if (SecurityContextHolder.getContext().getAuthentication()
					.isAuthenticated()) {
				UserDetails userDetails = (UserDetails) SecurityContextHolder
						.getContext().getAuthentication().getPrincipal();
				String newPassword = form.getNewPassword();
				Query query = User
						.findUsersByEmailAddress(userDetails.getUsername());
				User person = (User) query.getSingleResult();
				person.setPassword(messageDigestPasswordEncoder.encodePassword(newPassword, person.getEmailAddress()));
				person.merge();
				return "changepassword/thanks";
			} else {
				return "login";
			}
		}
	}

	@RequestMapping(value = "/changepassword/thanks")
	public String thanks(Model uiModel) {
		uiModel.addAttribute("contextPrefix", "../");
    	
		return "changepassword/thanks";
	}

}
