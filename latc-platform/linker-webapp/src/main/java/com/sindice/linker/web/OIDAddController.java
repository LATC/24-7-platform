package com.sindice.linker.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import net.tanesha.recaptcha.ReCaptcha;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.sindice.linker.domain.User;

@RequestMapping("/oidadd/**")
@Controller
public class OIDAddController {

	@Autowired
	ReCaptcha reCaptcha;
    
	@Autowired
    private OIDAddValidator validator;

    @Autowired
    private transient MailSender mailSender;

    private transient SimpleMailMessage simpleMailMessage;

	
    @ModelAttribute("User")
    public UserOpenIdAddForm formBackingObject() {
        return new UserOpenIdAddForm();
    }

    @RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(Model model) {
    	model.addAttribute("contextPrefix", "../");
    	
    	UserOpenIdAddForm form = new UserOpenIdAddForm();
        
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
        
    	model.addAttribute("User", form);
        model.addAttribute("captcha_form", reCaptcha.createRecaptchaHtml(null, null));
        return "oidadd/index";
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public String create(@Valid UserOpenIdAddForm userOpenIdForm, BindingResult result, Model model, HttpServletRequest request) {
    	model.addAttribute("contextPrefix", "../");
    	
    	validator.validate(userOpenIdForm, result);
        if (result.hasErrors()) {
        	return createForm(model);
        } else {
            // here get the user via email 
        	// and add openid
        	//TODO: !!!  check that I can add an open id to another account in this way !!!
        	// to see the form user must authenticate as an owner of that email 
        	// but what if he change the email and submit the form 
        	List<User> list = User.findUsersByEmailAddress(userOpenIdForm.getEmailAddress()).getResultList();
        	if(list.size()==1){
        		User user = list.get(0);
        		user.setOpenIdIdentifier(userOpenIdForm.getOpenIdIdentifier());
        		user.merge();
                SimpleMailMessage mail = new SimpleMailMessage();
        		mail.setTo(user.getEmailAddress());
        		mail.setSubject("User Activaton");
        		
        		mail.setText("Hi "+user.getUsername()+"\n"+
        		user.getFirstName()+ user.getLastName() +", you had added an openid to your existing account. \n\n Thanks");
                mailSender.send(mail);
                return "oidadd/thanks";
        	}
        }
       	return "oidadd/error";
    }

    @RequestMapping
    public String index(Model model) {
    	model.addAttribute("contextPrefix", "../");
    	
    	return "oidadd/index";
    }

    @RequestMapping
    public String thanks(Model model) {
    	model.addAttribute("contextPrefix", "../");
    	
    	return "oidadd/thanks";
    }
    @RequestMapping
    public String error(Model model) {
    	model.addAttribute("contextPrefix", "../");
    	
    	return "oidadd/error";
    }

    public void sendMessage(String mailTo, String message) {
        simpleMailMessage.setTo(mailTo);
        simpleMailMessage.setText(message);
        mailSender.send(simpleMailMessage);
    }
}
