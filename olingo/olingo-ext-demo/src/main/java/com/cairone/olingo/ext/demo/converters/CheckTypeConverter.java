package com.cairone.olingo.ext.demo.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.cairone.olingo.ext.demo.edm.enums.CheckTypeEnum;

@Converter(autoApply = true)
public class CheckTypeConverter implements AttributeConverter<CheckTypeEnum, Integer> {

	@Override
	public Integer convertToDatabaseColumn(CheckTypeEnum attribute) {
		return attribute.getDbValor();
	}

	@Override
	public CheckTypeEnum convertToEntityAttribute(Integer dbData) {
		switch(dbData) {
		case 1:
			return CheckTypeEnum.CHECK_IN;
		default:
			return CheckTypeEnum.CHECK_OUT;
		}
	}
}
