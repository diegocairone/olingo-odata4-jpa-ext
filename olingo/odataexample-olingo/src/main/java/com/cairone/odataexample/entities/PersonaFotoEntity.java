package com.cairone.odataexample.entities;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Lob;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity @Table(name="personas_fotos")
public class PersonaFotoEntity {

	@EmbeddedId private PersonaFotoPKEntity pk = null;

	@OneToOne @MapsId("personaPKEntity") @JoinColumns({
		@JoinColumn(name="id_tipodoc", referencedColumnName = "id_tipodoc", nullable = false, insertable = false, updatable = false),
		@JoinColumn(name="numero_documento", referencedColumnName = "numero_documento", nullable = false, insertable = false, updatable = false)
	})
	private PersonaEntity persona = null;
	
	@Column(name="bytes_foto", nullable=true) @Lob
	private byte[] foto = null;
	
	public PersonaFotoEntity() {
		pk = new PersonaFotoPKEntity();
	}

	public PersonaFotoEntity(PersonaEntity personaEntity) {
		this.persona = personaEntity;
		this.pk = new PersonaFotoPKEntity(personaEntity.getTipoDocumento(), personaEntity.getNumeroDocumento());
	}

	public PersonaEntity getPersona() {
		return persona;
	}

	public void setPersona(PersonaEntity personaEntity) {
		this.persona = personaEntity;
		this.pk.setTipoDocId(personaEntity.getTipoDocumento().getId());
		this.pk.setNumeroDocumento(personaEntity.getNumeroDocumento());
	}

	public byte[] getFoto() {
		return foto;
	}

	public void setFoto(byte[] foto) {
		this.foto = foto;
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
		PersonaFotoEntity other = (PersonaFotoEntity) obj;
		if (pk == null) {
			if (other.pk != null)
				return false;
		} else if (!pk.equals(other.pk))
			return false;
		return true;
	}
	
}
