package com.cairone.odataexample.entities;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.Transient;

@Embeddable
public class UsuarioPKEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private PersonaPKEntity personaPKEntity = null;
	
	@Transient
	private Integer tipoDocId = null;
	
	@Transient
	private String numeroDocumento = null;

	public UsuarioPKEntity() {
		personaPKEntity = new PersonaPKEntity();
	}

	public UsuarioPKEntity(Integer tipoDocId, String numeroDocumento) {
		this.tipoDocId = tipoDocId;
		this.numeroDocumento = numeroDocumento;
		this.personaPKEntity = new PersonaPKEntity(tipoDocId, numeroDocumento);
	}

	public UsuarioPKEntity(TipoDocumentoEntity tipoDocumentoEntity, String numeroDocumento) {
		this.tipoDocId = tipoDocumentoEntity.getId();
		this.numeroDocumento = numeroDocumento;
		this.personaPKEntity = new PersonaPKEntity(tipoDocumentoEntity.getId(), numeroDocumento);
	}

	public Integer getTipoDocId() {
		return tipoDocId;
	}

	public void setTipoDocId(Integer tipoDocId) {
		this.tipoDocId = tipoDocId;
		this.personaPKEntity.setTipoDocId(tipoDocId);
	}

	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
		this.personaPKEntity.setNumeroDocumento(numeroDocumento);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((personaPKEntity == null) ? 0 : personaPKEntity.hashCode());
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
		UsuarioPKEntity other = (UsuarioPKEntity) obj;
		if (personaPKEntity == null) {
			if (other.personaPKEntity != null)
				return false;
		} else if (!personaPKEntity.equals(other.personaPKEntity))
			return false;
		return true;
	}
}
