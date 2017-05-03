package com.cairone.odataexample.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class LocalidadPKEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private ProvinciaPKEntity provinciaPK = null;
	
	@Column(name="id_localidad", nullable = false)
	private Integer localidadId = null;
	
	public LocalidadPKEntity() {
		provinciaPK = new ProvinciaPKEntity();
	}

	public LocalidadPKEntity(Integer paisID, Integer provinciaID, Integer localidadID) {
		super();
		this.localidadId = localidadID;
		this.provinciaPK = new ProvinciaPKEntity(paisID, provinciaID);
	}

	public Integer getPaisId() {
		return provinciaPK.getPaisId();
	}

	public void setPaisId(Integer paisId) {
		this.provinciaPK.setPaisId(paisId);
	}

	public Integer getProvinciaId() {
		return provinciaPK.getProvinciaId();
	}

	public void setProvinciaId(Integer provinciaId) {
		this.provinciaPK.setProvinciaId(provinciaId);
	}

	public Integer getLocalidadId() {
		return localidadId;
	}

	public void setLocalidadId(Integer localidadId) {
		this.localidadId = localidadId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((localidadId == null) ? 0 : localidadId.hashCode());
		result = prime * result
				+ ((provinciaPK == null) ? 0 : provinciaPK.hashCode());
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
		LocalidadPKEntity other = (LocalidadPKEntity) obj;
		if (localidadId == null) {
			if (other.localidadId != null)
				return false;
		} else if (!localidadId.equals(other.localidadId))
			return false;
		if (provinciaPK == null) {
			if (other.provinciaPK != null)
				return false;
		} else if (!provinciaPK.equals(other.provinciaPK))
			return false;
		return true;
	}
}
