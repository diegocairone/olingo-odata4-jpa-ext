package com.cairone.olingo.ext.jpa.utilities;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.cairone.olingo.ext.jpa.annotations.EdmEnum;
import com.google.common.base.CharMatcher;

public class Util {

	public static Field[] getFields(Class<?> clazz) {
		return getFields(clazz, true);
	}
	
	public static Field[] getFields(Class<?> clazz, boolean includeSuperClass) {
		if(!includeSuperClass) return clazz.getDeclaredFields();
		
		List<Field> fields = new ArrayList<>(Arrays.asList( clazz.getDeclaredFields() ));
		
		if(includeSuperClass) {
			Class<?> superclazz = clazz.getSuperclass();
			if(superclazz != null) {
				Field[] superFields = superclazz.getDeclaredFields();
				fields.addAll(Arrays.asList( superFields ));
			}
		}
		
		return fields.toArray(new Field[fields.size()]);
	}
	
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
		} else {
			Class<?> enumClazz = field.getType();
			EdmEnum edmEnum = enumClazz.getAnnotation(EdmEnum.class);
			if(edmEnum != null) {
				String namespace = edmEnum.namespace();
				String name = edmEnum.name().isEmpty() ? enumClazz.getSimpleName() : edmEnum.name();
				String parameterType = String.format("%s.%s", namespace, name);
				return parameterType;
			}
		}
		
		return "Edm.String";
	}

	/**
	 * Encode special characters to UTF-8 character set
	 * @see https://www.w3schools.com/TagS/ref_urlencode.asp 
	 * @param keyValues
	 * @return
	 */
	public static String formatEntityID(Map<String, Object> keyValues) {
		
		String entityID = keyValues.entrySet().stream().map(Entry::toString).collect(Collectors.joining(",", "(", ")"));
		entityID = CharMatcher.is('<').replaceFrom(entityID, "%3C");
		entityID = CharMatcher.is('>').replaceFrom(entityID, "%3E");
		entityID = CharMatcher.is(' ').replaceFrom(entityID, "%20");
		
		return entityID;
	}
}
