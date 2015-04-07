package org.ibp.api.java.impl.middleware.ontology.validator;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.domain.ontology.PropertyRequest;
import org.junit.Assert;
import org.mockito.Mockito;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import java.util.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

public class PropertyRequestValidatorTest extends ApiUnitTestBase {

    @Configuration
    public static class TestConfiguration {

        @Bean
        @Primary
        public OntologyManagerService ontologyManagerService() {
            return Mockito.mock(OntologyManagerService.class);
        }

        @Bean
        @Primary
        public PropertyRequestValidator propertyRequestValidator() {
            return Mockito.mock(PropertyRequestValidator.class);
        }
    }

    @Autowired
    OntologyManagerService ontologyManagerService;

    @Autowired
    PropertyRequestValidator propertyRequestValidator;

    Integer cvId = CvId.PROPERTIES.getId();
    String propertyName = "MyProperty";
    String description = "Property Description";

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

        Mockito.doReturn(null).when(ontologyManagerService).getTermByNameAndCvId(propertyName, cvId);

        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

        PropertyRequest request = new PropertyRequest();
        request.setName("");
        request.setDescription(description);

        propertyRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("name"));
    }

    /**
     * Test for Name is unique
     * @throws MiddlewareQueryException
     */
    @Test
    public void testWithUniqueNonNullPropertyName() throws MiddlewareQueryException {

        Mockito.doReturn(new Term(10, propertyName, description)).when(ontologyManagerService).getTermByNameAndCvId(propertyName, cvId);

        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

        PropertyRequest request = new PropertyRequest();
        request.setName(propertyName);
        request.setDescription(description);

        propertyRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("name"));
    }

    /**
     * Test for should have at least one class and that is valid
     * @throws MiddlewareQueryException
     */
    @Test
    public void testWithClassNameNonEmptyUniqueValues() throws MiddlewareQueryException {

        Mockito.doReturn(null).when(ontologyManagerService).getTermByNameAndCvId(propertyName, cvId);

        PropertyRequest request = new PropertyRequest();
        request.setName(propertyName);
        request.setDescription(description);

        //Assert for no class defined
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

        propertyRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("classes"));

        //Assert for empty class is defined
        bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

        request.setClasses(Collections.singletonList(""));
        propertyRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("classes[0]"));

        //Assert for duplicate class names
        bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

        request.setClasses(Arrays.asList("class", "class"));
        propertyRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("classes"));
    }

    /**
     * Test for Name cannot change if the property is already in use
     * @throws MiddlewareQueryException
     */
    @Test
    public void testWithNonEditableRequest() throws MiddlewareQueryException, MiddlewareException {

        Term dbTerm = new Term(10, propertyName, description);
        Property toReturn = new Property(dbTerm);

        PropertyRequest request = new PropertyRequest();
        request.setId(10);
        request.setName(propertyName + "0");
        request.setDescription(description);

        Mockito.doReturn(dbTerm).when(ontologyManagerService).getTermByNameAndCvId(propertyName, cvId);
        Mockito.doReturn(true).when(ontologyManagerService).isTermReferred(request.getId());
        Mockito.doReturn(toReturn).when(ontologyManagerService).getProperty(request.getId());

        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

        propertyRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
    }

    /**
     * Test for to check name length not exceed 200 characters
     * @throws MiddlewareQueryException
     */
    @Test
    public void testWithNameLengthExceedMaxLimit() throws MiddlewareQueryException {

        Mockito.doReturn(null).when(ontologyManagerService).getTermByNameAndCvId(propertyName, cvId);

        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

        PropertyRequest request = new PropertyRequest();
        request.setName(randomString(205));
        request.setDescription(description);

        propertyRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("name"));
    }

    /**
     * Test for to check description length not exceed 255 characters
     * @throws MiddlewareQueryException
     */
    @Test
    public void testWithDescriptionLengthExceedMaxLimit() throws MiddlewareQueryException {

        Mockito.doReturn(null).when(ontologyManagerService).getTermByNameAndCvId(propertyName, cvId);

        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

        PropertyRequest request = new PropertyRequest();
        request.setName(propertyName);
        request.setDescription(randomString(260));

        propertyRequestValidator.validate(request, bindingResult);
        Assert.assertTrue(bindingResult.hasErrors());
        Assert.assertNotNull(bindingResult.getFieldError("description"));
    }

    /**
     * Test for valid request
     * @throws MiddlewareQueryException
     */
    @Test
    public void testWithValidRequest() throws MiddlewareQueryException {

        Mockito.doReturn(null).when(ontologyManagerService).getTermByNameAndCvId(propertyName, cvId);
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Property");

        PropertyRequest request = new PropertyRequest();
        request.setName(propertyName);
        request.setDescription(description);
        request.setClasses(Arrays.asList("Class1", "Class2"));

        propertyRequestValidator.validate(request, bindingResult);
        Assert.assertFalse(bindingResult.hasErrors());
    }
}