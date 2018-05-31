package com.cairone.olingo.ext.demo.dtos;

import com.cairone.olingo.ext.demo.edm.resources.FormTypeEdm;

public class FormTypeFrmDto {

	private Integer id = null;
	private String name = null;

	public FormTypeFrmDto() {}

	public FormTypeFrmDto(Integer id, String name) {
		super();
		this.id = id;
		this.name = name == null ? null : name.trim().toUpperCase();
	}

	public FormTypeFrmDto(FormTypeEdm formTypeEdm) {
		this(formTypeEdm.getId(), formTypeEdm.getName());
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
