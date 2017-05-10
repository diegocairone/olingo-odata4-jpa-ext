package com.cairone.odataexample.edm.resources;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.cairone.odataexample.OdataExample;
import com.cairone.odataexample.entities.UsuarioEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntitySet;
import com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;
import com.cairone.olingo.ext.jpa.annotations.ODataJPAEntity;
import com.cairone.olingo.ext.jpa.annotations.ODataJPAProperty;

@EdmEntity(name = "Usuario", key = { "tipoDocumentoId", "numeroDocumento" }, namespace = OdataExample.NAME_SPACE, containerName = OdataExample.CONTAINER_NAME)
@EdmEntitySet("Usuarios")
@ODataJPAEntity("com.cairone.odataexample.entities.UsuarioEntity")
public class UsuarioEdm {

	@EdmProperty(name="tipoDocumentoId", nullable = false) @ODataJPAProperty("persona.tipoDocumento.id")
	private Integer tipoDocumentoId = null;
	
	@EdmProperty(name="numeroDocumento", nullable = false) @ODataJPAProperty("persona.numeroDocumento")
	private String numeroDocumento = null;
	
	@EdmProperty(name="nombreUsuario", nullable = false, maxLength=200)
	private String nombreUsuario = null;
	
	@EdmProperty(name="fechaAlta")
	private LocalDate fechaAlta = null;
	
	@EdmProperty(name="cuentaVencida", nullable = false)
	private Boolean cuentaVencida = null;

	@EdmProperty(name="claveVencida", nullable = false)
	private Boolean claveVencida = null;

	@EdmProperty(name="cuentaBloqueada", nullable = false)
	private Boolean cuentaBloqueada = null;

	@EdmProperty(name="usuarioHabilitado", nullable = false)
	private Boolean usuarioHabilitado = null;

	@EdmNavigationProperty(name="persona")
	private PersonaEdm persona = null;
	
	@EdmNavigationProperty(name="permisos") @ODataJPAProperty("usuarioPermisoEntities")
	private List<PermisoEdm> permisos = null;

	public UsuarioEdm() {
		permisos = new ArrayList<PermisoEdm>();
	}

	public UsuarioEdm(Integer tipoDocumentoId, String numeroDocumento, String nombreUsuario, LocalDate fechaAlta, Boolean cuentaVencida, Boolean claveVencida, Boolean cuentaBloqueada, Boolean usuarioHabilitado, PersonaEdm persona) {
		super();
		this.tipoDocumentoId = tipoDocumentoId;
		this.numeroDocumento = numeroDocumento;
		this.nombreUsuario = nombreUsuario;
		this.fechaAlta = fechaAlta;
		this.cuentaVencida = cuentaVencida;
		this.claveVencida = claveVencida;
		this.cuentaBloqueada = cuentaBloqueada;
		this.usuarioHabilitado = usuarioHabilitado;
		this.persona = persona;
		this.permisos = new ArrayList<PermisoEdm>();
	}
	
	public UsuarioEdm(UsuarioEntity usuarioEntity) {
		this(
				usuarioEntity.getPersona().getTipoDocumento().getId(),
				usuarioEntity.getPersona().getNumeroDocumento(),
				usuarioEntity.getNombreUsuario(),
				usuarioEntity.getFechaAlta(),
				usuarioEntity.getCuentaVencida(),
				usuarioEntity.getClaveVencida(),
				usuarioEntity.getCuentaBloqueada(),
				usuarioEntity.getUsuarioHabilitado(),
				new PersonaEdm(usuarioEntity.getPersona()));
		
		if(usuarioEntity.getUsuarioPermisoEntities() != null) {
			usuarioEntity.getUsuarioPermisoEntities().forEach(usuarioPermisoEntity -> {
				this.permisos.add(new PermisoEdm(usuarioPermisoEntity.getPermiso()));
			});
		}
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

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
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

	public PersonaEdm getPersona() {
		return persona;
	}

	public void setPersona(PersonaEdm persona) {
		this.persona = persona;
	}

	public List<PermisoEdm> getPermisos() {
		return permisos;
	}

	public void setPermisos(List<PermisoEdm> permisos) {
		this.permisos = permisos;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((persona == null) ? 0 : persona.hashCode());
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
		UsuarioEdm other = (UsuarioEdm) obj;
		if (persona == null) {
			if (other.persona != null)
				return false;
		} else if (!persona.equals(other.persona))
			return false;
		return true;
	}
	
	public static List<UsuarioEdm> crearLista(Iterable<UsuarioEntity> usuarioEntities) {
		List<UsuarioEdm> usuarioEdms = new ArrayList<UsuarioEdm>();
		for(UsuarioEntity usuarioEntity : usuarioEntities) usuarioEdms.add(new UsuarioEdm(usuarioEntity));
		return usuarioEdms;
	}
}
