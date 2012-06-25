package com.sindice.linker.web;

import java.security.Principal;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sindice.linker.domain.User;

@RequestMapping("/member/**")
@Controller
public class MemberController {

    @RequestMapping(value="")
    public String index(Model uiModel, Principal principal) {
    	 final String emailAddress = principal.getName();
    	 TypedQuery<User> usersQuery =  User.findUsersByEmailAddress(emailAddress);
    	 try{
         	User user=usersQuery.getSingleResult();
         	System.out.println(user.getUsername());
        	uiModel.addAttribute("user", user);
    	 }catch(Exception e){
    		// ignore  
    	 }
    	 
    	uiModel.addAttribute("contextPrefix", "../");
    	
    	
    	
    	return "member/index";
    }

}
