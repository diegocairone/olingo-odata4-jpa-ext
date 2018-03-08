package com.cairone.olingo.ext.jpa.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ODataQueryDslEntity {

	Class<?> jpaentity() default Object.class;
	String variable() default "";
}
