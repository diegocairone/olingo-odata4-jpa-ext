package com.cairone.odataexample.edm.resources;

import com.cairone.odataexample.enums.GeneroEnum;
import com.cairone.olingo.ext.jpa.annotations.EdmEnum;
import com.cairone.olingo.ext.jpa.interfaces.OdataEnum;


@EdmEnum(name="genero")
public enum GeneroOdataEnum implements OdataEnum<GeneroOdataEnum>{
	MASCULINO(1), FEMENINO(2);

	private final int valor;
	
	private GeneroOdataEnum(int valor) {
		this.valor = valor;
	}

	public int getValor() {
		return valor;
	}
	
	public GeneroOdataEnum setValor(int valor) {
		if(valor == 2) return FEMENINO; else return MASCULINO;
	}

	public GeneroEnum toGeneroEnum() {
		
		switch(valor) {
		case 2:
			return GeneroEnum.FEMENINO;
		case 1:
		default:
			return GeneroEnum.MASCULINO;
		
		}
	}
}
