package com.cairone.olingo.ext.demo.dtos;

import com.cairone.olingo.ext.demo.edm.enums.GenderEnum;
import com.cairone.olingo.ext.demo.edm.enums.RegionEnum;
import com.cairone.olingo.ext.demo.edm.resources.PersonEdm;

public class PersonFrmDto {

	private Integer id = null;
	private String name = null;
	private String surname = null;
	private GenderEnum gender = null;
	private RegionEnum region = null;
	private String formId = null;
	
	public PersonFrmDto() {}

	public PersonFrmDto(Integer id, String name, String surname, GenderEnum gender, RegionEnum region, String formId) {
		super();
		this.id = id;
		this.name = name == null ? null : name.trim().toUpperCase();
		this.surname = surname == null ? null : surname.trim().toUpperCase();
		this.gender = gender;
		this.region = region;
		this.formId = formId == null ? null : formId.trim().toUpperCase();
	}

	public PersonFrmDto(PersonEdm personEdm) {
		this(personEdm.getId(), personEdm.getName(), personEdm.getSurname(), personEdm.getGender(), personEdm.getRegion(), personEdm.getForm() == null ? null : personEdm.getForm().getId());
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

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname == null ? null : surname.trim().toUpperCase();
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

	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId == null ? null : formId.trim().toUpperCase();
	}
}
