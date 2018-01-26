package com.cairone.olingo.ext.demo.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity @Table(name="users")
public class UserEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id @Column(name="user_id")
	private Integer id = null;
	
	@OneToOne @PrimaryKeyJoinColumn(name="user_id")
	private PersonEntity person = null;
	
	@Column(name="user_name", length=100, nullable=false, unique=true)
	private String name = null;
	
	@Column(name="user_password", length=45, nullable=false)
	private String password = null;
	
	public UserEntity() {}

	public UserEntity(PersonEntity person, String name, String password) {
		super();
		this.id = person == null ? null : person.getId();
		this.person = person;
		this.name = name;
		this.password = password;
	}

	public PersonEntity getPerson() {
		return person;
	}

	public void setPerson(PersonEntity person) {
		this.id = person == null ? null : person.getId();
		this.person = person;		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((person == null) ? 0 : person.hashCode());
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
		UserEntity other = (UserEntity) obj;
		if (person == null) {
			if (other.person != null)
				return false;
		} else if (!person.equals(other.person))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "UserEntity [person=" + person + ", name=" + name + "]";
	}
	
}
