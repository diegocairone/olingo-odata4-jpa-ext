package com.cairone.olingo.ext.demo.datasources;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cairone.olingo.ext.demo.edm.resources.CustomerEdm;
import com.cairone.olingo.ext.demo.entities.CustomerEntity;
import com.cairone.olingo.ext.demo.exceptions.ODataNotImplementedException;
import com.cairone.olingo.ext.demo.services.CustomerService;
import com.cairone.olingo.ext.demo.utils.OdataExceptionParser;
import com.cairone.olingo.ext.jpa.query.QuerydslQuery;
import com.cairone.olingo.ext.jpa.query.QuerydslQueryBuilder;

@Component
public class CustomersDataSource extends AbstractDataSource {

	private static final String ENTITY_SET_NAME = "Customers";

	@Autowired private CustomerService customerService = null;
	
	@Override
	public String isSuitableFor() {
		return ENTITY_SET_NAME;
	}

	@Override
	public Object create(Object entity, Object superentity) throws ODataApplicationException {
		throw new ODataNotImplementedException("OPERATION NOT IMPLEMENTED YET");
	}

	@Override
	public Object update(Map<String, UriParameter> keyPredicateMap, Object entity, Object superentity, List<String> propertiesInJSON, boolean isPut) throws ODataApplicationException {
		throw new ODataNotImplementedException("OPERATION NOT IMPLEMENTED YET");
	}

	@Override
	public Object delete(Map<String, UriParameter> keyPredicateMap, Object superentity) throws ODataApplicationException {
		throw new ODataNotImplementedException("OPERATION NOT IMPLEMENTED YET");
	}

	@Override
	public Object readFromKey(Map<String, UriParameter> keyPredicateMap, ExpandOption expandOption, SelectOption selectOption, Object superentity) throws ODataApplicationException {

		Integer customerID = Integer.valueOf( keyPredicateMap.get("Id").getText() );
		
		try {
			CustomerEntity customerEntity = customerService.findOne(customerID);
			CustomerEdm customerEdm = new CustomerEdm(customerEntity);
			
			return customerEdm;
		} catch (Exception e) {
			throw OdataExceptionParser.parse(e);
		}
	}

	@Override
	public Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption, Object parentEntity) throws ODataApplicationException {
		
		QuerydslQuery dslQuery = new QuerydslQueryBuilder()
				.setFilterOption(filterOption)
				.setFilterOption(filterOption)
				.setClazz(CustomerEdm.class)
				.build();
		
		LOG.debug("QUERYDSL: {}", dslQuery);
		
		List<CustomerEntity> customerEntities = QuerydslQuery.execute(customerService.getCustomerRepository(), dslQuery);
		List<CustomerEdm> customerEdms = customerEntities.stream()
				.map(entity -> { 
					CustomerEdm customerEdm = new CustomerEdm(entity);
					return customerEdm;
				})
				.collect(Collectors.toList());
		
		return customerEdms;
	}
}
