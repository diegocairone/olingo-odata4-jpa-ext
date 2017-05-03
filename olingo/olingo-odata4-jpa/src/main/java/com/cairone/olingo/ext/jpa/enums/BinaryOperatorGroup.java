package com.cairone.olingo.ext.jpa.enums;

import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;

public enum BinaryOperatorGroup {
	ARITHMETIC_OPERATOR, LOGICAL_OPERATOR, COMPARISON_OPERATOR, UNKNOWN;
	
	public static BinaryOperatorGroup from(BinaryOperatorKind binaryOperatorKind) {
		switch(binaryOperatorKind) {
		case ADD:
			return ARITHMETIC_OPERATOR;
		case AND:
			return LOGICAL_OPERATOR;
		case DIV:
			return ARITHMETIC_OPERATOR;
		case EQ:
			return COMPARISON_OPERATOR;
		case GE:
			return COMPARISON_OPERATOR;
		case GT:
			return COMPARISON_OPERATOR;
		case HAS:
			return LOGICAL_OPERATOR;
		case LE:
			return COMPARISON_OPERATOR;
		case LT:
			return COMPARISON_OPERATOR;
		case MOD:
			return ARITHMETIC_OPERATOR;
		case MUL:
			return ARITHMETIC_OPERATOR;
		case NE:
			return COMPARISON_OPERATOR;
		case OR:
			return LOGICAL_OPERATOR;
		case SUB:
			return ARITHMETIC_OPERATOR;
		default:
			return UNKNOWN;
		}
	}
}
