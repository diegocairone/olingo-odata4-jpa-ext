package com.cairone.olingo.ext.demo.edm.resources;

import java.util.Arrays;
import java.util.List;

import com.cairone.olingo.ext.demo.AppDemoConstants;
import com.cairone.olingo.ext.demo.entities.UserEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntitySet;
import com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;
import com.cairone.olingo.ext.jpa.annotations.ODataQueryDslEntity;
import com.cairone.olingo.ext.jpa.annotations.ODataQueryDslProperty;

@EdmEntity(name = "User", key = "Id", namespace = AppDemoConstants.NAME_SPACE, containerName = AppDemoConstants.CONTAINER_NAME)
@EdmEntitySet("Users")
@ODataQueryDslEntity(jpaentity=UserEntity.class, variable="userEntity")
public class UserEdm {

	@EdmProperty(name = "Id")
	private Integer id = null;
	
	@EdmProperty(name = "Name")
	private String name = null;
	
	@EdmProperty(name = "Surname")
	private String surname = null;
	
	@EdmNavigationProperty(name = "Form") @ODataQueryDslProperty("person.form")
	private FormEdm form = null;
	
	@EdmProperty(name = "Username")
	private String username = null;
	
	@EdmProperty(name = "Password")
	private String password = null;
	
	@EdmProperty(name = "Tags")
	private List<String> tags = null;
	
	public UserEdm() {
		this.tags = Arrays.asList("TAG-1", "TAG-2", "TAG-3");
	}

	public UserEdm(Integer id, String name, String surname, FormEdm form, String username, String password) {
		this();
		this.id = id;
		this.name = name;
		this.surname = surname;
		this.form = form;
		this.username = username;
		this.password = password;
	}
	
	public UserEdm(UserEntity userEntity) {
		this(	userEntity.getPerson().getId(),
				userEntity.getPerson().getName(),
				userEntity.getPerson().getSurname(),
				userEntity.getPerson().getForm() == null ? null : new FormEdm(userEntity.getPerson().getForm()),
				userEntity.getName(),
				userEntity.getPassword());
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

	public FormEdm getForm() {
		return form;
	}

	public void setForm(FormEdm form) {
		this.form = form;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "UserEdm [id=" + id + ", name=" + name + "]";
	}
}
