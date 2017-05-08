/**
 * Copyright (c) 2014 All Rights Reserved by the SDL Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cairone.olingo.ext.jpa.interfaces;

/**
 * OData property facets.
 *
 * Reference: OData Version 4.0 Part 3: Common Schema Definition Language (CSDL), paragraph 6.2
 *
 */
public interface Facets {

    /**
     * Special value to indicate that {@code maxLength} is unspecified.
     */
    int MAX_LENGTH_UNSPECIFIED = Integer.MIN_VALUE;

    /**
     * Special value to indicate that {@code maxLength} has the value "max".
     * Reference: OData Version 4.0 Part 3: Common Schema Definition Language (CSDL), paragraph 6.2.2
     */
    int MAX_LENGTH_MAX = -1;

    /**
     * Special value to indicate that {@code precision} is unspecified.
     */
    int PRECISION_UNSPECIFIED = Integer.MIN_VALUE;

    /**
     * Special value to indicate that {@code scale} is unspecified.
     */
    int SCALE_UNSPECIFIED = Integer.MIN_VALUE;

    /**
     * Special value to indicate that {@code scale} has the value "variable".
     * Reference: OData Version 4.0 Part 3: Common Schema Definition Language (CSDL), paragraph 6.2.4
     */
    int SCALE_VARIABLE = -1;

    /**
     * Special value to indicate that {@code srid} is unspecified.
     */
    int SRID_UNSPECIFIED = Integer.MIN_VALUE;

    /**
     * Special value to indicate that {@code srid} has the value "variable".
     * Reference: OData Version 4.0 Part 3: Common Schema Definition Language (CSDL), paragraph 6.2.6
     */
    int SRID_VARIABLE = -1;

    /**
     * Returns the maximum length of the value of the property. This is only relevant for binary, string and stream
     * properties. The maximum length is a positive integer or one of the special values {@link #MAX_LENGTH_UNSPECIFIED}
     * or {@link #MAX_LENGTH_MAX}.
     *
     * Reference: OData Version 4.0 Part 3: Common Schema Definition Language (CSDL), paragraph 6.2.2
     *
     * @return The maximum length of the value of the property.
     */
    int getMaxLength();

    /**
     * Returns the precision of the value of the property. This is only relevant for properties of the following
     * primitive types: {@code Edm.DateTimeOffset}, {@code Edm.Decimal}, {@code Edm.Duration}, {@code Edm.TimeOfDay}.
     *
     * For {@code Edm.Decimal} properties the precision is a positive integer or the special value
     * {@link #PRECISION_UNSPECIFIED}.
     *
     * For temporal properties the precision is an integer between zero and twelve or the special value
     * {@link #PRECISION_UNSPECIFIED}.
     *
     * Reference: OData Version 4.0 Part 3: Common Schema Definition Language (CSDL), paragraph 6.2.3
     *
     * @return The precision of the value of the property.
     */
    int getPrecision();

    /**
     * Returns the scale of the value of the property. This is only relevant for properties of the primitive type
     * {@code Edm.Decimal}. The scale is a non-negative integer that is less than or equal to the precision, or one of
     * the special values {@link #SCALE_UNSPECIFIED} or {@link #SCALE_VARIABLE}.
     *
     * Reference: OData Version 4.0 Part 3: Common Schema Definition Language (CSDL), paragraph 6.2.4
     *
     * @return The scale of the value of the property.
     */
    int getScale();

    /**
     * Returns the spatial reference system identifier of the property. This is only relevant for geography and
     * geometry properties. The SRID is a non-negative integer or one of the special values {@link #SRID_UNSPECIFIED}
     * or {@link #SRID_VARIABLE}.
     *
     * @return The spatial reference system identifier of the property.
     */
    int getSRID();

    /**
     * Returns {@code true} if the property is encoded with Unicode, {@code false} if it is encoded with ASCII. This
     * is only relevant for {@code Edm.String} properties.
     *
     * @return {@code true} if the property is encoded with Unicode, {@code false} if it is encoded with ASCII.
     */
    boolean isUnicode();
}
