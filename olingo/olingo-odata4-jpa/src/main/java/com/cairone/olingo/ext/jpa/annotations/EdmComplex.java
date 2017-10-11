package com.cairone.olingo.ext.jpa.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the class is a complex type in the OData entity data model.
 *
 * Reference: OData Version 4.0 Part 3: Common Schema Definition Language (CSDL), chapter 9
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EdmComplex {

    /**
     * The name of the complex type. If not specified, the name of the class is used.
     *
     * @return The name of the complex type.
     */
    String name() default "";

    /**
     * The namespace of the schema that the complex type is in. If not specified, the name of the package that contains
     * the complex class is used.
     *
     * @return The namespace of the schema that the complex type is in.
     */
    String namespace() default "";

    /**
     * Specifies whether the complex type is an open type.
     *
     * @return {@code true} if this is an open type, {@code false} otherwise.
     */
    boolean open() default false;
}
