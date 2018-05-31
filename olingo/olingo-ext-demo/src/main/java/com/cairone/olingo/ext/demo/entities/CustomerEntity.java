package com.cairone.olingo.ext.demo.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity @Table(name="customers")
public class CustomerEntity implements Serializable {
	
	private static final long serialVersionUID = 5656073630325504414L;

	@Id @Column(name="customer_id")
	private Integer id = null;
	
	@OneToOne @PrimaryKeyJoinColumn(name="customer_id", referencedColumnName="person_id")
	private PersonEntity person = null;
	
	@Column(name="company_name", nullable=false)
	private String companyName = null;
	
	public CustomerEntity() {}

	public CustomerEntity(PersonEntity person, String companyName) {
		super();
		this.id = person == null ? null : person.getId();
		this.person = person;
		this.companyName = companyName;
	}

	public PersonEntity getPerson() {
		return person;
	}

	public void setPerson(PersonEntity personEntity) {
		this.person = personEntity;
		this.id = personEntity == null ? null : personEntity.getId();
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
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
		CustomerEntity other = (CustomerEntity) obj;
		if (person == null) {
			if (other.person != null)
				return false;
		} else if (!person.equals(other.person))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CustomerEntity [personEntity=" + person + ", companyName=" + companyName + "]";
	}
}
