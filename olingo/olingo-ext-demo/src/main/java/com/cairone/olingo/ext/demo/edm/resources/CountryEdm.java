package com.cairone.olingo.ext.demo.edm.resources;

import java.util.List;

import com.cairone.olingo.ext.demo.AppDemoConstants;
import com.cairone.olingo.ext.demo.entities.CountryEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntitySet;
import com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;
import com.cairone.olingo.ext.jpa.annotations.ODataQueryDslEntity;

@EdmEntity(name = "Country", key = "Id", namespace = AppDemoConstants.NAME_SPACE, containerName = AppDemoConstants.CONTAINER_NAME)
@EdmEntitySet("Countries")
@ODataQueryDslEntity(jpaentity=CountryEntity.class, variable="countryEntity")
public class CountryEdm {
	
	@EdmProperty(nullable=false)
	private Integer id = null;
	
	@EdmProperty(nullable=false)
	private String name = null;

	@EdmProperty
	private Integer phoneCode = null;
	
	@EdmNavigationProperty
	private List<StateEdm> states = null;

	public CountryEdm() {}

	public CountryEdm(Integer id, String name, Integer phoneCode) {
		this(id, name, phoneCode, null);
	}
	
	public CountryEdm(Integer id, String name, Integer phoneCode, List<StateEdm> states) {
		super();
		this.id = id;
		this.name = name;
		this.phoneCode = phoneCode;
		this.states = states;
	}

	public CountryEdm(CountryEntity countryEntity) {
		this(countryEntity.getId(), countryEntity.getName(), countryEntity.getPhoneCode());
//		if(!countryEntity.getStates().isEmpty()) {
//			this.states = countryEntity.getStates()
//					.stream().map(stateEntity -> {
//						return new StateEdm(stateEntity.getId(), stateEntity.getName());
//					})
//					.collect(Collectors.toList());
//		}
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

	public Integer getPhoneCode() {
		return phoneCode;
	}

	public void setPhoneCode(Integer phoneCode) {
		this.phoneCode = phoneCode;
	}

	public List<StateEdm> getStates() {
		return states;
	}

	public void setStates(List<StateEdm> states) {
		this.states = states;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CountryEdm other = (CountryEdm) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
