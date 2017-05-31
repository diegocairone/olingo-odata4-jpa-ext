package com.cairone.odataexample.datasources;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManagerFactory;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.edm.resources.PermisoEdm;
import com.cairone.odataexample.entities.PermisoEntity;
import com.cairone.odataexample.services.PermisoService;
import com.cairone.olingo.ext.jpa.interfaces.DataSource;
import com.cairone.olingo.ext.jpa.query.JPQLQuery;
import com.cairone.olingo.ext.jpa.query.JPQLQueryBuilder;
import com.google.common.base.CharMatcher;

@Component
public class PermisoDataSource implements DataSource {

	private static final String ENTITY_SET_NAME = "Permisos";

	@Autowired private PermisoService permisoService = null;

	@Autowired
    private EntityManagerFactory entityManagerFactory;
	
	@Autowired
	private MessageSource messageSource = null;

	@Override
	public Object create(Object entity) throws ODataException {
		throw new ODataApplicationException("OPERACION NO PERMITIDA", HttpStatusCode.FORBIDDEN.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, List<String> propertiesInJSON, boolean isPut) throws ODataException {
		throw new ODataApplicationException("OPERACION NO PERMITIDA", HttpStatusCode.FORBIDDEN.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap) throws ODataException {
		throw new ODataApplicationException("OPERACION NO PERMITIDA", HttpStatusCode.FORBIDDEN.getStatusCode(), Locale.ENGLISH);
	}

	@Override
	public String isSuitableFor() {
		return ENTITY_SET_NAME;
	}

	@Override
	public Object readFromKey(Map<String, UriParameter> keyPredicateMap) throws ODataException {
		
		String permisoID = CharMatcher.is('\'').trimFrom( keyPredicateMap.get("id").getText() );
		
		PermisoEntity permisoEntity = permisoService.buscarPorNombre(permisoID);
		PermisoEdm permisoEdm = permisoEntity == null ? null : new PermisoEdm(permisoEntity);
		
		return permisoEdm;
	}

	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataException {

		JPQLQuery query = new JPQLQueryBuilder()
			.setDistinct(true)
			.setClazz(PermisoEdm.class)
			.setExpandOption(expandOption)
			.setFilterOption(filterOption)
			.setOrderByOption(orderByOption)
			.build();

		List<PermisoEntity> permisoEntities = JPQLQuery.execute(entityManagerFactory, query);
		List<PermisoEdm> permisoEdms = permisoEntities.stream().map(entity -> { return new PermisoEdm(entity); }).collect(Collectors.toList());
		
		return permisoEdms;
	}
}
