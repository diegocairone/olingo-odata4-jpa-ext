package com.cairone.olingo.ext.demo.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity @Table(name="forms")
public class FormEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id @Column(name="form_name")
	private String id;

	@Column(name="form_description", unique=true, nullable=false)
	private String name;
	
	@OneToOne @JoinColumn(name="type_id", nullable=false)
	private FormTypeEntity formType = null;

	public FormEntity() {}
	
	public FormEntity(String id, String name) {
		this(id, name, null);
	}
	
	public FormEntity(String id, String name, FormTypeEntity formType) {
		super();
		this.id = id;
		this.name = name;
		this.formType = formType;
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

	public FormTypeEntity getFormType() {
		return formType;
	}

	public void setFormType(FormTypeEntity formType) {
		this.formType = formType;
	}
}
