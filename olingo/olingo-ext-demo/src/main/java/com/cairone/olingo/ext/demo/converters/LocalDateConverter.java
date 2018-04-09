package com.cairone.olingo.ext.demo.converters;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.cairone.olingo.ext.demo.utils.DateUtil;

@Converter(autoApply = true)
public class LocalDateConverter implements AttributeConverter<LocalDate, Date> {

	@Override
	public Date convertToDatabaseColumn(LocalDate localDate) {
		return DateUtil.asDate(localDate);
	}

	@Override
	public LocalDate convertToEntityAttribute(Date date) {
		return DateUtil.asLocalDate(date);
	}
}
