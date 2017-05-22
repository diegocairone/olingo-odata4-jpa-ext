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

import java.util.Map;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;

public interface MediaDataSource {

	String isSuitableFor();
	
	byte[] findMediaResource(Map<String, UriParameter> keyPredicateMap) throws ODataApplicationException;
	Object createMediaResource(byte[] binary) throws ODataApplicationException;
	void updateMediaResource(Map<String, UriParameter> keyPredicateMap, byte[] binary) throws ODataApplicationException;
	//void deleteMediaResource(Map<String, UriParameter> keyPredicateMap) throws ODataApplicationException;
	
}
