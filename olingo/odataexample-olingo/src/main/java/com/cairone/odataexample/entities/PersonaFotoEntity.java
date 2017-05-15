package com.cairone.odataexample.entities;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity @Table(name="personas_fotos")
public class PersonaFotoEntity {

	@Id @Column(name="uuid_foto")
	private String uuid = null;

	@Column(name="bytes_foto", nullable=true) @Lob
	private byte[] foto = null;
	
	public PersonaFotoEntity() {}

	public PersonaFotoEntity(String uuid, byte[] foto) {
		super();
		this.uuid = uuid;
		this.foto = foto;
	}

	public PersonaFotoEntity(byte[] foto) {
		this(UUID.randomUUID().toString(), foto);
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
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
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
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
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}

}
