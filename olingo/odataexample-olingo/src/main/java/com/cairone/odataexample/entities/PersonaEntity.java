package com.cairone.odataexample.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.cairone.odataexample.enums.GeneroEnum;

@Entity @Table(name="personas")
public class PersonaEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@EmbeddedId private PersonaPKEntity pk = null;
	
	@OneToOne @JoinColumn(name = "id_tipodoc", nullable = false, insertable = false, updatable = false)
	private TipoDocumentoEntity tipoDocumento = null;

	@Column(name="numero_documento", nullable = false, length = 15, insertable = false, updatable = false)
	private String numeroDocumento = null;

	@Column(name="nombres", nullable = false, length = 100)
	private String nombres = null;

	@Column(name="apellidos", nullable = false, length = 100)
	private String apellidos = null;

	@Column(name="apodo", length = 100)
	private String apodo = null;
	
	@OneToOne @JoinColumns({
		@JoinColumn(name = "id_pais", referencedColumnName = "id_pais", nullable = false),
		@JoinColumn(name = "id_provincia", referencedColumnName = "id_provincia", nullable = false),
		@JoinColumn(name = "id_localidad", referencedColumnName = "id_localidad", nullable = false)
	})
	private LocalidadEntity localidad = null;
	
	@Column(name="fecha_alta", nullable = true) 
	private LocalDate fechaAlta = null;
	
	@Column(name="genero", nullable = false, length = 1)
	private GeneroEnum genero = null;
	
	@Column(name="uuid_foto", nullable = true, length = 36)
	private String fotoUUID = null;
	
	@OneToMany(orphanRemoval=true, mappedBy="persona", fetch=FetchType.EAGER)
	private List<PersonaSectorEntity> personaSectorEntities = null;
	
	public PersonaEntity() {
		pk = new PersonaPKEntity();
	}

	public PersonaEntity(TipoDocumentoEntity tipoDocumentoEntity, String numeroDocumento) {
		this.tipoDocumento = tipoDocumentoEntity;
		this.numeroDocumento = numeroDocumento;
		this.pk = new PersonaPKEntity(tipoDocumentoEntity, numeroDocumento);
	}

	public TipoDocumentoEntity getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(TipoDocumentoEntity tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
		this.pk.setTipoDocId(tipoDocumento.getId());
	}

	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
		this.pk.setNumeroDocumento(numeroDocumento);
	}

	public String getNombres() {
		return nombres;
	}

	public void setNombres(String nombres) {
		this.nombres = nombres;
	}

	public String getApellidos() {
		return apellidos;
	}

	public void setApellidos(String apellidos) {
		this.apellidos = apellidos;
	}

	public String getApodo() {
		return apodo;
	}

	public void setApodo(String apodo) {
		this.apodo = apodo;
	}

	public LocalidadEntity getLocalidad() {
		return localidad;
	}

	public void setLocalidad(LocalidadEntity localidad) {
		this.localidad = localidad;
	}

	public LocalDate getFechaAlta() {
		return fechaAlta;
	}

	public void setFechaAlta(LocalDate fechaAlta) {
		this.fechaAlta = fechaAlta;
	}

	public GeneroEnum getGenero() {
		return genero;
	}

	public void setGenero(GeneroEnum genero) {
		this.genero = genero;
	}

	public String getFotoUUID() {
		return fotoUUID;
	}

	public void setFotoUUID(String fotoUUID) {
		this.fotoUUID = fotoUUID;
	}

	public List<PersonaSectorEntity> getPersonaSectorEntities() {
		return personaSectorEntities;
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
		PersonaEntity other = (PersonaEntity) obj;
		if (pk == null) {
			if (other.pk != null)
				return false;
		} else if (!pk.equals(other.pk))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%s, %s", apellidos, nombres);
	}
}
