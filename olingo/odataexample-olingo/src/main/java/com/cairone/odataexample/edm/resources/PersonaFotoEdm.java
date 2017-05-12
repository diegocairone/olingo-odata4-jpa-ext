package com.cairone.odataexample.edm.resources;

import com.cairone.odataexample.OdataExample;
import com.cairone.odataexample.entities.PersonaEntity;
import com.cairone.odataexample.entities.PersonaFotoEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntitySet;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;
import com.cairone.olingo.ext.jpa.annotations.ODataJPAEntity;
import com.cairone.olingo.ext.jpa.annotations.ODataJPAProperty;

@EdmEntity(name = "PersonaFoto", hasStream=true, key = { "tipoDocumentoId", "numeroDocumento" }, namespace = OdataExample.NAME_SPACE, containerName = OdataExample.CONTAINER_NAME)
@EdmEntitySet("PersonasFoto")
@ODataJPAEntity("PersonaFotoEntity")
public class PersonaFotoEdm {

	@EdmProperty(name="tipoDocumentoId", nullable = false) @ODataJPAProperty("tipoDocumento.id")
	private Integer tipoDocumentoId = null;
	
	@EdmProperty(name="numeroDocumento", nullable = false)
	private String numeroDocumento = null;
	
	public PersonaFotoEdm() {}

	public PersonaFotoEdm(Integer tipoDocumentoId, String numeroDocumento) {
		super();
		this.tipoDocumentoId = tipoDocumentoId;
		this.numeroDocumento = numeroDocumento;
	}

	public PersonaFotoEdm(PersonaEntity personaEntity) {
		this(personaEntity.getTipoDocumento().getId(), personaEntity.getNumeroDocumento());
	}
	
	public PersonaFotoEdm(PersonaFotoEntity personaFotoEntity) {
		this(personaFotoEntity.getPersona().getTipoDocumento().getId(), personaFotoEntity.getPersona().getNumeroDocumento());
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
