package com.cairone.odataexample.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class UsuarioPermisoPKEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name="id_tipodoc", nullable = false)
	private Integer tipoDocId = null;
	
	@Column(name="numero_documento", length = 15, nullable = false)
	private String numeroDocumento = null;
	
	@Column(name="nombre_permiso", nullable = false, length = 50)
	private String nombrePermiso = null;
	
	public UsuarioPermisoPKEntity() {}

	public UsuarioPermisoPKEntity(Integer tipoDocId, String numeroDocumento,
			String nombrePermiso) {
		super();
		this.tipoDocId = tipoDocId;
		this.numeroDocumento = numeroDocumento;
		this.nombrePermiso = nombrePermiso;
	}
	
	public UsuarioPermisoPKEntity(UsuarioEntity usuarioEntity, PermisoEntity permisoEntity) {
		this(usuarioEntity.getPersona().getTipoDocumento().getId(), usuarioEntity.getPersona().getNumeroDocumento(), permisoEntity.getNombre());
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

	public String getNombrePermiso() {
		return nombrePermiso;
	}

	public void setNombrePermiso(String nombrePermiso) {
		this.nombrePermiso = nombrePermiso;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((nombrePermiso == null) ? 0 : nombrePermiso.hashCode());
		result = prime * result
				+ ((numeroDocumento == null) ? 0 : numeroDocumento.hashCode());
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
		UsuarioPermisoPKEntity other = (UsuarioPermisoPKEntity) obj;
		if (nombrePermiso == null) {
			if (other.nombrePermiso != null)
				return false;
		} else if (!nombrePermiso.equals(other.nombrePermiso))
			return false;
		if (numeroDocumento == null) {
			if (other.numeroDocumento != null)
				return false;
		} else if (!numeroDocumento.equals(other.numeroDocumento))
			return false;
		if (tipoDocId == null) {
			if (other.tipoDocId != null)
				return false;
		} else if (!tipoDocId.equals(other.tipoDocId))
			return false;
		return true;
	}
	
}
