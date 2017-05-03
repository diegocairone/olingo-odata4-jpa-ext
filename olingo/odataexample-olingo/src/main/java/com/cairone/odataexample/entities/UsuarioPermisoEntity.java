package com.cairone.odataexample.entities;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity @Table(name="usuarios_permisos")
public class UsuarioPermisoEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private UsuarioPermisoPKEntity pk = null;
	
	@ManyToOne @JoinColumns({
		@JoinColumn(name = "id_tipodoc", referencedColumnName = "id_tipodoc", nullable = false, insertable = false, updatable = false),
		@JoinColumn(name = "numero_documento", referencedColumnName = "numero_documento", nullable = false, insertable = false, updatable = false)
	})
	private UsuarioEntity usuario = null;
	
	@OneToOne @JoinColumn(name = "nombre_permiso", nullable = false, insertable = false, updatable = false)
	private PermisoEntity permiso = null;
	
	public UsuarioPermisoEntity() {
		pk = new UsuarioPermisoPKEntity();
	}

	public UsuarioPermisoEntity(UsuarioEntity usuario, PermisoEntity permiso) {
		super();
		this.usuario = usuario;
		this.permiso = permiso;
		this.pk = new UsuarioPermisoPKEntity(usuario, permiso);
	}

	public UsuarioEntity getUsuario() {
		return usuario;
	}

	public void setUsuario(UsuarioEntity usuario) {
		this.usuario = usuario;
		this.pk.setTipoDocId(usuario.getPersona().getTipoDocumento().getId());
		this.pk.setNumeroDocumento(usuario.getPersona().getNumeroDocumento());
	}

	public PermisoEntity getPermiso() {
		return permiso;
	}

	public void setPermiso(PermisoEntity permiso) {
		this.permiso = permiso;
		this.pk.setNombrePermiso(permiso.getNombre());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pk == null) ? 0 : pk.hashCode());
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
		UsuarioPermisoEntity other = (UsuarioPermisoEntity) obj;
		if (pk == null) {
			if (other.pk != null)
				return false;
		} else if (!pk.equals(other.pk))
			return false;
		return true;
	}
	
}
