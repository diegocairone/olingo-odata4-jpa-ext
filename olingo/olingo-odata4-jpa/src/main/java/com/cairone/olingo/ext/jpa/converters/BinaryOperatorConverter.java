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
package com.cairone.olingo.ext.jpa.converters;

import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;

import com.cairone.olingo.ext.jpa.interfaces.OperatorConverter;

/**
 * String representation of <code>org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind</code> for use in JPQL
 * 
 * @author diego.cairone
 */
public class BinaryOperatorConverter implements OperatorConverter<BinaryOperatorKind, String> {

	@Override
	public String convertToJpqlOperator(BinaryOperatorKind x) {
		switch(x) {
		case ADD:
			break;
		case AND:
			return " AND ";
		case DIV:
			break;
		case EQ:
			return " = ";
		case GE:
			return " >= ";
		case GT:
			return " > ";
		case HAS:
			break;
		case LE:
			return " <= ";
		case LT:
			return " < ";
		case MOD:
			break;
		case MUL:
			break;
		case NE:
			return " <> ";
		case OR:
			return " OR ";
		case SUB:
			break;
		default:
			return null;
		}
		return null;
	}

	@Override
	public BinaryOperatorKind convertToOlingoOperator(String y) {
		// TODO Auto-generated method stub
		return null;
	}
}
