package com.cairone.odataexample.dtos;

import com.cairone.odataexample.edm.resources.TipoDocumentoEdm;
import com.cairone.odataexample.entities.TipoDocumentoEntity;

public class TipoDocumentoFrmDto {

	private Integer id = null;
	private String nombre = null;
	private String abreviatura = null;

	public TipoDocumentoFrmDto() {}

	public TipoDocumentoFrmDto(Integer id, String nombre, String abreviatura) {
		super();
		this.id = id;
		this.nombre = nombre == null ? null : nombre.trim().toUpperCase();
		this.abreviatura = abreviatura == null ? null : abreviatura.trim().toUpperCase();
	}

	public TipoDocumentoFrmDto(TipoDocumentoEdm tipoDocumentoEdm) {
		this(tipoDocumentoEdm.getId(), tipoDocumentoEdm.getNombre(), tipoDocumentoEdm.getAbreviatura());
	}

	public TipoDocumentoFrmDto(TipoDocumentoEntity tipoDocumentoEntity) {
		this(tipoDocumentoEntity.getId(), tipoDocumentoEntity.getNombre(), tipoDocumentoEntity.getAbreviatura());
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getAbreviatura() {
		return abreviatura;
	}

	public void setAbreviatura(String abreviatura) {
		this.abreviatura = abreviatura;
	}

}
