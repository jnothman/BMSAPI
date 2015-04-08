package org.ibp.api.impl.middleware.ontology.validator;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.ontology.MethodRequest;
import org.ibp.api.java.impl.middleware.ontology.validator.MethodRequestValidator;
import org.junit.Assert;
import org.mockito.Mockito;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

public class MethodRequestValidatorTest extends ApiUnitTestBase {

    @Configuration
    public static class TestConfiguration {

        @Bean
        @Primary
        public OntologyManagerService ontologyManagerService() {
            return Mockito.mock(OntologyManagerService.class);
        }

        @Bean
        @Primary
        public MethodRequestValidator methodRequestValidator() {
            return Mockito.mock(MethodRequestValidator.class);
        }
    }

    @Autowired OntologyManagerService ontologyManagerService;

    @Autowired MethodRequestValidator methodRequestValidator;

    Integer cvId = CvId.METHODS.getId();
    String methodName = "MyMethod";
    String description = "Method Description";

    @Before
    public void reset(){
        Mockito.reset(ontologyManagerService);
    }

    @After
    public void validate() {
        Mockito.validateMockitoUsage();
    }

    /**
     * Test for Name is required
     * @throws MiddlewareQueryException
     */
    @Test
    public void testWithNullNameRequest() throws MiddlewareQueryException {

        Mockito.doReturn(null).when(ontologyManagerService).getTermByNameAndCvId(methodName, cvId);

        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

        MethodRequest request = new MethodRequest();
        request.setName("");
        request.setDescription(description);

        methodRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("name"));
    }

    /**
     * Test for Name is unique
     * @throws MiddlewareQueryException
     */
    @Test
    public void testWithUniqueNonNullMethodName() throws MiddlewareQueryException {

        Mockito.doReturn(new Term(10, methodName, description)).when(ontologyManagerService).getTermByNameAndCvId(methodName, cvId);

        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

        MethodRequest request = new MethodRequest();
        request.setName(methodName);
        request.setDescription(description);

        methodRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("name"));
    }

    /**
     * Test for Name cannot change if the method is already in use
     * @throws MiddlewareQueryException
     */
    @Test
    public void testWithNonEditableRequest() throws MiddlewareQueryException {

        MethodRequest request = new MethodRequest();
        request.setId(10);
        request.setName(methodName);
        request.setDescription(description);

        Mockito.doReturn(new Term(10, methodName, description)).when(ontologyManagerService).getTermByNameAndCvId(methodName, cvId);
        Mockito.doReturn(true).when(ontologyManagerService).isTermReferred(request.getId());

        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

        methodRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertTrue(bindingResult.getAllErrors().size() == 1);
    }

    /**
     * Test for to check name length not exceed 200 characters
     */
    @Test
    public void testWithNameLengthExceedMaxLimit() throws MiddlewareQueryException {
        Mockito.doReturn(null).when(ontologyManagerService).getTermByNameAndCvId(methodName, cvId);

        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

        MethodRequest request = new MethodRequest();
        request.setName(randomString(201));
        request.setDescription(description);

        methodRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("name"));
    }

    /**
     * Test for to check description length not exceed 255 characters
     */
    @Test
    public void testWithDescriptionLengthExceedMaxLimit() throws MiddlewareQueryException {
        Mockito.doReturn(null).when(ontologyManagerService).getTermByNameAndCvId(methodName, cvId);

        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

        MethodRequest request = new MethodRequest();
        request.setName(methodName);
        request.setDescription(randomString(260));

        methodRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("description"));
    }

    /**
     * Test for valid request
     */
    @Test
    public void testWithValidRequest() throws MiddlewareQueryException {

        Mockito.doReturn(null).when(ontologyManagerService).getTermByNameAndCvId(methodName, cvId);

        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");

        MethodRequest request = new MethodRequest();
        request.setName(methodName);
        request.setDescription(description);

        methodRequestValidator.validate(request, bindingResult);
        Assert.assertFalse(bindingResult.hasErrors());
    }

}