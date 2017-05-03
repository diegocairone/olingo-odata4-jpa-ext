package com.cairone.odataexample.edm.resources;

import com.cairone.odataexample.OdataExample;
import com.cairone.odataexample.entities.TipoDocumentoEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntitySet;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;
import com.cairone.olingo.ext.jpa.annotations.ODataJPAEntity;

@EdmEntity(name = "TipoDocumento", key = { "id" }, namespace = OdataExample.NAME_SPACE, containerName = OdataExample.CONTAINER_NAME)
@EdmEntitySet("TiposDocumentos")
@ODataJPAEntity("com.cairone.odataexample.entities.TipoDocumentoEntity")
public class TipoDocumentoEdm {

	@EdmProperty(name="id", nullable = false)
	private Integer id = null;
	
	@EdmProperty(name="nombre", nullable = false)
	private String nombre = null;
	
	@EdmProperty(name="abreviatura", nullable = true)
	private String abreviatura = null;

	public TipoDocumentoEdm() {}

	public TipoDocumentoEdm(Integer id, String nombre, String abreviatura) {
		super();
		this.id = id;
		this.nombre = nombre;
		this.abreviatura = abreviatura;
	}

	public TipoDocumentoEdm(TipoDocumentoEntity tipoDocumentoEntity) {
		this(tipoDocumentoEntity.getId(), tipoDocumentoEntity.getNombre(), tipoDocumentoEntity.getAbreviatura());
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
		this.nombre = nombre;
	}

	public String getAbreviatura() {
		return abreviatura;
	}

	public void setAbreviatura(String abreviatura) {
		this.abreviatura = abreviatura;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		TipoDocumentoEdm other = (TipoDocumentoEdm) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
