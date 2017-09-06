package com.cairone.olingo.ext.demo.edm.enums;

import com.cairone.olingo.ext.demo.AppDemoConstants;
import com.cairone.olingo.ext.jpa.annotations.EdmEnum;
import com.cairone.olingo.ext.jpa.interfaces.OdataEnum;

@EdmEnum(namespace = AppDemoConstants.NAME_SPACE, name = "Gender")
public enum GenderEnum implements OdataEnum<GenderEnum> {
	FEMALE('F'),
	MALE('M');
	
	private char value;
	private int ordinal;
	
	private GenderEnum(char value) {
		this.value = value;
		switch (value) {
		case 'F':
			ordinal = 0;
			break;
		default:
			ordinal = 1;
			break;
		}
	}

	public char getDbValor() {
		return value;
	}

	@Override
	public int getOrdinal() {
		return ordinal;
	}

	@Override
	public GenderEnum setOrdinal(int ordinal) {
		this.ordinal = ordinal;
		switch (ordinal) {
		case 0:
			value = 'F';
			return FEMALE;
		default:
			value = 'M';
			return MALE;
		}
	}
}
