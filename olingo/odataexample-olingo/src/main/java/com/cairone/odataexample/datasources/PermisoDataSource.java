package com.cairone.odataexample.datasources;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.edm.resources.PermisoEdm;
import com.cairone.odataexample.entities.PermisoEntity;
import com.cairone.odataexample.services.PermisoService;
import com.cairone.olingo.ext.jpa.interfaces.DataSource;
import com.cairone.olingo.ext.jpa.interfaces.DataSourceProvider;

@Component
public class PermisoDataSource implements DataSourceProvider, DataSource {

	private static final String ENTITY_SET_NAME = "Permisos";

	@Autowired private PermisoService permisoService = null;
	
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
	public DataSource getDataSource() {
		return this;
	}

	@Override
	public Object readFromKey(Map<String, UriParameter> keyPredicateMap) throws ODataException {
		
		String permisoID = keyPredicateMap.get("id").getText();
		
		PermisoEntity permisoEntity = permisoService.buscarPorNombre(permisoID);
		PermisoEdm permisoEdm = permisoEntity == null ? null : new PermisoEdm(permisoEntity);
		
		return permisoEdm;
	}

	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) throws ODataException {

		List<Sort.Order> orderByList = new ArrayList<Sort.Order>();
		
		if(orderByOption != null) {
			orderByOption.getOrders().forEach(orderByItem -> {
				
				Expression expression = orderByItem.getExpression();
				if(expression instanceof Member){
					
					UriInfoResource resourcePath = ((Member)expression).getResourcePath();
					UriResource uriResource = resourcePath.getUriResourceParts().get(0);
					
				    if (uriResource instanceof UriResourcePrimitiveProperty) {
				    	EdmProperty edmProperty = ((UriResourcePrimitiveProperty)uriResource).getProperty();
						Direction direction = orderByItem.isDescending() ? Direction.DESC : Direction.ASC;
						String property = edmProperty.getName();
						orderByList.add(new Order(direction, property));
				    }
				}
				
			});
		}
		
		return permisoService.ejecutarConsulta(null, orderByList).stream().map(e -> new PermisoEdm(e)).collect(Collectors.toList());
	}
}
