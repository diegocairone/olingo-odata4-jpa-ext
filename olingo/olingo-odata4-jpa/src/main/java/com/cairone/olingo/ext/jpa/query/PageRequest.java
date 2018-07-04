package com.cairone.olingo.ext.jpa.query;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageRequest implements Pageable {
	
	private int offset = 0;
	private int limit = Integer.MAX_VALUE;
	private Sort sort = null;
	
	public static PageRequest of(int offset, int limit) {
		return of(offset, limit, Sort.unsorted());
	}

	public static PageRequest of(int offset, int limit, Sort sort) {
		return new PageRequest(offset, limit, sort);
	}
	
	private PageRequest(int offset, int limit, Sort sort) {
		super();
		this.offset = offset;
		this.limit = limit;
		this.sort = sort;
	}

	@Override
	public int getPageNumber() {
		return 0;
	}

	@Override
	public int getPageSize() {
		return limit;
	}

	@Override
	public long getOffset() {
		return offset;
	}

	@Override
	public Sort getSort() {
		return sort;
	}

	@Override
	public Pageable next() {
		return null;
	}

	@Override
	public Pageable previousOrFirst() {
		return null;
	}

	@Override
	public Pageable first() {
		return null;
	}

	@Override
	public boolean hasPrevious() {
		return false;
	}
}
