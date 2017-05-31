package com.cairone.olingo.ext.jpa.utilities;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Util {

	public static String inferEdmType(Field field) {
		
		if(field.getType().isAssignableFrom(Integer.class)) {
			return "Edm.Int32";
		} else if (field.getType().isAssignableFrom(Long.class)) {
			return "Edm.Int64";
		} else if (field.getType().isAssignableFrom(LocalDate.class)) {
			return "Edm.Date";
		} else if (field.getType().isAssignableFrom(Boolean.class)) {
			return "Edm.Boolean";
		} else if (field.getType().isAssignableFrom(BigDecimal.class)) {
			return "Edm.Decimal";
		}
		
		return "Edm.String";
	}

}
