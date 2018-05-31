package com.cairone.olingo.ext.demo.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.cairone.olingo.ext.demo.edm.enums.GenderEnum;

@Converter(autoApply = true)
public class GenderConverter implements AttributeConverter<GenderEnum, Character> {

	@Override
	public Character convertToDatabaseColumn(GenderEnum attribute) {
		return attribute.getDbValor();
	}

	@Override
	public GenderEnum convertToEntityAttribute(Character dbData) {
		switch(dbData) {
		case 'F':
			return GenderEnum.FEMALE;
		default:
			return GenderEnum.MALE;
		}
	}
}
