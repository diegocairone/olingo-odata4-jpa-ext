package com.cairone.olingo.ext.demo.edm.resources;

import com.cairone.olingo.ext.demo.AppDemoConstants;
import com.cairone.olingo.ext.demo.entities.FormEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntitySet;
import com.cairone.olingo.ext.jpa.annotations.EdmNavigationProperty;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;
import com.cairone.olingo.ext.jpa.annotations.ODataQueryDslEntity;

@EdmEntity(name = "Form", key = "Name", namespace = AppDemoConstants.NAME_SPACE, containerName = AppDemoConstants.CONTAINER_NAME)
@EdmEntitySet("Forms")
@ODataQueryDslEntity(jpaentity=FormEntity.class, variable="formEntity")
public class FormEdm {
	
	@EdmProperty(name = "Name")
	private String id = null;
	
	@EdmProperty(name = "Description")
	private String name = null;

	@EdmNavigationProperty(name = "FormType")
	private FormTypeEdm formType = null;
	
	public FormEdm() {}

	public FormEdm(String id, String name, FormTypeEdm formType) {
		super();
		this.id = id;
		this.name = name;
		this.formType = formType;
	}

	public FormEdm(FormEntity formEntity) {
		this(formEntity.getId(), formEntity.getName(), new FormTypeEdm(formEntity.getFormType()));
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
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
		FormEdm other = (FormEdm) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
