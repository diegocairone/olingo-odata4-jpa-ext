package com.cairone.odataexample.dtos;

import com.cairone.odataexample.edm.resources.LocalidadEdm;
import com.cairone.odataexample.entities.LocalidadEntity;


public class LocalidadFrmDto {

	private Integer paisId = null;
	private Integer provinciaId = null;
	private Integer localidadId = null;
	private String nombre = null;
	private Integer cp = null;
	private Integer prefijo = null;

	public LocalidadFrmDto() {}

	public LocalidadFrmDto(Integer paisId, Integer provinciaId, Integer localidadId, String nombre, Integer cp, Integer prefijo) {
		super();
		this.paisId = paisId;
		this.provinciaId = provinciaId;
		this.localidadId = localidadId;
		this.nombre = nombre == null ? null : nombre.trim().toUpperCase();
		this.cp = cp;
		this.prefijo = prefijo;
	}

	public LocalidadFrmDto(LocalidadEdm localidadEdm) {
		this(localidadEdm.getPaisId(), localidadEdm.getProvinciaId(), localidadEdm.getLocalidadId(), localidadEdm.getNombre(), localidadEdm.getCp(), localidadEdm.getPrefijo());
	}

	public LocalidadFrmDto(LocalidadEntity localidadEntity) {
		this(localidadEntity.getProvincia().getPais().getId(), localidadEntity.getProvincia().getId(), localidadEntity.getId(), localidadEntity.getNombre(), localidadEntity.getCp(), localidadEntity.getPrefijo());
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

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Integer getCp() {
		return cp;
	}

	public void setCp(Integer cp) {
		this.cp = cp;
	}

	public Integer getPrefijo() {
		return prefijo;
	}

	public void setPrefijo(Integer prefijo) {
		this.prefijo = prefijo;
	}
	
}
