package com.cairone.odataexample.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class PersonaSectorPKEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name="id_tipodoc", nullable = false)
	private Integer tipoDocId = null;
	
	@Column(name="numero_documento", length = 15, nullable = false)
	private String numeroDocumento = null;
	
	@Column(name="id_sector", nullable = false)
	private Integer sectorId = null;
	
	public PersonaSectorPKEntity() {}

	public PersonaSectorPKEntity(Integer tipoDocId, String numeroDocumento, Integer sectorId) {
		super();
		this.tipoDocId = tipoDocId;
		this.numeroDocumento = numeroDocumento;
		this.sectorId = sectorId;
	}
	
	public PersonaSectorPKEntity(PersonaEntity personaEntity, SectorEntity sectorEntity) {
		this(personaEntity.getTipoDocumento().getId(), personaEntity.getNumeroDocumento(), sectorEntity.getId());
	}

	public Integer getTipoDocId() {
		return tipoDocId;
	}

	public void setTipoDocId(Integer tipoDocId) {
		this.tipoDocId = tipoDocId;
	}

	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	public Integer getSectorId() {
		return sectorId;
	}

	public void setSectorId(Integer sectorId) {
		this.sectorId = sectorId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((numeroDocumento == null) ? 0 : numeroDocumento.hashCode());
		result = prime * result
				+ ((sectorId == null) ? 0 : sectorId.hashCode());
		result = prime * result
				+ ((tipoDocId == null) ? 0 : tipoDocId.hashCode());
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
		PersonaSectorPKEntity other = (PersonaSectorPKEntity) obj;
		if (numeroDocumento == null) {
			if (other.numeroDocumento != null)
				return false;
		} else if (!numeroDocumento.equals(other.numeroDocumento))
			return false;
		if (sectorId == null) {
			if (other.sectorId != null)
				return false;
		} else if (!sectorId.equals(other.sectorId))
			return false;
		if (tipoDocId == null) {
			if (other.tipoDocId != null)
				return false;
		} else if (!tipoDocId.equals(other.tipoDocId))
			return false;
		return true;
	}
}
