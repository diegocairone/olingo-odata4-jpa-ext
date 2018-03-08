package com.cairone.olingo.ext.jpa.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ODataQueryDslProperty {

	String value() default "";
	Class<?> jpaproperty() default Object.class;
}
