package com.cairone.olingo.ext.demo.edm.resources;

import com.cairone.olingo.ext.demo.AppDemoConstants;
import com.cairone.olingo.ext.demo.edm.enums.GenderEnum;
import com.cairone.olingo.ext.demo.edm.enums.RegionEnum;
import com.cairone.olingo.ext.demo.entities.PersonEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntitySet;
import com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;
import com.cairone.olingo.ext.jpa.annotations.ODataJPAEntity;

@EdmEntity(name = "Person", key = "Id", namespace = AppDemoConstants.NAME_SPACE, containerName = AppDemoConstants.CONTAINER_NAME)
@EdmEntitySet("People")
@ODataJPAEntity("PersonEntity")
public class PersonEdm {
	
	@EdmProperty(name = "Id")
	private Integer id = null;
	
	@EdmProperty(name = "Name")
	private String name = null;
	
	@EdmProperty(name = "Surname")
	private String surname = null;
	
	@EdmProperty(name = "Gender")
	private GenderEnum gender = null;
	
	@EdmProperty(name = "Region")
	private RegionEnum region = null;
	
	@EdmNavigationProperty(name = "Form")
	private FormEdm form = null;
	
	@EdmProperty(name = "Address")
	private PersonAddressEdm address = null;
	
	public PersonEdm() {}

	public PersonEdm(Integer id, String name, String surname, GenderEnum gender, RegionEnum region, FormEdm form, PersonAddressEdm address) {
		super();
		this.id = id;
		this.name = name;
		this.surname = surname;
		this.gender = gender;
		this.region = region;
		this.form = form;
		this.address = address;
	}
	
	public PersonEdm(PersonEntity personEntity) {
		this(personEntity.getId(), 
				personEntity.getName(), 
				personEntity.getSurname(), 
				personEntity.getGender(), 
				personEntity.getRegion() == null ? null : RegionEnum.fromDb(personEntity.getRegion().getId()),
				personEntity.getForm() == null ? null : new FormEdm(personEntity.getForm()), 
				null);
		
		if(personEntity.getAddressNumber() != null || personEntity.getAddressStreet() != null) {
			this.address = new PersonAddressEdm();
			if(personEntity.getAddressStreet() != null) this.address.setName(personEntity.getAddressStreet());
			if(personEntity.getAddressNumber() != null) this.address.setNumber(personEntity.getAddressNumber());
		}
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

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public GenderEnum getGender() {
		return gender;
	}

	public void setGender(GenderEnum gender) {
		this.gender = gender;
	}

	public RegionEnum getRegion() {
		return region;
	}

	public void setRegion(RegionEnum region) {
		this.region = region;
	}

	public FormEdm getForm() {
		return form;
	}

	public void setForm(FormEdm form) {
		this.form = form;
	}

	public PersonAddressEdm getAddress() {
		return address;
	}

	public void setAddress(PersonAddressEdm address) {
		this.address = address;
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
		PersonEdm other = (PersonEdm) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
