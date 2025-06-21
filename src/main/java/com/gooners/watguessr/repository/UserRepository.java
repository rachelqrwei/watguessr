package com.gooners.watguessr.repository;

import com.gooners.watguessr.entity.User;
import jakarta.persistence.NoResultException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class UserRepository extends EntityRepository<User, UUID> {
    protected UserRepository(Class entityClass) {
        super(entityClass);
    }

    public void create(User user) {
        entityManager.persist(user);
    }

    public void update(User user) {
        entityManager.merge(user);
    }

    public boolean usernameExists(String username) {
        try {
            return entityManager.createQuery("SELECT u FROM User u WHERE u.username = :username")
                    .setParameter("username", username)
                    .getSingleResult() != null;
        } catch (NoResultException e) {
            return false;
        }
    }

    public User findByUsername(String username) {
        try {
            return entityManager.createQuery(
                            "SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public boolean emailAddressExists(String emailAddress) {
        try {
            return entityManager.createQuery("SELECT u FROM User u WHERE u.emailAddress = :emailAddress")
                    .setParameter("emailAddress", emailAddress)
                    .getSingleResult() != null;
        } catch (NoResultException e) {
            return false;
        }
    }

    //leaderboard, default sort is elo DESC
    public List<User> findSorted(String keyword, String sortBy, int page, int pageSize) {
        StringBuilder jpql = new StringBuilder("SELECT u FROM User u");

        boolean hasKeyword = keyword != null && !keyword.isBlank();
        if (hasKeyword) {
            jpql.append(" WHERE u.username LIKE :kw");
        }

        switch (sortBy != null ? sortBy : "") {
            case "createdAtAsc":
                jpql.append(" ORDER BY u.createdAt ASC");
                break;
            case "createdAtDesc":
                jpql.append(" ORDER BY u.createdAt DESC");
                break;
            case "elo":
            default:
                jpql.append(" ORDER BY u.elo DESC");
                break;
        }

        var query = entityManager.createQuery(jpql.toString(), User.class);
        if (hasKeyword) {
            query.setParameter("kw", "%" + keyword + "%");
        }

        return query
                .setFirstResult(page * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }


}
