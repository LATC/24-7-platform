package com.sindice.linker.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import net.tanesha.recaptcha.ReCaptcha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.sindice.linker.domain.Role;
import com.sindice.linker.domain.User;
import com.sindice.linker.domain.UserRole;
import com.sindice.linker.provider.CustomRequestResponseAction;
import com.sindice.linker.provider.HashAuthenticationToken;

@RequestMapping("/signup/**")
@Controller
public class SignUpController {
	Logger logger = LoggerFactory.getLogger(SignUpController.class);
	
	
	@Autowired
	CustomRequestResponseAction action;
	
	
	@Autowired
	private ReCaptcha reCaptcha;
    
	@Autowired
    private SignUpValidator validator;

    @Autowired
    private transient MailSender mailSender;

    @Autowired
	private MessageDigestPasswordEncoder messageDigestPasswordEncoder;

	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private AuthenticationManager authenticationManager;
	
    @ModelAttribute("User")
    public UserRegistrationForm formBackingObject() {
        return new UserRegistrationForm();
    }

    @RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(Model model) {
    	model.addAttribute("contextPrefix", "../");
    	UserRegistrationForm form = new UserRegistrationForm();
        model.addAttribute("User", form);
        model.addAttribute("captcha_form",reCaptcha.createRecaptchaHtml(null, null));
        return "signup/index";
    }
    
    @RequestMapping(params = "activate", method = RequestMethod.GET)
    public String activateUser(@RequestParam(value = "activate", required = true) String activationKey,@RequestParam(value = "emailAddress", required = true) String emailAddress,HttpServletRequest request, HttpServletResponse response,Model model) {
    	model.addAttribute("contextPrefix", "../");

    	TypedQuery<User> usersQuery = User.findUsersWaitingToBeAcivateByActivationKeyAndEmailAddress(activationKey, emailAddress);
    	TypedQuery<Role> rolesQuery = Role.findRolesByRoleName("ROLE_USER");
    	try{
        	User user=usersQuery.getSingleResult();
	        Role role = rolesQuery.getSingleResult();
        	if(null!=user){
	        	user.setActivationDate(new Date());
	        	user.setEnabled(true);
	        	user.merge();
	        	
	        	// here assign USER_ROLE 
	        	UserRole userRole = new UserRole();
	        	userRole.setUserEntry(user);
	        	userRole.setRoleEntry(role);
	        	userRole.persist();
	        	
	        	if(this.autoLogin(request, response, user)){
	        		// here try redirect here
	        		return "redirect:/member/index"; // here maybe a page saying that everything was ok 
	            }
	        	return "login";
	        }else{
	        	String msg = "There is no user: "+emailAddress+" waiting to be activated";
	        	logger.error(msg);
	        	model.addAttribute("errorMsg",msg);
	        	return "signup/error";
	        }
        }catch(Exception e){
        	// TODO: here check that maybe user is already logged on 
        	// if so display different message
        	
        	String msg = "Exception: There is no user: "+emailAddress+" waiting to be activated";
        	logger.error(msg,e);
        	model.addAttribute("errorMsg",msg);
        	return "signup/error";
        }
    }

    
    public boolean autoLogin(HttpServletRequest request,HttpServletResponse response, User user) {
        try{
    		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        	TypedQuery<UserRole> roleQuery=UserRole.findUserRolesByUserEntry(user);
	        List<UserRole> userRoles = roleQuery.getResultList();
	        for(UserRole userRole:userRoles){
	        	authorities.add(new GrantedAuthorityImpl(userRole.getRoleEntry().getRoleName()));
	        } 	
        	// create a user 
	        org.springframework.security.core.userdetails.User springUser = new org.springframework.security.core.userdetails.User(
	      	      user.getEmailAddress(),
	    	      user.getPassword(),
	    	      true, // enabled 
	    	      true, // account not expired
	    	      true, // credentials not expired 
	    	      true, // account not locked
	    	      authorities
	    	    );
        	
        	
        	
          // Must be called from request filtered by Spring Security, otherwise SecurityContextHolder is not updated
          HashAuthenticationToken token = new HashAuthenticationToken(springUser, springUser.getPassword(), springUser.getAuthorities());
          
          request.getSession();
          token.setDetails(new WebAuthenticationDetails(request));
          
          
          
          
          Authentication authentication = authenticationManager.authenticate(token);
          
          logger.info("Logging in with {}", authentication.getPrincipal());
          logger.info("isAuthenticated: {}",authentication.isAuthenticated());
          SecurityContextHolder.getContext().setAuthentication(authentication);
          logger.info("Finish autoLogin");
          
          //here also do the after login action 
          action.success(request, response,  authentication);
          
          return true;
        } catch (Exception e) {
          SecurityContextHolder.getContext().setAuthentication(null);
          logger.error("Failure in autoLogin", e);
        }
        return false;
      }
    
    @RequestMapping(method = RequestMethod.POST)
    public String create(@Valid UserRegistrationForm userRegistration, BindingResult result, Model model, HttpServletRequest httpServletRequest) {
    	model.addAttribute("contextPrefix", "../");

    	validator.validate(userRegistration, result);
        if (result.hasErrors()) {
        	
            return createForm(model);
        } else {
        	String activationKey = "activationKey:" + new RandomString().getRandomString();

            User User = new User();
            User.setActivationDate(null);
            User.setUsername(userRegistration.getUsername());
            User.setEmailAddress(userRegistration.getEmailAddress());
            User.setFirstName(userRegistration.getFirstName());
            User.setLastName(userRegistration.getLastName());
            User.setPassword(messageDigestPasswordEncoder.encodePassword(userRegistration.getPassword(), userRegistration.getEmailAddress()));
            User.setActivationKey(activationKey);
            User.setEnabled(false);
            User.setLocked(false);
            User.persist();
            
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setTo(User.getEmailAddress());
    		simpleMailMessage.setSubject("User Activaton");
    		
    		
    		//TODO: duplication in opisignup controller
    		String baseUrl = (String) httpServletRequest.getSession().getServletContext().getAttribute("applicationBaseUrl");
    		simpleMailMessage.setText("Hi "+User.getFirstName()+".\n" +
    				"You had registered with us. Please click on this link to activate your account - \n" +
    				baseUrl+"/signup?emailAddress="+User.getEmailAddress()+"&activate="+activationKey+"\n\n" +
    				"Thanks Admin");
    		mailSender.send(simpleMailMessage);
            return "signup/thanks";
        }
    }

    @RequestMapping
    public String index(Model model) {
    	model.addAttribute("contextPrefix", "../");
    	return "signup/index";
    }

    @RequestMapping
    public String thanks(Model model) {
    	model.addAttribute("contextPrefix", "../");
        return "signup/thanks";
    }
    @RequestMapping
    public String error(Model model) {
    	model.addAttribute("contextPrefix", "../");
        return "signup/error";
    }
}
