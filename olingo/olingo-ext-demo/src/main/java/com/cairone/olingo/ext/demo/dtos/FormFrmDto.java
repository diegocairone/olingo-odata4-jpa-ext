package com.cairone.olingo.ext.demo.dtos;

import com.cairone.olingo.ext.demo.edm.resources.FormEdm;


public class FormFrmDto {

	private String id = null;
	private String name = null;

	public FormFrmDto() {}

	public FormFrmDto(String id, String name) {
		super();
		this.id = id;
		this.name = name == null ? null : name.trim().toUpperCase();
	}

	public FormFrmDto(FormEdm formEdm) {
		this(formEdm.getId(), formEdm.getName());
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name == null ? null : name.trim().toUpperCase();
	}
}
