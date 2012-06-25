package com.sindice.linker.domain;

import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.validation.constraints.NotNull;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooEntity(finders = { "findUserRolesByUserEntry" })
public class UserRole {

    @NotNull
    @ManyToOne
    private User userEntry;

    @NotNull
    @ManyToOne
    private Role roleEntry;

   
    public static Query findUserRoles(Long userId) {
        EntityManager em = UserRole.entityManager();
        Query q = em.createNativeQuery("SELECT role.id,role.role_description, role.role_name, role.version FROM role LEFT JOIN user_role ON  role.id =user_role.role_entry WHERE user_role.user_entry = :user_id", Role.class);
        q.setParameter("user_id",userId);
        return q;
    }

    
}
