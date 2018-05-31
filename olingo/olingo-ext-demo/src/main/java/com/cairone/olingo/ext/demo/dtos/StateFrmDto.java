package com.cairone.olingo.ext.demo.dtos;

import com.cairone.olingo.ext.demo.edm.resources.StateEdm;

public class StateFrmDto {

	private Integer id = null;
	private String name = null;
	private Integer countryId = null;
	
	public StateFrmDto() {}

	public StateFrmDto(Integer id, String name, Integer countryId) {
		super();
		this.id = id;
		this.name = name;
		this.countryId = countryId;
	}
	
	public StateFrmDto(StateEdm stateEdm) {
		this(stateEdm.getId(), stateEdm.getName(), stateEdm.getCountry() == null ? null : stateEdm.getCountry().getId());
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
		this.name = name;
	}

	public Integer getCountryId() {
		return countryId;
	}

	public void setCountryId(Integer countryId) {
		this.countryId = countryId;
	}
}
