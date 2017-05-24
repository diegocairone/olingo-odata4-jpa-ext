package com.cairone.odataexample;

import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.ODataServiceDocumentRequest;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientServiceDocument;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.server.api.ODataApplicationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.Assert;

import com.cairone.olingo.ext.jpa.annotations.EdmEntitySet;
import com.cairone.olingo.ext.jpa.providers.EdmProvider;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = OdataExample.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class OdataExampleTest
{
	@Value("${local.server.port}")
    int port;
	
	private ODataClient client;
	
	private String serviceRoot = OdataExample.SERVICE_ROOT;
	
	@Before
	public void init() {
		serviceRoot = serviceRoot.replace(":8080", ":" + port);
		client = ODataClientFactory.getEdmEnabledClient(serviceRoot);
	}
	
	@Test
	public void checkMetadata() throws ODataApplicationException {
		
		ODataServiceDocumentRequest req = client.getRetrieveRequestFactory().getServiceDocumentRequest(serviceRoot);
		ODataRetrieveResponse<ClientServiceDocument> res = req.execute();
		
		Assert.isTrue(res.getStatusCode() == 200);
		
		ClientServiceDocument clientServiceDocument = res.getBody();
		EdmProvider edmProvider = new EdmProvider()
			.setDefaultEdmPackage(OdataExample.DEFAULT_EDM_PACKAGE)
			.initialize();
		
		clientServiceDocument.getEntitySetNames().forEach(entitySetName -> {
			Class<?> clazz = edmProvider.getEntitySetsMap().get(entitySetName);
			Assert.notNull(clazz);
			
			EdmEntitySet edmEntitySet = clazz.getAnnotation(EdmEntitySet.class);
			Assert.notNull(edmEntitySet);
			
			String entitySetNameInClass = edmEntitySet.value().isEmpty() ? clazz.getSimpleName() : edmEntitySet.value();
			Assert.isTrue(entitySetNameInClass.equals(entitySetName));
		});
	}
	
}
