package com.cairone.olingo.ext.demo.edm.resources;

import com.cairone.olingo.ext.demo.AppDemoConstants;
import com.cairone.olingo.ext.demo.entities.FormTypeEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntitySet;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;
import com.cairone.olingo.ext.jpa.annotations.ODataJPAEntity;
import com.cairone.olingo.ext.jpa.annotations.ODataQueryDslEntity;

@EdmEntity(name = "FormType", key = "Id", namespace = AppDemoConstants.NAME_SPACE, containerName = AppDemoConstants.CONTAINER_NAME)
@EdmEntitySet("FormTypes")
@ODataJPAEntity("FormTypeEntity")
@ODataQueryDslEntity(jpaentity=FormTypeEntity.class, variable="formTypeEntity")
public class FormTypeEdm {

	@EdmProperty(name = "Id")
	private Integer id = null;
	
	@EdmProperty(name = "Name")
	private String name = null;

	public FormTypeEdm() {}

	public FormTypeEdm(Integer id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public FormTypeEdm(FormTypeEntity formTypeEntity) {
		this(formTypeEntity.getId(), formTypeEntity.getName());
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
		FormTypeEdm other = (FormTypeEdm) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
