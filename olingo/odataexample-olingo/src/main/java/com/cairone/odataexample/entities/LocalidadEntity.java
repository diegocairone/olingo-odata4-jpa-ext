package com.cairone.odataexample.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity @Table(name="localidades")
public class LocalidadEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private LocalidadPKEntity pk = null;
	
	@OneToOne @MapsId("provinciaPK") @JoinColumns({
		@JoinColumn(name="id_pais", referencedColumnName = "id_pais", nullable = false, insertable = false, updatable = false),
		@JoinColumn(name="id_provincia", referencedColumnName = "id_provincia", nullable = false, insertable = false, updatable = false)
	}) 
	private ProvinciaEntity provincia = null;
	
	@Column(name="id_localidad", nullable = false, insertable = false, updatable = false)
	private Integer id = null;
	
	@Column(name="nombre", nullable = false, length = 100)
	private String nombre = null;
	
	@Column(name="cp", nullable = true)
	private Integer cp = null;
	
	@Column(name="prefijo", nullable = true)
	private Integer prefijo = null;
	
	public LocalidadEntity() {
		this.pk = new LocalidadPKEntity();
	}

	public ProvinciaEntity getProvincia() {
		return provincia;
	}

	public void setProvincia(ProvinciaEntity provincia) {
		this.provincia = provincia;
		this.pk.setPaisId(provincia.getPais().getId());
		this.pk.setProvinciaId(provincia.getId());
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
		this.pk.setLocalidadId(id);
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Integer getCp() {
		return cp;
	}

	public void setCp(Integer cp) {
		this.cp = cp;
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
		LocalidadEntity other = (LocalidadEntity) obj;
		if (pk == null) {
			if (other.pk != null)
				return false;
		} else if (!pk.equals(other.pk))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("%s [%s]", nombre, id);
	}
}
