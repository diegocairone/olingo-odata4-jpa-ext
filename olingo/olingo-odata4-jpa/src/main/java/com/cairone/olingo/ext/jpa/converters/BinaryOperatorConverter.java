package com.cairone.olingo.ext.jpa.converters;

import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;

import com.cairone.olingo.ext.jpa.interfaces.OperatorConverter;

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
