package com.sindice.linker.provider;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sindice.linker.domain.Role;
import com.sindice.linker.domain.User;
import com.sindice.linker.domain.UserRole;

@Component
@Configurable
public class PrepopulateDb implements ApplicationListener<ContextRefreshedEvent> {

	Logger logger = LoggerFactory.getLogger(PrepopulateDb.class);
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (event.getApplicationContext().getParent() == null) {
			// root context
			// Now check are we in test mode 
			String runMode = "server";
            try {
                Resource resource = new ClassPathResource("/env.properties");
                Properties props = PropertiesLoaderUtils.loadProperties(resource);
                runMode = props.getProperty("run.mode");
            } catch (IOException ignored) {
            }
            logger.info("run.mode = " + runMode);
            if ("server".equals(runMode)) {
            	// here create initial data as we are in production and parent context
    			init();
            }
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
    private void init() {
		if (!User.findAllUsers().isEmpty()) { // don't do anything if there is data in the db
            return;
        }
		if (!Role.findAllRoles().isEmpty()) { // don't do anything if there is data in the db
            return;
        }
		if (!UserRole.findAllUserRoles().isEmpty()) { // don't do anything if there is data in the db
            return;
        }
		
		logger.info("Prepopulating db should fire only if db was completele empty");
		
		MessageDigestPasswordEncoder messageDigestPasswordEncoder = new org.springframework.security.authentication.encoding.ShaPasswordEncoder(256);
		
		Role role = new Role();
		role.setRoleName("ROLE_USER");
		role.setRoleDescription("Regular user");
		role.persist();
		
		Role adminRole = new Role();
		adminRole.setRoleName("ROLE_ADMIN");
		adminRole.setRoleDescription("Admin user");
		adminRole.persist();
		
		User user = new User();
		user.setEmailAddress("admin@example.com");
		user.setUsername("admin");
		// TODO:
		// here place already encoded one
		// get it from application config
		user.setPassword(messageDigestPasswordEncoder.encodePassword("123456", "admin@example.com"));
		user.setFirstName("Admin");
		user.setLastName("Admin");
		user.setEnabled(true);
		user.persist();
		
		UserRole userRole = new UserRole();
		userRole.setUserEntry(user);
		userRole.setRoleEntry(role);
		userRole.persist();

		userRole = new UserRole();
		userRole.setUserEntry(user);
		userRole.setRoleEntry(adminRole);
		userRole.persist();
	}
	
	
	

}
