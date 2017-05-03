package com.cairone.odataexample.entities;

import java.io.Serializable;

import javax.persistence.*;

@Embeddable
public class ProvinciaPKEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Column(name="id_pais", nullable = false)
	private Integer paisId = null;

	@Column(name="id_provincia", nullable = false)
	private Integer provinciaId = null;

	public ProvinciaPKEntity() {}
	
	public ProvinciaPKEntity(Integer paisId, Integer provinciaId) {
		super();
		this.paisId = paisId;
		this.provinciaId = provinciaId;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((paisId == null) ? 0 : paisId.hashCode());
		result = prime * result
				+ ((provinciaId == null) ? 0 : provinciaId.hashCode());
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
		ProvinciaPKEntity other = (ProvinciaPKEntity) obj;
		if (paisId == null) {
			if (other.paisId != null)
				return false;
		} else if (!paisId.equals(other.paisId))
			return false;
		if (provinciaId == null) {
			if (other.provinciaId != null)
				return false;
		} else if (!provinciaId.equals(other.provinciaId))
			return false;
		return true;
	}
}