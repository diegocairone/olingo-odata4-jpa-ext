package com.cairone.odataexample.entities;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity @Table(name="usuarios")
public class UsuarioEntity {

	@EmbeddedId private UsuarioPKEntity pk = null;

	@OneToOne @MapsId("personaPKEntity") @JoinColumns({
		@JoinColumn(name="id_tipodoc", referencedColumnName = "id_tipodoc", nullable = false, insertable = false, updatable = false),
		@JoinColumn(name="numero_documento", referencedColumnName = "numero_documento", nullable = false, insertable = false, updatable = false)
	})
	private PersonaEntity persona = null;

	@Column(name="nombre_usuario", nullable = false, length = 200)
	private String nombreUsuario = null;

	@Column(name="clave", nullable = false, length = 40)
	private String clave = null;
	
	@Column(name="fecha_alta", nullable = false)
	private LocalDate fechaAlta = null;
	
	@Column(name = "cuenta_vencida", nullable = false)
	private Boolean cuentaVencida = null;

	@Column(name = "clave_vencida", nullable = false)
	private Boolean claveVencida = null;

	@Column(name = "cuenta_bloqueada", nullable = false)
	private Boolean cuentaBloqueada = null;

	@Column(name = "usuario_habilitado", nullable = false)
	private Boolean usuarioHabilitado = null;

	@OneToMany(cascade=CascadeType.ALL, mappedBy="usuario", fetch=FetchType.EAGER)
	private List<UsuarioPermisoEntity> usuarioPermisoEntities = null;
	
	public UsuarioEntity() {
		pk = new UsuarioPKEntity();
	}

	public UsuarioEntity(PersonaEntity personaEntity) {
		this.persona = personaEntity;
		this.pk = new UsuarioPKEntity(personaEntity.getTipoDocumento(), personaEntity.getNumeroDocumento());
	}

	public PersonaEntity getPersona() {
		return persona;
	}

	public void setPersona(PersonaEntity personaEntity) {
		this.persona = personaEntity;
		this.pk.setTipoDocId(personaEntity.getTipoDocumento().getId());
		this.pk.setNumeroDocumento(personaEntity.getNumeroDocumento());
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public LocalDate getFechaAlta() {
		return fechaAlta;
	}

	public void setFechaAlta(LocalDate fechaAlta) {
		this.fechaAlta = fechaAlta;
	}

	public Boolean getCuentaVencida() {
		return cuentaVencida;
	}

	public void setCuentaVencida(Boolean cuentaVencida) {
		this.cuentaVencida = cuentaVencida;
	}

	public Boolean getClaveVencida() {
		return claveVencida;
	}

	public void setClaveVencida(Boolean claveVencida) {
		this.claveVencida = claveVencida;
	}

	public Boolean getCuentaBloqueada() {
		return cuentaBloqueada;
	}

	public void setCuentaBloqueada(Boolean cuentaBloqueada) {
		this.cuentaBloqueada = cuentaBloqueada;
	}

	public Boolean getUsuarioHabilitado() {
		return usuarioHabilitado;
	}

	public void setUsuarioHabilitado(Boolean usuarioHabilitado) {
		this.usuarioHabilitado = usuarioHabilitado;
	}

	public List<UsuarioPermisoEntity> getUsuarioPermisoEntities() {
		return usuarioPermisoEntities;
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
		UsuarioEntity other = (UsuarioEntity) obj;
		if (pk == null) {
			if (other.pk != null)
				return false;
		} else if (!pk.equals(other.pk))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("%s [%s]", nombreUsuario, persona);
	}
}
