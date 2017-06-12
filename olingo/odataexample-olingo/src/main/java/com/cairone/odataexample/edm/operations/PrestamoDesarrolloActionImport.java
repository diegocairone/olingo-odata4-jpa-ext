package com.cairone.odataexample.edm.operations;

import com.cairone.odataexample.OdataExample;
import com.cairone.olingo.ext.jpa.annotations.EdmFunctionImport;

@EdmFunctionImport(namespace = OdataExample.NAME_SPACE, name = "PrestamoDesarrolloFunctionImport", function = "PrestamoDesarrolloFunction", entitySet = "PrestamoCuotas")
public class PrestamoDesarrolloActionImport {

}
