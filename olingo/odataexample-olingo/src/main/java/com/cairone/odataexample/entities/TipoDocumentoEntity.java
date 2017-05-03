package com.cairone.odataexample.entities;

import java.io.Serializable;

import javax.persistence.*;

@Entity @Table(name="tipos_documentos")
public class TipoDocumentoEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id @Column(name="id_tipodoc")
	private Integer id = null;

	@Column(name="nombre", nullable = false, length = 100)
	private String nombre;

	@Column(name="abreviatura", nullable = true, length = 5)
	private String abreviatura = null;

	public TipoDocumentoEntity() {}

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

	public String getAbreviatura() {
		return abreviatura;
	}

	public void setAbreviatura(String abreviatura) {
		this.abreviatura = abreviatura;
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
		TipoDocumentoEntity other = (TipoDocumentoEntity) obj;
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