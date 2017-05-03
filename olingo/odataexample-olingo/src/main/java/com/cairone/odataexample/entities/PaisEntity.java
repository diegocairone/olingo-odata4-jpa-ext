package com.cairone.odataexample.entities;

import java.io.Serializable;

import javax.persistence.*;

import com.cairone.odataexample.edm.resources.PaisEdm;

@Entity @Table(name="paises")
public class PaisEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id @Column(name="id_pais", nullable = false)
	private Integer id = null;

	@Column(name="nombre", nullable = false, unique = true, length = 100)
	private String nombre = null;

	@Column(name="prefijo", nullable = true)
	private Integer prefijo = null;

	public PaisEntity() {}

	public PaisEntity(Integer id, String nombre, Integer prefijo) {
		super();
		this.id = id;
		this.nombre = nombre;
		this.prefijo = prefijo;
	}

	public PaisEntity(PaisEdm paisEdm) {
		this.id = paisEdm.getId();
		this.nombre = paisEdm.getNombre();
		this.prefijo = paisEdm.getPrefijo();
	}

	public int getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Integer getPrefijo() {
		return prefijo;
	}

	public void setPrefijo(Integer prefijo) {
		this.prefijo = prefijo;
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
		PaisEntity other = (PaisEntity) obj;
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