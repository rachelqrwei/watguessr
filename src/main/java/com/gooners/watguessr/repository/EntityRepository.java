package com.gooners.watguessr.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public class EntityRepository {

    @PersistenceContext
    protected EntityManager entityManager;

    public <T> List<T> findAll(Class<T> entityClass) {
        String jpql = "select entity from " + entityClass.getName() + " entity";
        return entityManager.createQuery(jpql, entityClass)
                .getResultList();
    }

    public <T> T find(Class<T> entityClass, Object id) {
        return entityManager.find(entityClass, id);
    }

    public <T> void delete(Class<T> entityClass, Object id) {
        T entity = entityManager.find(entityClass, id);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }
}