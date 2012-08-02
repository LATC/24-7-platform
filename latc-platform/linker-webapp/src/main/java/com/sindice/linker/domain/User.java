package com.sindice.linker.domain;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.persistence.Column;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import org.springframework.format.annotation.DateTimeFormat;

@RooJavaBean
@RooToString
@RooEntity(finders = { "findUsersByEmailAddress", "findUsersByActivationKeyAndEmailAddress", "findUsersByOpenIdIdentifier", "findUsersByUsername" })
public class User {

    @NotNull
    @Size(min = 1)
    private String firstName;

    @NotNull
    @Size(min = 1)
    private String lastName;

    @NotNull
    @Column(unique = true)
    @Size(min = 1)
    @Pattern(regexp = "^[A-Za-z0-9_-]+$")
    private String username;

    @NotNull
    @Column(unique = true)
    @Size(min = 1)
    private String emailAddress;

    @NotNull
    @Size(min = 1)
    private String password;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date activationDate;

    private String activationKey;

    private Boolean enabled;

    private Boolean locked;

    @Column(name = "openid_identifier")
    private String openIdIdentifier;

    public static TypedQuery<User> findUsersWaitingToBeAcivateByActivationKeyAndEmailAddress(String activationKey, String emailAddress) {
        if (activationKey == null || activationKey.length() == 0) throw new IllegalArgumentException("The activationKey argument is required");
        if (emailAddress == null || emailAddress.length() == 0) throw new IllegalArgumentException("The emailAddress argument is required");
        EntityManager em = User.entityManager();
        TypedQuery<User> q = em.createQuery("SELECT o FROM User AS o WHERE o.activationKey = :activationKey AND o.emailAddress = :emailAddress " + "AND o.enabled=false AND locked=false ", User.class);
        q.setParameter("activationKey", activationKey);
        q.setParameter("emailAddress", emailAddress);
        return q;
    }
}
