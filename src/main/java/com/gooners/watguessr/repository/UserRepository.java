package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class UserRepository extends EntityRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public void create(User user) {
        entityManager.persist(user);
    }
    public void update(User user) {
        entityManager.merge(user);
    }

    public boolean usernameExists(String username) {
        try {
            return (entityManager.createQuery("select u from User u where u.username = :username")
                    .setParameter("username", username)
                    .getSingleResult()) != null;
        } catch (NoResultException e) {
            return false;
        }
    }

    public boolean emailAddressExists(String emailAddress) {
        try {
            return (entityManager.createQuery("select u from User u where u.emailAddress = :emailAddress")
                    .setParameter("emailAddress", emailAddress)
                    .getSingleResult()) != null;
        } catch (NoResultException e) {
            return false;
        }
    }

}
