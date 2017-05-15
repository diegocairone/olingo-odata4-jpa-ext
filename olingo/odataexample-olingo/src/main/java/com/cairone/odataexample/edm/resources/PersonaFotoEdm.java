package com.cairone.odataexample.edm.resources;

import com.cairone.odataexample.OdataExample;
import com.cairone.olingo.ext.jpa.annotations.EdmEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntitySet;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;
import com.cairone.olingo.ext.jpa.annotations.ODataJPAEntity;

@EdmEntity(name = "PersonaFoto", hasStream=true, key = { "uuid" }, namespace = OdataExample.NAME_SPACE, containerName = OdataExample.CONTAINER_NAME)
@EdmEntitySet("PersonasFotos")
@ODataJPAEntity("PersonaFotoEntity")
public class PersonaFotoEdm {
	
	@EdmProperty(name="uuid", nullable = false)
	private String uuid = null;
	
	@EdmProperty(name="numeroDocumento", nullable = false)
	private String numeroDocumento = null;
	
	@EdmProperty(name="tipoDocumentoId", nullable = false)
	private Integer tipoDocumentoId = null;
	
	public PersonaFotoEdm() {}

	public PersonaFotoEdm(String uuid) {
		super();
		this.uuid = uuid;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
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
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}
}
