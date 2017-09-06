package com.cairone.olingo.ext.demo.edm.enums;

import com.cairone.olingo.ext.demo.AppDemoConstants;
import com.cairone.olingo.ext.jpa.annotations.EdmEnum;
import com.cairone.olingo.ext.jpa.interfaces.OdataEnum;

@EdmEnum(namespace = AppDemoConstants.NAME_SPACE, name = "Region")
public enum RegionEnum implements OdataEnum<RegionEnum> {
	SUDAMERICA(10),
	EUROPE(20),
	ASIA(30);
	
	private int value;
	private int ordinal;
	
	private RegionEnum(int inicial) {
		this.value = inicial;
		switch (inicial) {
		case 30:
			ordinal = 2;
			break;
		case 20:
			ordinal = 1;
			break;
		case 10:
		default:
			ordinal = 0;
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
	public RegionEnum setOrdinal(int ordinal) {
		this.ordinal = ordinal;
		switch (ordinal) {
		case 1:
			value = 20;
			return EUROPE;
		case 2:
			value = 30;
			return ASIA;
		case 0:
		default:
			value = 10;
			return SUDAMERICA;
		}
	}
	
	public static RegionEnum fromDb(int value) {
		switch(value) {
		case 30:
			return ASIA;
		case 20:
			return EUROPE;
		default:
			return SUDAMERICA;
		}
	}
}
