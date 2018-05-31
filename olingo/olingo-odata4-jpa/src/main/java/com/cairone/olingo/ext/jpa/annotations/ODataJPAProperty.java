/**
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
package com.cairone.olingo.ext.jpa.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.cairone.olingo.ext.jpa.enums.EnumerationTreatedAs;

/**
 * Establishes a one to one relationship between a field in an Edm Entity Type and a field in an JPA Entity
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Deprecated
public @interface ODataJPAProperty {

	/**
	 * The path of the field in JPA entity using dot notation
	 * 
	 * @return The path of the field in JPA entity 
	 */
	String value() default "";
	
	boolean ignore() default false;
	
	EnumerationTreatedAs treatedAs() default EnumerationTreatedAs.ENUMERATION;
}
