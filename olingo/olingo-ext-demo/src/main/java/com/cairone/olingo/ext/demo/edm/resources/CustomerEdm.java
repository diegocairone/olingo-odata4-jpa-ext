package com.cairone.olingo.ext.demo.edm.resources;

import com.cairone.olingo.ext.demo.AppDemoConstants;
import com.cairone.olingo.ext.demo.edm.enums.GenderEnum;
import com.cairone.olingo.ext.demo.edm.enums.RegionEnum;
import com.cairone.olingo.ext.jpa.annotations.EdmEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntitySet;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;

@EdmEntity(name = "Customer", key = "Id", namespace = AppDemoConstants.NAME_SPACE, containerName = AppDemoConstants.CONTAINER_NAME)
@EdmEntitySet("Customers")
public class CustomerEdm extends PersonEdm {

	@EdmProperty(name = "CompanyName")
	private String companyName = null;
	
	public CustomerEdm() {
		super();
	}
	
	public CustomerEdm(String companyName, Integer id, String name, String surname, GenderEnum gender, RegionEnum region, FormEdm form, PersonAddressEdm address) {
		super(id, name, surname, gender, region, form, address);
		this.companyName = companyName;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
}
