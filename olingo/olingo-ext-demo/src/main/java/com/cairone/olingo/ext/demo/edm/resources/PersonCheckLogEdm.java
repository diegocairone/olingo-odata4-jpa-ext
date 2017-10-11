package com.cairone.olingo.ext.demo.edm.resources;

import java.time.LocalDateTime;

import com.cairone.olingo.ext.demo.AppDemoConstants;
import com.cairone.olingo.ext.demo.edm.enums.CheckTypeEnum;
import com.cairone.olingo.ext.demo.entities.PersonCheckLogEntity;
import com.cairone.olingo.ext.demo.utils.DateUtil;
import com.cairone.olingo.ext.jpa.annotations.EdmEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntitySet;
import com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;
import com.cairone.olingo.ext.jpa.annotations.ODataJPAEntity;

@EdmEntity(name = "PersonCheckLog", key = "Id", namespace = AppDemoConstants.NAME_SPACE, containerName = AppDemoConstants.CONTAINER_NAME)
@EdmEntitySet("PeopleCheckLogs")
@ODataJPAEntity("PersonCheckLogEntity")
public class PersonCheckLogEdm {
	
	@EdmProperty(name = "Id")
	private Long id = null;

	@EdmNavigationProperty(name = "Person")
	private PersonEdm person = null;

	@EdmProperty(name = "CheckType")
	private CheckTypeEnum checkType = null;
	
	@EdmProperty(name = "Moment")
	private LocalDateTime moment = null;
	
	public PersonCheckLogEdm() {}

	public PersonCheckLogEdm(Long id, PersonEdm person, CheckTypeEnum checkType, LocalDateTime moment) {
		super();
		this.id = id;
		this.person = person;
		this.checkType = checkType;
		this.moment = moment;
	}

	public PersonCheckLogEdm(PersonCheckLogEntity personCheckLogEntity) {
		this(
				personCheckLogEntity.getId(),
				new PersonEdm(personCheckLogEntity.getPerson()),
				personCheckLogEntity.getCheckType(),
				DateUtil.asLocalDateTime(personCheckLogEntity.getDatetime()));
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PersonEdm getPerson() {
		return person;
	}

	public void setPerson(PersonEdm person) {
		this.person = person;
	}

	public CheckTypeEnum getCheckType() {
		return checkType;
	}

	public void setCheckType(CheckTypeEnum checkType) {
		this.checkType = checkType;
	}

	public LocalDateTime getMoment() {
		return moment;
	}

	public void setMoment(LocalDateTime moment) {
		this.moment = moment;
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
		PersonCheckLogEdm other = (PersonCheckLogEdm) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PersonCheckLogEdm [id=" + id + "]";
	}
}
