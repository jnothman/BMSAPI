package org.ibp.api.rest.ontology;

import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.service.api.OntologyManagerService;
import org.ibp.ApiUnitTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class OntologyClassResourceTest extends ApiUnitTestBase {

    @Configuration
    public static class TestConfiguration {

        @Bean
        @Primary
        public OntologyManagerService ontologyManagerService() {
            return Mockito.mock(OntologyManagerService.class);
        }
    }

    @Autowired
    private OntologyManagerService ontologyManagerService;

    @Before
    public void reset(){
        Mockito.reset(ontologyManagerService);
    }

    @After
    public void validate() {
        validateMockitoUsage();
    }

    @Test
    public void listAllClasses() throws Exception {

        String cropName = "maize";

        List<Term> termList = new ArrayList<>();
        Term term = new Term(1, "Abiotic Stress","");
        termList.add(term);
        term = new Term(2, "Agronomic","");
        termList.add(term);
        term = new Term(3, "Biotic Stress","");
        termList.add(term);

        Mockito.doReturn(termList).when(ontologyManagerService).getAllTraitClass();

        mockMvc.perform(get("/ontology/{cropname}/classes", cropName).contentType(contentType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(termList.size())))
                .andExpect(jsonPath("$[0]", is(termList.get(0).getName())))
                .andDo(print());

        verify(ontologyManagerService, times(1)).getAllTraitClass();
    }
}