package com.cairone.olingo.ext.demo.edm.resources;

import com.cairone.olingo.ext.demo.AppDemoConstants;
import com.cairone.olingo.ext.jpa.annotations.EdmComplex;
import com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;
import com.cairone.olingo.ext.jpa.annotations.ODataQueryDslProperty;

@EdmComplex(name="Address", namespace = AppDemoConstants.NAME_SPACE)
public class PersonAddressEdm {

	@EdmProperty(name = "Name") @ODataQueryDslProperty("addressStreet")
	private String name = null;
	
	@EdmProperty(name = "Number") @ODataQueryDslProperty("addressNumber")
	private String number = null;
	
	@EdmNavigationProperty(name = "State") @ODataQueryDslProperty("state")
	private StateEdm state = null;
	
	public PersonAddressEdm() {}

	public PersonAddressEdm(String name, String number, StateEdm state) {
		super();
		this.name = name;
		this.number = number;
		this.state = state;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public StateEdm getState() {
		return state;
	}

	public void setState(StateEdm state) {
		this.state = state;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((number == null) ? 0 : number.hashCode());
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
		PersonAddressEdm other = (PersonAddressEdm) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (number == null) {
			if (other.number != null)
				return false;
		} else if (!number.equals(other.number))
			return false;
		return true;
	}
}
