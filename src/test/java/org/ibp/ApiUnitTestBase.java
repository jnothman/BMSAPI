
package org.ibp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.generationcp.commons.derivedvariable.DerivedVariableProcessor;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.service.api.derived_variables.FormulaService;
import org.generationcp.middleware.util.Debug;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.Charset;
import java.util.UUID;

/**
 * This class must only be extended by tests which require Spring context to be loaded.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration(classes = Main.class)
public abstract class ApiUnitTestBase {

	protected final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(),
			Charset.forName("utf8"));

	protected final MediaType csvContentType = new MediaType(MediaType.TEXT_PLAIN.getType(), MediaType.TEXT_PLAIN.getSubtype(),
		Charset.forName("utf8"));


	protected final String cropName = "maize";
	protected final String programUuid = UUID.randomUUID().toString();

	protected MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	protected WorkbenchDataManager workbenchDataManager;

	@Autowired
	protected StudyDataManager studyDataManager;

	@Autowired
	protected UserDataManager userDataManager;

	@Autowired
	protected ObjectMapper jsonMapper;

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public WorkbenchDataManager workbenchDataManager() {
			return Mockito.mock(WorkbenchDataManager.class);
		}

		@Bean
		@Primary
		public org.generationcp.middleware.service.api.study.StudyService getStudyServiceMW() {
			return Mockito.mock(org.generationcp.middleware.service.api.study.StudyService.class);
		}

		@Bean
		@Primary
		public StudyDataManager studyDataManager() {
			return Mockito.mock(StudyDataManager.class);
		}
		
		@Bean
		@Primary
		public UserDataManager userDataManager() {
			return Mockito.mock(UserDataManager.class);
		}

		@Bean
		@Primary
		public FormulaService formulaService() {
			return Mockito.mock(FormulaService.class);
		}

		@Bean
		@Primary
		public DerivedVariableProcessor derivedVariableProcessor() {
			return Mockito.mock(DerivedVariableProcessor.class);
		}

		@Bean
		@Primary
		public TermDataManager termDataManager() {
			return Mockito.mock(TermDataManager.class);
		}

		@Bean
		@Primary
		public OntologyVariableDataManager ontologyVariableDataManager() {
			return Mockito.mock(OntologyVariableDataManager.class);
		}
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
		Mockito.doReturn(new CropType(this.cropName)).when(this.workbenchDataManager).getCropTypeByName(this.cropName);
	}

	@After
	public void tearDown() throws Exception {
		this.mockMvc = null;
	}

	public byte[] convertObjectToByte(Object object) throws JsonProcessingException {
		ObjectWriter ow = jsonMapper.writer().withDefaultPrettyPrinter();
		Debug.println("Request:" + ow.writeValueAsString(object));
		return ow.writeValueAsBytes(object);
	}
}
