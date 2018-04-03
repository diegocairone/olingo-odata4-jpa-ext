package com.cairone.olingo.ext.jpa.utilities;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.cairone.olingo.ext.jpa.annotations.EdmEnum;
import com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty;
import com.cairone.olingo.ext.jpa.annotations.EdmParameter;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;
import com.cairone.olingo.ext.jpa.enums.NamingConvention;
import com.google.common.base.CaseFormat;
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
	
	public static String applyNamingConvention(final EdmProperty property, final String propertyName) {
		String formatedPropertyName = propertyName;
		if(!property.fieldConvention().equals(NamingConvention.NONE) &&
				!property.propertyConvention().equals(NamingConvention.NONE)) {
			CaseFormat cfFrom = null;
			switch(property.fieldConvention()) {
			case LOWER_CAMEL:
				cfFrom = CaseFormat.LOWER_CAMEL;
				break;
			case LOWER_UNDERSCORE:
				cfFrom = CaseFormat.LOWER_UNDERSCORE;
				break;
			case UPPER_CAMEL:
				cfFrom = CaseFormat.UPPER_CAMEL;
				break;
			case UPPER_UNDERSCORE:
				cfFrom = CaseFormat.UPPER_UNDERSCORE;
				break;
			case NONE:
			default:
				break;
			}
			
			if(cfFrom != null) {
				CaseFormat cfTo = null;
				switch(property.propertyConvention()) {
				case LOWER_CAMEL:
					cfTo = CaseFormat.LOWER_CAMEL;
					formatedPropertyName = cfFrom.to(cfTo, propertyName);
					break;
				case LOWER_UNDERSCORE:
					cfTo = CaseFormat.LOWER_UNDERSCORE;
					formatedPropertyName = cfFrom.to(cfTo, propertyName);
					break;
				case UPPER_CAMEL:
					cfTo = CaseFormat.UPPER_CAMEL;
					formatedPropertyName = cfFrom.to(cfTo, propertyName);
					break;
				case UPPER_UNDERSCORE:
					cfTo = CaseFormat.UPPER_UNDERSCORE;
					formatedPropertyName = cfFrom.to(cfTo, propertyName);
					break;
				case NONE:
				default:
					break;
				}
			}
		}
		return formatedPropertyName;
	}
	
	public static String revertNamingConvention(final EdmProperty property, final String propertyName) {
		String formatedPropertyName = propertyName;
		if(!property.fieldConvention().equals(NamingConvention.NONE) &&
				!property.propertyConvention().equals(NamingConvention.NONE)) {
			CaseFormat cfFrom = null;
			switch(property.propertyConvention()) {
			case LOWER_CAMEL:
				cfFrom = CaseFormat.LOWER_CAMEL;
				break;
			case LOWER_UNDERSCORE:
				cfFrom = CaseFormat.LOWER_UNDERSCORE;
				break;
			case UPPER_CAMEL:
				cfFrom = CaseFormat.UPPER_CAMEL;
				break;
			case UPPER_UNDERSCORE:
				cfFrom = CaseFormat.UPPER_UNDERSCORE;
				break;
			case NONE:
			default:
				break;
			}
			
			if(cfFrom != null) {
				CaseFormat cfTo = null;
				switch(property.fieldConvention()) {
				case LOWER_CAMEL:
					cfTo = CaseFormat.LOWER_CAMEL;
					formatedPropertyName = cfFrom.to(cfTo, propertyName);
					break;
				case LOWER_UNDERSCORE:
					cfTo = CaseFormat.LOWER_UNDERSCORE;
					formatedPropertyName = cfFrom.to(cfTo, propertyName);
					break;
				case UPPER_CAMEL:
					cfTo = CaseFormat.UPPER_CAMEL;
					formatedPropertyName = cfFrom.to(cfTo, propertyName);
					break;
				case UPPER_UNDERSCORE:
					cfTo = CaseFormat.UPPER_UNDERSCORE;
					formatedPropertyName = cfFrom.to(cfTo, propertyName);
					break;
				case NONE:
				default:
					break;
				}
			}
		}
		return formatedPropertyName;
	}

	public static String applyNamingConvention(final EdmNavigationProperty property, String propertyName) {
		if(!property.fieldConvention().equals(NamingConvention.NONE) &&
				!property.propertyConvention().equals(NamingConvention.NONE)) {
			CaseFormat cfFrom = null;
			switch(property.fieldConvention()) {
			case LOWER_CAMEL:
				cfFrom = CaseFormat.LOWER_CAMEL;
				break;
			case LOWER_UNDERSCORE:
				cfFrom = CaseFormat.LOWER_UNDERSCORE;
				break;
			case UPPER_CAMEL:
				cfFrom = CaseFormat.UPPER_CAMEL;
				break;
			case UPPER_UNDERSCORE:
				cfFrom = CaseFormat.UPPER_UNDERSCORE;
				break;
			case NONE:
			default:
				break;
			}
			
			if(cfFrom != null) {
				CaseFormat cfTo = null;
				switch(property.propertyConvention()) {
				case LOWER_CAMEL:
					cfTo = CaseFormat.LOWER_CAMEL;
					propertyName = cfFrom.to(cfTo, propertyName);
					break;
				case LOWER_UNDERSCORE:
					cfTo = CaseFormat.LOWER_UNDERSCORE;
					propertyName = cfFrom.to(cfTo, propertyName);
					break;
				case UPPER_CAMEL:
					cfTo = CaseFormat.UPPER_CAMEL;
					propertyName = cfFrom.to(cfTo, propertyName);
					break;
				case UPPER_UNDERSCORE:
					cfTo = CaseFormat.UPPER_UNDERSCORE;
					propertyName = cfFrom.to(cfTo, propertyName);
					break;
				case NONE:
				default:
					break;
				}
			}
		}
		return propertyName;
	}

	public static String revertNamingConvention(final EdmNavigationProperty property, final String propertyName) {
		String formatedPropertyName = propertyName;
		if(!property.fieldConvention().equals(NamingConvention.NONE) &&
				!property.propertyConvention().equals(NamingConvention.NONE)) {
			CaseFormat cfFrom = null;
			switch(property.propertyConvention()) {
			case LOWER_CAMEL:
				cfFrom = CaseFormat.LOWER_CAMEL;
				break;
			case LOWER_UNDERSCORE:
				cfFrom = CaseFormat.LOWER_UNDERSCORE;
				break;
			case UPPER_CAMEL:
				cfFrom = CaseFormat.UPPER_CAMEL;
				break;
			case UPPER_UNDERSCORE:
				cfFrom = CaseFormat.UPPER_UNDERSCORE;
				break;
			case NONE:
			default:
				break;
			}
			
			if(cfFrom != null) {
				CaseFormat cfTo = null;
				switch(property.fieldConvention()) {
				case LOWER_CAMEL:
					cfTo = CaseFormat.LOWER_CAMEL;
					formatedPropertyName = cfFrom.to(cfTo, propertyName);
					break;
				case LOWER_UNDERSCORE:
					cfTo = CaseFormat.LOWER_UNDERSCORE;
					formatedPropertyName = cfFrom.to(cfTo, propertyName);
					break;
				case UPPER_CAMEL:
					cfTo = CaseFormat.UPPER_CAMEL;
					formatedPropertyName = cfFrom.to(cfTo, propertyName);
					break;
				case UPPER_UNDERSCORE:
					cfTo = CaseFormat.UPPER_UNDERSCORE;
					formatedPropertyName = cfFrom.to(cfTo, propertyName);
					break;
				case NONE:
				default:
					break;
				}
			}
		}
		return formatedPropertyName;
	}

	public static String applyNamingConvention(final EdmParameter parameter, String parameterName) {
		if(!parameter.fieldConvention().equals(NamingConvention.NONE) &&
				!parameter.propertyConvention().equals(NamingConvention.NONE)) {
			CaseFormat cfFrom = null;
			switch(parameter.fieldConvention()) {
			case LOWER_CAMEL:
				cfFrom = CaseFormat.LOWER_CAMEL;
				break;
			case LOWER_UNDERSCORE:
				cfFrom = CaseFormat.LOWER_UNDERSCORE;
				break;
			case UPPER_CAMEL:
				cfFrom = CaseFormat.UPPER_CAMEL;
				break;
			case UPPER_UNDERSCORE:
				cfFrom = CaseFormat.UPPER_UNDERSCORE;
				break;
			case NONE:
			default:
				break;
			}
			
			if(cfFrom != null) {
				CaseFormat cfTo = null;
				switch(parameter.propertyConvention()) {
				case LOWER_CAMEL:
					cfTo = CaseFormat.LOWER_CAMEL;
					parameterName = cfFrom.to(cfTo, parameterName);
					break;
				case LOWER_UNDERSCORE:
					cfTo = CaseFormat.LOWER_UNDERSCORE;
					parameterName = cfFrom.to(cfTo, parameterName);
					break;
				case UPPER_CAMEL:
					cfTo = CaseFormat.UPPER_CAMEL;
					parameterName = cfFrom.to(cfTo, parameterName);
					break;
				case UPPER_UNDERSCORE:
					cfTo = CaseFormat.UPPER_UNDERSCORE;
					parameterName = cfFrom.to(cfTo, parameterName);
					break;
				case NONE:
				default:
					break;
				}
			}
		}
		return parameterName;
	}

	public static String revertNamingConvention(final EdmParameter parameter, final String parameterName) {
		String formatedPropertyName = parameterName;
		if(!parameter.fieldConvention().equals(NamingConvention.NONE) &&
				!parameter.propertyConvention().equals(NamingConvention.NONE)) {
			CaseFormat cfFrom = null;
			switch(parameter.propertyConvention()) {
			case LOWER_CAMEL:
				cfFrom = CaseFormat.LOWER_CAMEL;
				break;
			case LOWER_UNDERSCORE:
				cfFrom = CaseFormat.LOWER_UNDERSCORE;
				break;
			case UPPER_CAMEL:
				cfFrom = CaseFormat.UPPER_CAMEL;
				break;
			case UPPER_UNDERSCORE:
				cfFrom = CaseFormat.UPPER_UNDERSCORE;
				break;
			case NONE:
			default:
				break;
			}
			
			if(cfFrom != null) {
				CaseFormat cfTo = null;
				switch(parameter.fieldConvention()) {
				case LOWER_CAMEL:
					cfTo = CaseFormat.LOWER_CAMEL;
					formatedPropertyName = cfFrom.to(cfTo, parameterName);
					break;
				case LOWER_UNDERSCORE:
					cfTo = CaseFormat.LOWER_UNDERSCORE;
					formatedPropertyName = cfFrom.to(cfTo, parameterName);
					break;
				case UPPER_CAMEL:
					cfTo = CaseFormat.UPPER_CAMEL;
					formatedPropertyName = cfFrom.to(cfTo, parameterName);
					break;
				case UPPER_UNDERSCORE:
					cfTo = CaseFormat.UPPER_UNDERSCORE;
					formatedPropertyName = cfFrom.to(cfTo, parameterName);
					break;
				case NONE:
				default:
					break;
				}
			}
		}
		return formatedPropertyName;
	}

	public static String inferEdmType(Field field) {
		return inferEdmType(field.getType());
	}

	public static String inferEdmType(Class<?> type) {
		
		if(type.isAssignableFrom(Short.class)) {
			return "Edm.Int16";
		} else if(type.isAssignableFrom(Integer.class)) {
			return "Edm.Int32";
		} else if (type.isAssignableFrom(Long.class)) {
			return "Edm.Int64";
		} else if (type.isAssignableFrom(LocalDate.class)) {
			return "Edm.Date";
		} else if (type.isAssignableFrom(LocalDateTime.class)) {
			return "Edm.DateTimeOffset";
		} else if (type.isAssignableFrom(Boolean.class)) {
			return "Edm.Boolean";
		} else if (type.isAssignableFrom(BigDecimal.class)) {
			return "Edm.Decimal";
		} else {
			Class<?> enumClazz = type;
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
