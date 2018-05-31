package com.cairone.olingo.ext.jpa.interfaces;

public interface OdataExtendedEnum<T, A> extends OdataEnum<T> {

	public A getAsPrimitive();
}
