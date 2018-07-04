package com.cairone.olingo.ext.jpa.interfaces;

import java.util.Optional;

import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;

public interface QueryOptions {

	public Optional<ExpandOption> getExpandOption();
	public Optional<FilterOption> getFilterOption();
	public Optional<OrderByOption> getOrderByOption();
	
	public boolean isSkipByLib();
	public boolean isTopByLib();
	public int getSkip();
	public int getTop();
}
