package com.cairone.olingo.ext.demo.edm.resources;

import com.cairone.olingo.ext.demo.AppDemoConstants;
import com.cairone.olingo.ext.demo.entities.CountryEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntitySet;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;
import com.cairone.olingo.ext.jpa.annotations.ODataJPAEntity;

@EdmEntity(name = "Country", key = "Id", namespace = AppDemoConstants.NAME_SPACE, containerName = AppDemoConstants.CONTAINER_NAME)
@EdmEntitySet("Countries")
@ODataJPAEntity("CountryEntity")
public class CountryEdm {
	
	@EdmProperty(name = "Id")
	private Integer id = null;
	
	@EdmProperty(name = "Name")
	private String name = null;

	public CountryEdm() {}

	public CountryEdm(Integer id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public CountryEdm(CountryEntity countryEntity) {
		this(countryEntity.getId(), countryEntity.getName());
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
