package com.cairone.odataexample.dtos;

import com.cairone.odataexample.edm.resources.GeneroOdataEnum;
import com.cairone.odataexample.edm.resources.PersonaEdm;
import com.cairone.odataexample.entities.PersonaEntity;

public class PersonaFrmDto {
	
	private Integer tipoDocumentoId = null;
	private String numeroDocumento = null;
	private String nombres = null;
	private String apellidos = null;
	private String apodo = null;
	private Integer paisId = null;
	private Integer provinciaId = null;
	private Integer localidadId = null;
	private GeneroOdataEnum genero = null;

	public PersonaFrmDto() {}

	public PersonaFrmDto(Integer tipoDocumentoId, String numeroDocumento, String nombres, String apellidos, String apodo, Integer paisId, Integer provinciaId, Integer localidadId, GeneroOdataEnum genero) {
		super();
		this.tipoDocumentoId = tipoDocumentoId;
		this.numeroDocumento = numeroDocumento == null ? null : numeroDocumento.trim();
		this.nombres = nombres == null ? null : nombres.trim().toUpperCase();
		this.apellidos = apellidos == null ? null : apellidos.trim().toUpperCase();
		this.apodo = apodo == null ? null : apodo.trim().toUpperCase();
		this.paisId = paisId;
		this.provinciaId = provinciaId;
		this.localidadId = localidadId;
		this.genero = genero;
	}
	
	public PersonaFrmDto(PersonaEdm personaEdm) {
		this(
				personaEdm.getTipoDocumentoId(), 
				personaEdm.getNumeroDocumento(), 
				personaEdm.getNombres(), 
				personaEdm.getApellidos(), 
				personaEdm.getApodo(), 
				personaEdm.getLocalidad() == null ? null : personaEdm.getLocalidad().getPaisId(),
				personaEdm.getLocalidad() == null ? null : personaEdm.getLocalidad().getProvinciaId(),
				personaEdm.getLocalidad() == null ? null : personaEdm.getLocalidad().getLocalidadId(),
				personaEdm.getGenero());
	}

	public PersonaFrmDto(PersonaEntity personaEntity) {
		this(
				personaEntity.getTipoDocumento().getId(), 
				personaEntity.getNumeroDocumento(), 
				personaEntity.getNombres(), 
				personaEntity.getApellidos(), 
				personaEntity.getApodo(), 
				personaEntity.getLocalidad().getProvincia().getPais().getId(),
				personaEntity.getLocalidad().getProvincia().getId(),
				personaEntity.getLocalidad().getId(),
				personaEntity.getGenero().toGeneroOdataEnum());
	}
	
	public Integer getTipoDocumentoId() {
		return tipoDocumentoId;
	}

	public void setTipoDocumentoId(Integer tipoDocumentoId) {
		this.tipoDocumentoId = tipoDocumentoId;
	}

	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	public String getNombres() {
		return nombres;
	}

	public void setNombres(String nombres) {
		this.nombres = nombres;
	}

	public String getApellidos() {
		return apellidos;
	}

	public void setApellidos(String apellidos) {
		this.apellidos = apellidos;
	}

	public String getApodo() {
		return apodo;
	}

	public void setApodo(String apodo) {
		this.apodo = apodo;
	}

	public Integer getPaisId() {
		return paisId;
	}

	public void setPaisId(Integer paisId) {
		this.paisId = paisId;
	}

	public Integer getProvinciaId() {
		return provinciaId;
	}

	public void setProvinciaId(Integer provinciaId) {
		this.provinciaId = provinciaId;
	}

	public Integer getLocalidadId() {
		return localidadId;
	}

	public void setLocalidadId(Integer localidadId) {
		this.localidadId = localidadId;
	}

	public GeneroOdataEnum getGenero() {
		return genero;
	}

	public void setGenero(GeneroOdataEnum genero) {
		this.genero = genero;
	}
}
