package com.cairone.olingo.ext.demo.datasources;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;

import com.cairone.olingo.ext.jpa.interfaces.ConditionalDataSource;

public abstract class AbstractConditionalDataSource extends AbstractDataSource implements ConditionalDataSource {

	@Override
	public Iterable<?> readConditioned(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption, Object conditionalEntity) throws ODataApplicationException {
		return null;
	}
}
