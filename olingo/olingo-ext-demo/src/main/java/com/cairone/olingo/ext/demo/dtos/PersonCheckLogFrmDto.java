package com.cairone.olingo.ext.demo.dtos;

import java.util.Date;

import com.cairone.olingo.ext.demo.edm.enums.CheckTypeEnum;
import com.cairone.olingo.ext.demo.edm.resources.PersonCheckLogEdm;
import com.cairone.olingo.ext.demo.utils.DateUtil;

public class PersonCheckLogFrmDto {

	private Long id = null;
	private Integer personId = null;
	private CheckTypeEnum checkType = null;
	private Date datetime = null;
	
	public PersonCheckLogFrmDto() {}

	public PersonCheckLogFrmDto(Long id, Integer personId, CheckTypeEnum checkType, Date datetime) {
		super();
		this.id = id;
		this.personId = personId;
		this.checkType = checkType;
		this.datetime = datetime;
	}
	
	public PersonCheckLogFrmDto(PersonCheckLogEdm personCheckLogEdm) {
		this.id = personCheckLogEdm.getId();
		this.personId = personCheckLogEdm.getPerson() == null ? null : personCheckLogEdm.getPerson().getId();
		this.checkType = personCheckLogEdm.getCheckType();
		this.datetime = DateUtil.asDate(personCheckLogEdm.getMoment());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getPersonId() {
		return personId;
	}

	public void setPersonId(Integer personId) {
		this.personId = personId;
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

	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}
}
