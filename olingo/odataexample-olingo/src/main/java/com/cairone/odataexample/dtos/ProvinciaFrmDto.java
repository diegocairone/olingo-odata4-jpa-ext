package com.cairone.odataexample.dtos;

import com.cairone.odataexample.edm.resources.ProvinciaEdm;
import com.cairone.odataexample.entities.ProvinciaEntity;

public class ProvinciaFrmDto {

	private Integer paisID = null;
	private Integer id = null;
	private String nombre = null;

	public ProvinciaFrmDto() {}

	public ProvinciaFrmDto(Integer paisID, Integer id, String nombre) {
		super();
		this.paisID = paisID;
		this.id = id;
		this.nombre = nombre == null ? null : nombre.trim().toUpperCase();
	}

	public ProvinciaFrmDto(ProvinciaEdm provinciaEdm) {
		this(provinciaEdm.getPaisId(), provinciaEdm.getId(), provinciaEdm.getNombre());
	}

	public ProvinciaFrmDto(ProvinciaEntity provinciaEntity) {
		this(provinciaEntity.getPais().getId(), provinciaEntity.getId(), provinciaEntity.getNombre());
	}
	
	public Integer getPaisID() {
		return paisID;
	}

	public void setPaisID(Integer paisID) {
		this.paisID = paisID;
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
		this.nombre = nombre == null ? null : nombre.trim().toUpperCase();
	}
	
}
