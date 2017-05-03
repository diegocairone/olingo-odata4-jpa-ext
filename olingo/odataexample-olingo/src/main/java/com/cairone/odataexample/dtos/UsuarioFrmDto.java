package com.cairone.odataexample.dtos;

import com.cairone.odataexample.edm.resources.UsuarioEdm;
import com.cairone.odataexample.entities.UsuarioEntity;

public class UsuarioFrmDto {

	private Integer tipoDocumentoId = null;
	private String numeroDocumento = null;
	private String nombreUsuario = null;
	private Boolean cuentaVencida = null;
	private Boolean claveVencida = null;
	private Boolean cuentaBloqueada = null;
	private Boolean usuarioHabilitado = null;

	public UsuarioFrmDto() {}

	public UsuarioFrmDto(Integer tipoDocumentoId, String numeroDocumento, String nombreUsuario, Boolean cuentaVencida, Boolean claveVencida, Boolean cuentaBloqueada, Boolean usuarioHabilitado) {
		super();
		this.tipoDocumentoId = tipoDocumentoId;
		this.numeroDocumento = numeroDocumento;
		this.nombreUsuario = nombreUsuario;
		this.cuentaVencida = cuentaVencida;
		this.claveVencida = claveVencida;
		this.cuentaBloqueada = cuentaBloqueada;
		this.usuarioHabilitado = usuarioHabilitado;
	}
	
	public UsuarioFrmDto(UsuarioEdm usuarioEdm) {
		this(usuarioEdm.getTipoDocumentoId(), usuarioEdm.getNumeroDocumento(), usuarioEdm.getNombreUsuario(), usuarioEdm.getCuentaVencida(), usuarioEdm.getClaveVencida(), usuarioEdm.getCuentaBloqueada(), usuarioEdm.getUsuarioHabilitado());
	}
	
	public UsuarioFrmDto(UsuarioEntity usuarioEntity) {
		this(usuarioEntity.getPersona().getTipoDocumento().getId(), usuarioEntity.getPersona().getNumeroDocumento(), usuarioEntity.getNombreUsuario(), usuarioEntity.getCuentaVencida(), usuarioEntity.getClaveVencida(), usuarioEntity.getCuentaBloqueada(), usuarioEntity.getUsuarioHabilitado());
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
}
