package com.cairone.odataexample.entities;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity @Table(name="personas_sectores")
public class PersonaSectorEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private PersonaSectorPKEntity pk = null;
	
	@ManyToOne @JoinColumns({
		@JoinColumn(name = "id_tipodoc", referencedColumnName = "id_tipodoc", nullable = false, insertable = false, updatable = false),
		@JoinColumn(name = "numero_documento", referencedColumnName = "numero_documento", nullable = false, insertable = false, updatable = false)
	})
	private PersonaEntity persona = null;
	
	@OneToOne @JoinColumn(name = "id_sector", nullable = false, insertable = false, updatable = false)
	private SectorEntity sector = null;
	
	@Column(name="fecha_ingreso", nullable = false)
	private LocalDate fechaIngreso = null;
	
	public PersonaSectorEntity() {
		this.pk = new PersonaSectorPKEntity();
	}

	public PersonaEntity getPersona() {
		return persona;
	}

	public void setPersona(PersonaEntity personaEntity) {
		this.persona = personaEntity;
		this.pk.setTipoDocId(personaEntity.getTipoDocumento().getId());
		this.pk.setNumeroDocumento(personaEntity.getNumeroDocumento());
	}

	public SectorEntity getSector() {
		return sector;
	}

	public void setSector(SectorEntity sector) {
		this.sector = sector;
		this.pk.setSectorId(sector.getId());
	}

	public LocalDate getFechaIngreso() {
		return fechaIngreso;
	}

	public void setFechaIngreso(LocalDate fechaIngreso) {
		this.fechaIngreso = fechaIngreso;
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
		PersonaSectorEntity other = (PersonaSectorEntity) obj;
		if (pk == null) {
			if (other.pk != null)
				return false;
		} else if (!pk.equals(other.pk))
			return false;
		return true;
	}
}
