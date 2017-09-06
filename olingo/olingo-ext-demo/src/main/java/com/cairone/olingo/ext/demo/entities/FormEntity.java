package com.cairone.olingo.ext.demo.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity @Table(name="forms")
public class FormEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id @Column(name="form_name")
	private String id;

	@Column(name="form_description", unique=true, nullable=false)
	private String name;

	public FormEntity() {}

	public FormEntity(String id, String name) {
		super();
		this.id = id;
		this.name = name;
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
		this.name = name;
	}
}
