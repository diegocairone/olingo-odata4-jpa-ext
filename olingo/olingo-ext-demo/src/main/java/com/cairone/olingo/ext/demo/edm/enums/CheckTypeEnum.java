package com.cairone.olingo.ext.demo.edm.enums;

import com.cairone.olingo.ext.demo.AppDemoConstants;
import com.cairone.olingo.ext.jpa.annotations.EdmEnum;
import com.cairone.olingo.ext.jpa.interfaces.OdataEnum;

@EdmEnum(namespace = AppDemoConstants.NAME_SPACE, name = "CheckType")
public enum CheckTypeEnum implements OdataEnum<CheckTypeEnum> {
	CHECK_IN(1),
	CHECK_OUT(2);

	private int value;
	private int ordinal;

	private CheckTypeEnum(int value) {
		this.value = value;
		switch (value) {
		case 1:
			ordinal = 0;
			break;
		default:
			ordinal = 1;
			break;
		}
	}

	public int getDbValor() {
		return value;
	}

	@Override
	public int getOrdinal() {
		return ordinal;
	}

	@Override
	public CheckTypeEnum setOrdinal(int ordinal) {
		this.ordinal = ordinal;
		switch (ordinal) {
		case 0:
			value = 1;
			return CHECK_IN;
		default:
			value = 2;
			return CHECK_OUT;
		}
	}
}
