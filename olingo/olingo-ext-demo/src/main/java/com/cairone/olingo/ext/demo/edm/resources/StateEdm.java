package com.cairone.olingo.ext.demo.edm.resources;

import com.cairone.olingo.ext.demo.AppDemoConstants;
import com.cairone.olingo.ext.demo.entities.StateEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntitySet;
import com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;
import com.cairone.olingo.ext.jpa.annotations.ODataJPAEntity;

@EdmEntity(name = "State", key = "Id", namespace = AppDemoConstants.NAME_SPACE, containerName = AppDemoConstants.CONTAINER_NAME)
@EdmEntitySet("States")
@ODataJPAEntity(entity=StateEntity.class)
public class StateEdm {

	@EdmProperty(name = "Id")
	private Integer id;

	@EdmProperty(name = "Name", nullable= false, maxLength=200)
	private String name;
	
	@EdmNavigationProperty(name = "Country")
	private CountryEdm country = null;
	
	public StateEdm() {}
	
	public StateEdm(Integer id, String name) {
		this(id, name, null);
	}

	public StateEdm(Integer id, String name, CountryEdm country) {
		super();
		this.id = id;
		this.name = name;
		this.country = country;
	}
	
	public StateEdm(StateEntity stateEntity) {
		this(	stateEntity.getId(),
				stateEntity.getName(),
				new CountryEdm(stateEntity.getCountry())
		);
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

	public CountryEdm getCountry() {
		return country;
	}

	public void setCountry(CountryEdm country) {
		this.country = country;
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
		StateEdm other = (StateEdm) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StateEdm [id=" + id + ", name=" + name + "]";
	}
}
