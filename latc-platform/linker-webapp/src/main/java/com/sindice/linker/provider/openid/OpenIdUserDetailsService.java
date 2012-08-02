package com.sindice.linker.provider.openid;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.sindice.linker.domain.Role;
import com.sindice.linker.domain.User;
import com.sindice.linker.domain.UserRole;

public class OpenIdUserDetailsService implements UserDetailsService {

    public UserDetails loadUserByUsername(String openIdIdentifier) {
        List<User> userList = User.findUsersByOpenIdIdentifier(openIdIdentifier).getResultList();
        User user = userList.size() == 0 ? null : userList.get(0);

        if (user == null) {
            throw new UsernameNotFoundException("User not found for OpenID: " + openIdIdentifier);
        } else {
            if (!user.getEnabled()) {
                throw new DisabledException("User is disabled");
            }
            
            
            List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
            List<Role> list = UserRole.findUserRoles(user.getId()).getResultList();
            for(Role role:list){
            	authorities.add(new GrantedAuthorityImpl(role.getRoleName()));
            }
            
            //TODO: here check all booleans 
            // enabled, expired, locked etc ...
            
            return (UserDetails)  new org.springframework.security.core.userdetails.User(
          	      user.getEmailAddress(),
          	      user.getPassword(),
          	      true, // enabled 
          	      true, // account not expired
          	      true, // credentials not expired 
          	      true, // account not locked
          	      authorities
          	    );
        }
    }

}
