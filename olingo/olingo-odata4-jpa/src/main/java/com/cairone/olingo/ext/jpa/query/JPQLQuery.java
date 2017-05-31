package com.cairone.olingo.ext.jpa.query;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

public final class JPQLQuery {

	private final String queryString;
    private final Map<String, Object> queryParams;
    
	public JPQLQuery(String queryString, Map<String, Object> queryParams) {
		super();
		this.queryString = queryString;
		this.queryParams = queryParams;
	}

	public String getQueryString() {
		return queryString;
	}

	public Map<String, Object> getQueryParams() {
		return queryParams;
	}

	@Override
	public String toString() {
		return queryString + ", params=" + queryParams;
	}

    @SuppressWarnings("unchecked")
	public static <T> List<T> execute(EntityManagerFactory emf, JPQLQuery jpaQuery) {

        EntityManager em = emf.createEntityManager();

        String queryString = jpaQuery.getQueryString();
        
        Query query = em.createQuery(queryString);
        Map<String, Object> queryParams = jpaQuery.getQueryParams();

        try {
        	em.getTransaction().begin();

            for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }

            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
