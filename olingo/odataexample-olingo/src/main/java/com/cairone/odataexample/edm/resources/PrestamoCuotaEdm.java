package com.cairone.odataexample.edm.resources;

import java.math.BigDecimal;

import com.cairone.odataexample.OdataExample;
import com.cairone.olingo.ext.jpa.annotations.EdmEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntitySet;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;

@EdmEntity(name = "PrestamoCuota", key = { "numero" }, namespace = OdataExample.NAME_SPACE, containerName = OdataExample.CONTAINER_NAME)
@EdmEntitySet("PrestamoCuotas")
public class PrestamoCuotaEdm implements Comparable<PrestamoCuotaEdm> {

	@EdmProperty(name="numero", nullable = false) 
	private Integer numero = null;
	
	@EdmProperty(name="capital", nullable = false, scale = 2) 
	private BigDecimal capital = null;
	
	@EdmProperty(name="interes", nullable = false, scale = 2) 
	private BigDecimal interes = null;
	
	@EdmProperty(name="iva", nullable = false, scale = 2) 
	private BigDecimal iva = null;
	
	@EdmProperty(name="interesesGravados", nullable = false, scale = 2) 
	private BigDecimal interesesGravados = null;
	
	@EdmProperty(name="monto", nullable = false, scale = 2) 
	private BigDecimal monto = null;
	
	@EdmProperty(name="saldoCapital", nullable = false, scale = 2) 
	private BigDecimal saldoCapital = null;
	
	public PrestamoCuotaEdm() {}

	public PrestamoCuotaEdm(Integer numero, BigDecimal capital, BigDecimal interes, BigDecimal iva, BigDecimal interesesGravados, BigDecimal monto, BigDecimal saldoCapital) {
		super();
		this.numero = numero;
		this.capital = capital;
		this.interes = interes;
		this.iva = iva;
		this.interesesGravados = interesesGravados;
		this.monto = monto;
		this.saldoCapital = saldoCapital;
	}

	public Integer getNumero() {
		return numero;
	}

	public void setNumero(Integer numero) {
		this.numero = numero;
	}

	public BigDecimal getCapital() {
		return capital;
	}

	public void setCapital(BigDecimal capital) {
		this.capital = capital;
	}

	public BigDecimal getInteres() {
		return interes;
	}

	public void setInteres(BigDecimal interes) {
		this.interes = interes;
	}

	public BigDecimal getIva() {
		return iva;
	}

	public void setIva(BigDecimal iva) {
		this.iva = iva;
	}

	public BigDecimal getInteresesGravados() {
		return interesesGravados;
	}

	public void setInteresesGravados(BigDecimal interesesGravados) {
		this.interesesGravados = interesesGravados;
	}

	public BigDecimal getMonto() {
		return monto;
	}

	public void setMonto(BigDecimal monto) {
		this.monto = monto;
	}

	public BigDecimal getSaldoCapital() {
		return saldoCapital;
	}

	public void setSaldoCapital(BigDecimal saldoCapital) {
		this.saldoCapital = saldoCapital;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((numero == null) ? 0 : numero.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrestamoCuotaEdm other = (PrestamoCuotaEdm) obj;
		if (numero == null) {
			if (other.numero != null)
				return false;
		} else if (!numero.equals(other.numero))
			return false;
		return true;
	}

	@Override
	public int compareTo(PrestamoCuotaEdm o) {
		return this.numero.compareTo(o.getNumero());
	}
}
