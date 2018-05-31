package com.cairone.olingo.ext.demo.edm.operations;

import com.cairone.olingo.ext.demo.AppDemoConstants;
import com.cairone.olingo.ext.jpa.annotations.EdmFunctionImport;

@EdmFunctionImport(namespace = AppDemoConstants.NAME_SPACE, name = "SearchPersonsByGender", function = "SearchPersonsByGenderFunction", entitySet = "People")
public class PersonGenderSearchFunctionImport {

}
