package com.cairone.odataexample.edm.resources;

import com.cairone.odataexample.OdataExample;
import com.cairone.odataexample.entities.PermisoEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntitySet;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;
import com.cairone.olingo.ext.jpa.annotations.ODataJPAEntity;

@EdmEntity(name = "Permiso", key = { "id" }, namespace = OdataExample.NAME_SPACE, containerName = OdataExample.CONTAINER_NAME)
@EdmEntitySet("Permisos")
@ODataJPAEntity("com.cairone.odataexample.entities.PermisoEntity")
public class PermisoEdm {

	@EdmProperty(name="id", nullable = false, maxLength=15)
	private String id = null;
	
	@EdmProperty(name="descripcion", nullable = false, maxLength=200)
	private String descripcion = null;

	public PermisoEdm() {}

	public PermisoEdm(String id, String descripcion) {
		super();
		this.id = id;
		this.descripcion = descripcion;
	}
	
	public PermisoEdm(PermisoEntity permisoEntity) {
		this(permisoEntity.getNombre(), permisoEntity.getDescripcion());
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
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
		PermisoEdm other = (PermisoEdm) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
