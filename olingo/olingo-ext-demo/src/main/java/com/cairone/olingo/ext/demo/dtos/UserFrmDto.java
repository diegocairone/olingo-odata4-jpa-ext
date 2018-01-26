package com.cairone.olingo.ext.demo.dtos;

import com.cairone.olingo.ext.demo.edm.resources.UserEdm;

public class UserFrmDto {

	private Integer id = null;
	private String username = null;
	private String password = null;
	
	public UserFrmDto() {}

	public UserFrmDto(Integer id, String username, String password) {
		super();
		this.id = id;
		this.username = username;
		this.password = password;
	}
	
	public UserFrmDto(UserEdm userEdm) {
		this(	userEdm.getId(),
				userEdm.getUsername(),
				userEdm.getPassword());
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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
	
}
