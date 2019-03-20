package org.ibp.api.rest.derived;

import org.apache.commons.lang.math.RandomUtils;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.OverwriteDataException;
import org.ibp.api.java.derived.DerivedVariableService;
import org.ibp.api.java.impl.middleware.derived.DerivedVariableServiceImpl;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.validation.ObjectError;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DerivedVariableResourceTest extends ApiUnitTestBase {

	@Resource
	private DerivedVariableService derivedVariableService;


	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public DerivedVariableService derivedVariableService() {
			return mock(DerivedVariableService.class);
		}
	}

	@Test
	public void testCalculateSuccess() throws Exception {

		final CalculateVariableRequest calculateVariableRequest = new CalculateVariableRequest();
		calculateVariableRequest.setVariableId(RandomUtils.nextInt());
		calculateVariableRequest.setOverwriteExistingData(false);
		calculateVariableRequest.setGeoLocationIds(Arrays.asList(RandomUtils.nextInt()));

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.post(
					"/crops/{crop}/studies/{studyId}/datasets/{datasetId}/derived-variables/calculation", this.cropName, 100,
					102)
				.contentType(this.contentType).content(this.convertObjectToByte(calculateVariableRequest)))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk());

		verify(this.derivedVariableService)
			.execute(100, 102, calculateVariableRequest.getVariableId(), calculateVariableRequest.getGeoLocationIds(),
				calculateVariableRequest.isOverwriteExistingData());

	}

	@Test
	public void testCalculateInvalidRequest() throws Exception {

		final CalculateVariableRequest calculateVariableRequest = new CalculateVariableRequest();
		calculateVariableRequest.setVariableId(RandomUtils.nextInt());
		calculateVariableRequest.setOverwriteExistingData(false);
		calculateVariableRequest.setGeoLocationIds(Arrays.asList(RandomUtils.nextInt()));

		final ObjectError objectError =
			new ObjectError("", new String[] {DerivedVariableServiceImpl.STUDY_EXECUTE_CALCULATION_PARSING_EXCEPTION}, new Object[] {}, "");
		final ApiRequestValidationException exception = new ApiRequestValidationException(Arrays.asList(objectError));

		when(this.derivedVariableService
			.execute(100, 102, calculateVariableRequest.getVariableId(), calculateVariableRequest.getGeoLocationIds(),
				calculateVariableRequest.isOverwriteExistingData())).thenThrow(exception);

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.post(
					"/crops/{crop}/studies/{studyId}/datasets/{datasetId}/derived-variables/calculation", this.cropName, 100,
					102)
				.contentType(this.contentType).content(this.convertObjectToByte(calculateVariableRequest)))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isBadRequest())
			.andExpect(MockMvcResultMatchers.jsonPath(
				"$.errors[0].message",
				is("The system was unable to execute this formula; one or more date values may be invalid. Date values should follow yyyymmdd format.")));

	}

	@Test
	public void testCalculateOverwriteWarning() throws Exception {

		final CalculateVariableRequest calculateVariableRequest = new CalculateVariableRequest();
		calculateVariableRequest.setVariableId(RandomUtils.nextInt());
		calculateVariableRequest.setOverwriteExistingData(false);
		calculateVariableRequest.setGeoLocationIds(Arrays.asList(RandomUtils.nextInt()));

		final OverwriteDataException exception = new OverwriteDataException(null);

		when(this.derivedVariableService
			.execute(100, 102, calculateVariableRequest.getVariableId(), calculateVariableRequest.getGeoLocationIds(),
				calculateVariableRequest.isOverwriteExistingData())).thenThrow(exception);

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.post(
					"/crops/{crop}/studies/{studyId}/datasets/{datasetId}/derived-variables/calculation", this.cropName, 100,
					102)
				.contentType(this.contentType).content(this.convertObjectToByte(calculateVariableRequest)))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string("{\"hasDataOverwrite\":true}"));

	}

	@Test
	public void testDependencyVariables() throws Exception {

		final Set<String> dependencies = new HashSet<>();
		dependencies.add("VAR1");
		dependencies.add("VAR2");

		doReturn(dependencies).when(this.derivedVariableService)
			.getDependencyVariables(100, 102);

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.get(
					"/crops/{crop}/studies/{studyId}/datasets/{datasetId}/derived-variables/missing-dependencies", this.cropName, 100,
					102)
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string("[\"VAR1\",\"VAR2\"]"));

	}

	@Test
	public void testDependencyVariablesForSpecificDerivedTrait() throws Exception {

		final Set<String> dependencies = new HashSet<>();
		dependencies.add("VAR1");
		dependencies.add("VAR2");

		doReturn(dependencies).when(this.derivedVariableService)
			.getDependencyVariables(100, 102, 103);

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.get(
					"/crops/{crop}/studies/{studyId}/datasets/{datasetId}/derived-variables/{variableId}/missing-dependencies",
					this.cropName, 100,
					102, 103)
				.contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string("[\"VAR1\",\"VAR2\"]"));

	}

	@Test
	public void testCountCalculatedVariables() throws Exception {
		final long count = 10;

		doReturn(count).when(this.derivedVariableService)
			.countCalculatedVariablesInDatasets(100, new HashSet<Integer>(Arrays.asList(1, 2, 3)));

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.head("/crops/{crop}/studies/{studyId}/datasets/derived-variables", this.cropName, 100, 102)
				.param("datasetIds", "1,2,3").contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.header().string("X-Total-Count", String.valueOf(count)));
	}

}