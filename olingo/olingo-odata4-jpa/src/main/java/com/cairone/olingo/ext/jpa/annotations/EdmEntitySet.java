package com.cairone.olingo.ext.jpa.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines an entity set for an entity type. This annotation must only be used on classes that also have an
 * {@code EdmEntity} annotation.
 *
 * Reference: OData Version 4.0 Part 3: Common Schema Definition Language (CSDL), paragraph 13.2
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EdmEntitySet {

    /**
     * The name of the entity set. If the name of the entity set is not specified using either this attribute or the
     * {@code value} attribute, the automatically pluralized name of the entity is used.
     *
     * @return The name of the entity set.
     */
    String name() default "";

    /**
     * Convenience attribute for the name of the entity set.
     *
     * @return The name of the entity set.
     */
    String value() default "";

    /**
     * Specifies whether this entity set should be included in the service document.
     *
     * @return {@code true} if this entity set should be included in the service document, {@code false} otherwise.
     */
    boolean includedInServiceDocument() default true;

}
