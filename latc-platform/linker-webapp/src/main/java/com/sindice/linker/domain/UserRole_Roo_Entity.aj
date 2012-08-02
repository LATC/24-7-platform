// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.sindice.linker.domain;

import com.sindice.linker.domain.UserRole;
import java.lang.Integer;
import java.lang.Long;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Version;
import org.springframework.transaction.annotation.Transactional;

privileged aspect UserRole_Roo_Entity {
    
    declare @type: UserRole: @Entity;
    
    @PersistenceContext
    transient EntityManager UserRole.entityManager;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long UserRole.id;
    
    @Version
    @Column(name = "version")
    private Integer UserRole.version;
    
    public Long UserRole.getId() {
        return this.id;
    }
    
    public void UserRole.setId(Long id) {
        this.id = id;
    }
    
    public Integer UserRole.getVersion() {
        return this.version;
    }
    
    public void UserRole.setVersion(Integer version) {
        this.version = version;
    }
    
    @Transactional
    public void UserRole.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void UserRole.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            UserRole attached = UserRole.findUserRole(this.id);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void UserRole.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public void UserRole.clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional
    public UserRole UserRole.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        UserRole merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
    public static final EntityManager UserRole.entityManager() {
        EntityManager em = new UserRole().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long UserRole.countUserRoles() {
        return entityManager().createQuery("SELECT COUNT(o) FROM UserRole o", Long.class).getSingleResult();
    }
    
    public static List<UserRole> UserRole.findAllUserRoles() {
        return entityManager().createQuery("SELECT o FROM UserRole o", UserRole.class).getResultList();
    }
    
    public static UserRole UserRole.findUserRole(Long id) {
        if (id == null) return null;
        return entityManager().find(UserRole.class, id);
    }
    
    public static List<UserRole> UserRole.findUserRoleEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM UserRole o", UserRole.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
}
