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
package com.cairone.olingo.ext.jpa.interfaces;

import java.util.List;
import java.util.Map;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;

public interface DataSource {

	String isSuitableFor();
	
	Object create(Object entity, Object superentity) throws ODataApplicationException;
	Object update(Map<String, UriParameter> keyPredicateMap, Object entity, Object superentity, List<String> propertiesInJSON, boolean isPut) throws ODataApplicationException;
	Object delete(Map<String, UriParameter> keyPredicateMap, Object superentity) throws ODataApplicationException;
	
	Object readFromKey(Map<String, UriParameter> keyPredicateMap, ExpandOption expandOption, SelectOption selectOption, Object superentity) throws ODataApplicationException;
	Iterable<?> readAll(ExpandOption expandOption, FilterOption filterOption, OrderByOption orderByOption, Object parentEntity) throws ODataApplicationException;
}
