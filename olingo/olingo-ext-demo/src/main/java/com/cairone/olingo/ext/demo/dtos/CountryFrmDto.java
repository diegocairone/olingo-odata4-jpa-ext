package com.cairone.olingo.ext.demo.dtos;

import com.cairone.olingo.ext.demo.edm.resources.CountryEdm;

public class CountryFrmDto {
	
	private Integer id = null;
	private String name = null;

	public CountryFrmDto() {}

	public CountryFrmDto(Integer id, String name) {
		super();
		this.id = id;
		this.name = name == null ? null : name.trim().toUpperCase();
	}

	public CountryFrmDto(CountryEdm countryEdm) {
		this(countryEdm.getId(), countryEdm.getName());
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name == null ? null : name.trim().toUpperCase();
	}
}