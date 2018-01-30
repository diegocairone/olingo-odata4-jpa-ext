package com.cairone.olingo.ext.jpa.interfaces;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;

public interface ConditionalDataSource extends DataSource {

	Iterable<?> readConditioned(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption, Object conditionalEntity) throws ODataApplicationException;
}
