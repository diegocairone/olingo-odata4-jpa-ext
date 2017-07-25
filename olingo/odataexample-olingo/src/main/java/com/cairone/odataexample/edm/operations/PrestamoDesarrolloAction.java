package com.cairone.odataexample.edm.operations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.OdataExample;
import com.cairone.odataexample.edm.resources.PrestamoCuotaEdm;
import com.cairone.olingo.ext.jpa.annotations.EdmFunction;
import com.cairone.olingo.ext.jpa.annotations.EdmParameter;
import com.cairone.olingo.ext.jpa.annotations.EdmReturnType;
import com.cairone.olingo.ext.jpa.interfaces.Operation;

@Component
@EdmFunction(namespace = OdataExample.NAME_SPACE, name = "PrestamoDesarrolloFunction", isBound = false) 
@EdmReturnType(type = "Collection(PrestamoCuota)")
public class PrestamoDesarrolloAction implements Operation<List<PrestamoCuotaEdm>>{

	@EdmParameter(nullable = false)
	private BigDecimal prestamo = null;
	
	@EdmParameter(nullable = false)
	private String tipoTasa = null;
	
	@EdmParameter(nullable = false)
	private BigDecimal tasa = null;
	
	@EdmParameter(nullable = false)
	private Integer cuotas = null;
	
	@EdmParameter(nullable = false)
	private BigDecimal alicuota = null;
	
	@Override
	public List<PrestamoCuotaEdm> doOperation(boolean isBound, Map<String, UriParameter> keyPredicateMap) throws ODataException {
		ArrayList<PrestamoCuotaEdm> prestamoCuotaEdms = new ArrayList<PrestamoCuotaEdm>();
		prestamoCuotaEdms.add(new PrestamoCuotaEdm(1, BigDecimal.valueOf(852.22), BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN));
		return prestamoCuotaEdms;
	}

}
