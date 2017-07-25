package com.cairone.odataexample.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.cairone.odataexample.edm.resources.GeneroOdataEnum;

@Converter(autoApply = true)
public class GeneroOdataConverter implements AttributeConverter<GeneroOdataEnum, Character> {

	@Override
	public Character convertToDatabaseColumn(GeneroOdataEnum attribute) {
		return attribute.equals(GeneroOdataEnum.FEMENINO) ? 'F' : 'M';
	}

	@Override
	public GeneroOdataEnum convertToEntityAttribute(Character dbData) {
		switch(dbData) {
		case 'F':
			return GeneroOdataEnum.FEMENINO;
		case 'M':
		default:
			return GeneroOdataEnum.MASCULINO;
		}
	}

}
