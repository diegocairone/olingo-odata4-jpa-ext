package com.cairone.olingo.ext.demo.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity @Table(name="countries")
public class CountryEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id @Column(name="country_id")
	private Integer id = null;

	@Column(name="name", unique=true, nullable=false)
	private String name = null;
	
	@Column(name="phone_code", nullable=true)
	private Integer phoneCode = null;
	
//	@OneToMany(mappedBy="country", fetch=FetchType.EAGER)
//	private List<StateEntity> states = null;
	
	public CountryEntity() {}

	public CountryEntity(Integer id, String name) {
		super();
		this.id = id;
		this.name = name;
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

	public Integer getPhoneCode() {
		return phoneCode;
	}

	public void setPhoneCode(Integer phoneCode) {
		this.phoneCode = phoneCode;
	}

//	public List<StateEntity> getStates() {
//		return states;
//	}
//
//	public void setStates(List<StateEntity> states) {
//		this.states = states;
//	}

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
		CountryEntity other = (CountryEntity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CountryEntity [id=" + id + ", name=" + name + "]";
	}
}