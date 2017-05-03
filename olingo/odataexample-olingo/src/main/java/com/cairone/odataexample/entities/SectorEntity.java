package com.cairone.odataexample.entities;

import java.io.Serializable;

import javax.persistence.*;

@Entity @Table(name="sectores")
public class SectorEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id @Column(name="id_sector")
	private Integer id = null;

	@Column(name="nombre", nullable = false, length = 100)
	private String nombre = null;

	public SectorEntity() {}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
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
		SectorEntity other = (SectorEntity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%s [%s]", nombre, id);
	}
}