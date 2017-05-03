package com.cairone.odataexample.entities;

import java.io.Serializable;

import javax.persistence.*;

@Entity @Table(name="provincias", uniqueConstraints = @UniqueConstraint(columnNames = {"id_pais", "nombre"}))
public class ProvinciaEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private ProvinciaPKEntity pk = null;

	@Column(name="id_provincia", nullable = false, insertable = false, updatable = false)
	private Integer id = null;
	
	@Column(name="nombre", nullable = false, length = 100)
	private String nombre = null;

	@OneToOne @JoinColumn(name="id_pais", nullable = false, insertable = false, updatable = false)
	private PaisEntity pais = null;

	public ProvinciaEntity() {
		this.pk = new ProvinciaPKEntity();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
		this.pk.setProvinciaId(id);
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public PaisEntity getPais() {
		return this.pais;
	}

	public void setPais(PaisEntity pais) {
		this.pais = pais;
		this.pk.setPaisId(pais.getId());
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
		ProvinciaEntity other = (ProvinciaEntity) obj;
		if (pk == null) {
			if (other.pk != null)
				return false;
		} else if (!pk.equals(other.pk))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%s [%s] EN %s", nombre, id, pais);
	}
}