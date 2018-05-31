package com.cairone.olingo.ext.demo.edm.resources;

import java.time.LocalDate;

import com.cairone.olingo.ext.demo.AppDemoConstants;
import com.cairone.olingo.ext.jpa.annotations.EdmEntity;
import com.cairone.olingo.ext.jpa.annotations.EdmEntitySet;
import com.cairone.olingo.ext.jpa.annotations.EdmProperty;

@EdmEntity(name = "ServerDate", key = "Date", namespace = AppDemoConstants.NAME_SPACE, containerName = AppDemoConstants.CONTAINER_NAME)
@EdmEntitySet("ServerDates")
public class ServerDateEdm {

	@EdmProperty(name = "Date")
	private LocalDate localDate = null;
	
	public ServerDateEdm() {
		localDate = LocalDate.now();
	}

	public LocalDate getLocalDate() {
		return localDate;
	}

	public void setLocalDate(LocalDate localDate) {
		this.localDate = localDate;
	}
}
