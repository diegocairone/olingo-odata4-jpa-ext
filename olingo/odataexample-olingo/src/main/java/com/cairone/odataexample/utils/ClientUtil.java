package com.cairone.odataexample.utils;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientObjectFactory;
import org.apache.olingo.client.api.domain.ClientPrimitiveValue;
import org.apache.olingo.client.api.domain.ClientProperty;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.ex.ODataException;

import com.cairone.olingo.ext.jpa.annotations.EdmEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;
import com.cairone.olingo.ext.jpa.utilities.Util;

public class ClientUtil {

	public static ClientEntity getClientEntity(final ClientObjectFactory factory, final Object object) throws ODataException {
		
		Class<?> clazz = object.getClass();
		Field[] fields = clazz.getDeclaredFields();
		
		EdmEntity edmEntity = clazz.getDeclaredAnnotation(EdmEntity.class);
		
		String entityTypeName = edmEntity.name().isEmpty() ? clazz.getSimpleName() : edmEntity.name();
		String namespace = edmEntity.namespace();
		
		FullQualifiedName fqn = new FullQualifiedName(namespace, entityTypeName);
		ClientEntity clientEntity = factory.newEntity(fqn);
		
		List<ClientProperty> clientProperties = new ArrayList<ClientProperty>();
		
		for(Field field : fields) {
			
			EdmProperty edmProperty = field.getAnnotation(EdmProperty.class);
			String propName = edmProperty.name().isEmpty() ? field.getName() : edmProperty.name();
			
			if(edmProperty != null) {
				String edmType = edmProperty.type();
				if(edmType.isEmpty()) {
					edmType = Util.inferEdmType(field);
				}
				
				clientProperties.add(factory.newPrimitiveProperty(propName, getClientPrimitiveValue(factory, field, object)));
			}
		}
		
		clientEntity.getProperties().addAll(clientProperties);
		
		return clientEntity;
	}
	
	public static ClientPrimitiveValue getClientPrimitiveValue(final ClientObjectFactory factory, final Field field, final Object object) throws ODataException {
		
		Class<?> clazz = field.getType();
				
		try 
		{
			field.setAccessible(true);
			
			if(clazz.isAssignableFrom(Integer.class)) {
				ClientPrimitiveValue clientPrimitiveValue = factory.newPrimitiveValueBuilder()
						.setType(EdmPrimitiveTypeKind.Int32).setValue(field.get(object)).build();
				return clientPrimitiveValue;
			} else if(clazz.isAssignableFrom(Long.class)) {
				ClientPrimitiveValue clientPrimitiveValue = factory.newPrimitiveValueBuilder()
						.setType(EdmPrimitiveTypeKind.Int64).setValue(field.get(object)).build();
				return clientPrimitiveValue;
			} else if(clazz.isAssignableFrom(LocalDate.class)) {
				LocalDate value = (LocalDate) field.get(object);
				ClientPrimitiveValue clientPrimitiveValue = factory.newPrimitiveValueBuilder()
						.setType(EdmPrimitiveTypeKind.Date).setValue(value).build();
				return clientPrimitiveValue;
			} else if(clazz.isAssignableFrom(Boolean.class)) {
				ClientPrimitiveValue clientPrimitiveValue = factory.newPrimitiveValueBuilder()
						.setType(EdmPrimitiveTypeKind.Boolean).setValue(field.get(object)).build();
				return clientPrimitiveValue;
			} else {
				ClientPrimitiveValue clientPrimitiveValue = factory.newPrimitiveValueBuilder()
						.setType(EdmPrimitiveTypeKind.String).setValue(field.get(object)).build();
				return clientPrimitiveValue;
			}
			
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new ODataException(e.getMessage(), e);
		}
	}
}
