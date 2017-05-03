package com.cairone.olingo.ext.jpa.query;

import java.util.Map;

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
}
