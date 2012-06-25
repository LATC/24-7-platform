package com.sindice.linker.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.sindice.linker.domain.User;
import com.sindice.linker.domain.UserRole;

@RooWebScaffold(path = "admin/users", formBackingObject = User.class)
@RequestMapping("/admin/users")
@Controller
public class UserController {
	@Autowired
	private MessageDigestPasswordEncoder messageDigestPasswordEncoder;

	@RequestMapping(method = RequestMethod.POST)
    public String create(@Valid User user, BindingResult result, Model model, HttpServletRequest request) {
        if (result.hasErrors()) {
            model.addAttribute("user", user);
            addDateTimeFormatPatterns(model);
            return "admin/users/create";
        }
        if(user.getId() != null){
        	User savedUser = User.findUser(user.getId());
        	if(!savedUser.getPassword().equals(user.getPassword())){
        		user.setPassword(messageDigestPasswordEncoder.encodePassword(user.getPassword(), user.getEmailAddress()));
        	}
        } else {
    		user.setPassword(messageDigestPasswordEncoder.encodePassword(user.getPassword(), user.getEmailAddress()));
        }
        
        
        user.persist();
        return "redirect:/admin/users/" + encodeUrlPathSegment(user.getId().toString(), request);
    }
	
	
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        // first remove all userRoles for this user
    	User user = User.findUser(id);
    	List<UserRole> userRoleList = UserRole.findUserRolesByUserEntry(user).getResultList();
    	for(UserRole userRole:userRoleList){
    		userRole.remove();
    	}
    	user.remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/admin/users";
    }

    

}
