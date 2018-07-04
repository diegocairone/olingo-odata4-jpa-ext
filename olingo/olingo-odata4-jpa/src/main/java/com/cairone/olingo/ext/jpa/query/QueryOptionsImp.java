package com.cairone.olingo.ext.jpa.query;

import java.util.Optional;

import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;

import com.cairone.olingo.ext.jpa.interfaces.QueryOptions;

public class QueryOptionsImp implements QueryOptions {

	private Optional<ExpandOption> expandOption = Optional.empty();
	private Optional<FilterOption> filterOption = Optional.empty();
	private Optional<OrderByOption> orderByOption = Optional.empty();
	
	private boolean skipByLib = true;
	private boolean topByLib = true;
	private int skip = 0;
	private int top = Integer.MAX_VALUE;
	
	public QueryOptionsImp() {}

	public QueryOptionsImp(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption) {
		super();
		if(expandOption != null) this.expandOption = Optional.of(expandOption);
		if(filterOption != null) this.filterOption = Optional.of(filterOption);
		if(orderByOption != null) this.orderByOption = Optional.of(orderByOption);
	}

	public Optional<ExpandOption> getExpandOption() {
		return expandOption;
	}

	public void setExpandOption(Optional<ExpandOption> expandOption) {
		this.expandOption = expandOption;
	}

	public Optional<FilterOption> getFilterOption() {
		return filterOption;
	}

	public void setFilterOption(Optional<FilterOption> filterOption) {
		this.filterOption = filterOption;
	}

	public Optional<OrderByOption> getOrderByOption() {
		return orderByOption;
	}

	public void setOrderByOption(Optional<OrderByOption> orderByOption) {
		this.orderByOption = orderByOption;
	}

	public boolean isSkipByLib() {
		return skipByLib;
	}

	public void setSkipByLib(boolean skipByLib) {
		this.skipByLib = skipByLib;
	}

	public boolean isTopByLib() {
		return topByLib;
	}

	public void setTopByLib(boolean topByLib) {
		this.topByLib = topByLib;
	}

	public int getSkip() {
		return skip;
	}

	public void setSkip(int skip) {
		this.skip = skip;
	}

	public int getTop() {
		return top;
	}

	public void setTop(int top) {
		this.top = top;
	}
}
