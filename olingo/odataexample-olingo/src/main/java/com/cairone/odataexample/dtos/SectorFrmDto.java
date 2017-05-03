package com.cairone.odataexample.dtos;

import com.cairone.odataexample.edm.resources.SectorEdm;
import com.cairone.odataexample.entities.SectorEntity;

public class SectorFrmDto {

	private Integer id = null;
	private String nombre = null;

	public SectorFrmDto() {}

	public SectorFrmDto(Integer id, String nombre) {
		super();
		this.id = id;
		this.nombre = nombre == null ? null : nombre.trim().toUpperCase();
	}

	public SectorFrmDto(SectorEdm sectorEdm) {
		this(sectorEdm.getId(), sectorEdm.getNombre());
	}

	public SectorFrmDto(SectorEntity sectorEntity) {
		this(sectorEntity.getId(), sectorEntity.getNombre());
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
