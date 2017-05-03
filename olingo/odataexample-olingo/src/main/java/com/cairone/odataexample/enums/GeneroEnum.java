package com.cairone.odataexample.enums;

import com.cairone.odataexample.edm.resources.GeneroOdataEnum;


public enum GeneroEnum {
	MASCULINO('M'), FEMENINO('F');

	private final char valor;
	
	private GeneroEnum(char valor) {
		this.valor = valor;
	}

	public char getValor() {
		return valor;
	}

	public GeneroOdataEnum toGeneroOdataEnum() {
		
		switch(valor) {
		case 'F':
			return GeneroOdataEnum.FEMENINO;
		case 'M':
		default:
			return GeneroOdataEnum.MASCULINO;
		
		}
	}
}
