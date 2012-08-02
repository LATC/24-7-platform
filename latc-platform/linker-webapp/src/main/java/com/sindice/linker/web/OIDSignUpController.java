package com.sindice.linker.web;

import java.util.Date;

import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import net.tanesha.recaptcha.ReCaptcha;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.sindice.linker.domain.User;
import com.sindice.linker.provider.openid.PasswordGenerator;

@RequestMapping("/oidsignup/**")
@Controller
public class OIDSignUpController {

    
	@Autowired
    private ReCaptcha reCaptcha;

	@Autowired
    private OIDSignUpValidator validator;

    @Autowired
    private transient MailSender mailSender;

    @Autowired
	private MessageDigestPasswordEncoder messageDigestPasswordEncoder;

    @ModelAttribute("User")
    public UserOpenIdRegistrationForm formBackingObject() {
        return new UserOpenIdRegistrationForm();
    }

    @RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(Model model) {
    	model.addAttribute("contextPrefix", "../");
    	
    	UserOpenIdRegistrationForm form = new UserOpenIdRegistrationForm();
        
    	ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession();
    	if(session!=null && session.getAttribute("USER")!=null){
    		User user = (User) session.getAttribute("USER");
    		form.setUsername(user.getUsername());
    		form.setEmailAddress(user.getEmailAddress());
    		form.setFirstName(user.getFirstName());
    		form.setLastName(user.getLastName());
    		form.setOpenIdIdentifier(user.getOpenIdIdentifier());
    	}
        System.out.println("SSSSSSSSS");
    	model.addAttribute("User", form);
    	model.addAttribute("captcha_form", reCaptcha.createRecaptchaHtml(null, null));
        return "oidsignup/index";
    }
    
    @RequestMapping(params = "activate", method = RequestMethod.GET)
    public String activateUser(Model uiModel,@RequestParam(value = "activate", required = true) String activationKey,@RequestParam(value = "emailAddress", required = true) String emailAddress,Model model) {
    	uiModel.addAttribute("contextPrefix", "../");
    	
    	// TODO: this one is never called and can be removed 
    	//TypedQuery<User> query = User.findUsersByActivationKeyAndEmailAddress(activationKey, emailAddress);
    	TypedQuery<User> query = User.findUsersWaitingToBeAcivateByActivationKeyAndEmailAddress(activationKey, emailAddress);
        User User=query.getSingleResult();
        if(null!=User){
        	User.setActivationDate(new Date());
        	User.setEnabled(true);
        	User.merge();
        	return "login";
        }else{
        	return "oidsignup/error";
        }

    }

    @RequestMapping(method = RequestMethod.POST)
    public String create(Model uiModel,@Valid UserOpenIdRegistrationForm userRegistration, BindingResult result, Model model, HttpServletRequest httpServletRequest) {
    	uiModel.addAttribute("contextPrefix", "../");
    	
    	validator.validate(userRegistration, result);
        if (result.hasErrors()) {
        	return createForm(model);
        } else {
            String activationKey = "activationKey:" + new RandomString().getRandomString();

            User User = new User();
            User.setActivationDate(null);
            User.setEmailAddress(userRegistration.getEmailAddress());
            User.setUsername(userRegistration.getUsername());
            User.setFirstName(userRegistration.getFirstName());
            User.setLastName(userRegistration.getLastName());
            User.setOpenIdIdentifier(userRegistration.getOpenIdIdentifier());
            User.setPassword(messageDigestPasswordEncoder.encodePassword(PasswordGenerator.generateString(16), userRegistration.getEmailAddress()));
            User.setActivationKey(activationKey);
            User.setEnabled(false);
            User.setLocked(false);
            User.persist();
            
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
    		simpleMailMessage.setTo(User.getEmailAddress());
    		simpleMailMessage.setSubject("User Activaton");
    		
    		String baseUrl = (String) httpServletRequest.getSession().getServletContext().getAttribute("applicationBaseUrl");
    		simpleMailMessage.setText("Hi "+User.getUsername()+"\n"+
    				User.getFirstName()+" "+User.getFirstName()+
    				", you had registered with us. Please click on this link to activate your account - \n"
    				+baseUrl+"/signup?emailAddress="+User.getEmailAddress()+"&activate="+activationKey+"\" \n\n" +
    				"Thanks Admin");
    		
    		mailSender.send(simpleMailMessage);
            return "oidsignup/thanks";
        }
    }

    @RequestMapping
    public String index(Model uiModel) {
    	uiModel.addAttribute("contextPrefix", "../");
    	
    	return "oidsignup/index";
    }

    @RequestMapping
    public String thanks(Model uiModel) {
    	uiModel.addAttribute("contextPrefix", "../");
    	
    	return "oidsignup/thanks";
    }
    @RequestMapping
    public String error(Model uiModel) {
    	uiModel.addAttribute("contextPrefix", "../");
    	
    	return "oidsignup/error";
    }

}
