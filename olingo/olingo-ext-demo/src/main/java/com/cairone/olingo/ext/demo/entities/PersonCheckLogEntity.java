package com.cairone.olingo.ext.demo.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.cairone.olingo.ext.demo.edm.enums.CheckTypeEnum;

@Entity @Table(name="people_check_in_out_logs")
public class PersonCheckLogEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="event_id")
	private Long id = null;

	@OneToOne @JoinColumn(name="person_id", nullable=false)
	private PersonEntity person = null;
	
	@Column(name="type", nullable=false)
	private CheckTypeEnum checkType = null;
	
	@Column(name="check_datetime", nullable=false)
	private Date datetime = null;
	
	public PersonCheckLogEntity() {}

	public PersonCheckLogEntity(PersonEntity person, CheckTypeEnum checkType, Date datetime) {
		super();
		this.person = person;
		this.checkType = checkType;
		this.datetime = datetime;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PersonEntity getPerson() {
		return person;
	}

	public void setPerson(PersonEntity person) {
		this.person = person;
	}

	public CheckTypeEnum getCheckType() {
		return checkType;
	}

	public void setCheckType(CheckTypeEnum checkType) {
		this.checkType = checkType;
	}

	public Date getDatetime() {
		return datetime;
	}

	public void setDatetime(Date checkIn) {
		this.datetime = checkIn;
	}

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
		PersonCheckLogEntity other = (PersonCheckLogEntity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PersonCheckLogEntity [id=" + id + "]";
	}
}
