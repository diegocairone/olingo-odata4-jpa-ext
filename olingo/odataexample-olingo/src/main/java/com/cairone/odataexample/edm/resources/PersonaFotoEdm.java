package com.cairone.odataexample.edm.resources;

import com.cairone.odataexample.OdataExample;
import com.cairone.odataexample.entities.PersonaFotoEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntitySet;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;
import com.cairone.olingo.ext.jpa.annotations.ODataJPAEntity;

@EdmEntity(name = "PersonaFoto", key = { "tipoDocumentoId", "numeroDocumento" }, namespace = OdataExample.NAME_SPACE, containerName = OdataExample.CONTAINER_NAME)
@EdmEntitySet("PersonasFotos")
@ODataJPAEntity("PersonaFotoEntity")
public class PersonaFotoEdm {
	
	@EdmProperty(name="numeroDocumento", nullable = false)
	private String numeroDocumento = null;
	
	@EdmProperty(name="tipoDocumentoId", nullable = false)
	private Integer tipoDocumentoId = null;
	
	@EdmProperty(name="foto", nullable = false)
	private byte[] foto = null;
	
	public PersonaFotoEdm() {}

	public PersonaFotoEdm(String numeroDocumento, Integer tipoDocumentoId, byte[] foto) {
		super();
		this.numeroDocumento = numeroDocumento;
		this.tipoDocumentoId = tipoDocumentoId;
		this.foto = foto;
	}
	
	public PersonaFotoEdm(PersonaFotoEntity personaFotoEntity) {
		this(personaFotoEntity.getPersona().getNumeroDocumento(), personaFotoEntity.getPersona().getTipoDocumento().getId(), personaFotoEntity.getFoto());
	}

	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	public Integer getTipoDocumentoId() {
		return tipoDocumentoId;
	}

	public void setTipoDocumentoId(Integer tipoDocumentoId) {
		this.tipoDocumentoId = tipoDocumentoId;
	}

	public byte[] getFoto() {
		return foto;
	}

	public void setFoto(byte[] foto) {
		this.foto = foto;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((numeroDocumento == null) ? 0 : numeroDocumento.hashCode());
		result = prime * result
				+ ((tipoDocumentoId == null) ? 0 : tipoDocumentoId.hashCode());
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
		PersonaFotoEdm other = (PersonaFotoEdm) obj;
		if (numeroDocumento == null) {
			if (other.numeroDocumento != null)
				return false;
		} else if (!numeroDocumento.equals(other.numeroDocumento))
			return false;
		if (tipoDocumentoId == null) {
			if (other.tipoDocumentoId != null)
				return false;
		} else if (!tipoDocumentoId.equals(other.tipoDocumentoId))
			return false;
		return true;
	}

}
