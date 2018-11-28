package org.ibp.api.java.impl.middleware.dataset.validator;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

public class ObservationsTableValidatorTest {

	final ObservationsTableValidator observationsTableValidator = new ObservationsTableValidator();

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateListFailsWhenDataHasLessThan2Elements() throws Exception {
		final List<List<String>> data = new ArrayList<>();
		final List<String> headers = Arrays.asList("A", "B");
		data.add(headers);
		try {
			observationsTableValidator.validateList(data);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("table.should.have.at.least.two.rows"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateListFailsWhenDataElementsHasDifferentLength() throws Exception {
		final List<List<String>> data = new ArrayList<>();
		final List<String> headers = Arrays.asList("A", "B");
		final List<String> rows = Arrays.asList("A");
		data.add(headers);
		data.add(rows);
		try {
			observationsTableValidator.validateList(data);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("table.format.inconsistency"));
			throw e;
		}
	}

	@Test
	public void testValidateListOk() throws Exception {
		final List<List<String>> data = new ArrayList<>();
		final List<String> headers = Arrays.asList("A", "B");
		final List<String> rows = Arrays.asList("A", "B");
		data.add(headers);
		data.add(rows);
		observationsTableValidator.validateList(data);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateObservationsValuesDataTypesInvalidNumberGivenRanges() throws Exception {
		final Table<String, String, String> data = HashBasedTable.create();
		data.put("Obs1", "A", "A");
		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setName("A");
		measurementVariable.setMinRange(1D);
		measurementVariable.setMaxRange(2D);
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		measurementVariables.add(measurementVariable);
		try {
			observationsTableValidator.validateObservationsValuesDataTypes(data, measurementVariables);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("warning.import.save.invalidCellValue"));
			throw e;
		}
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateObservationsValuesDataTypesInvalidNumberGivenVariableType() throws Exception {
		final Table<String, String, String> data = HashBasedTable.create();
		data.put("Obs1", "A", "A");
		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setName("A");
		measurementVariable.setDataType("Numeric");
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		measurementVariables.add(measurementVariable);
		try {
			observationsTableValidator.validateObservationsValuesDataTypes(data, measurementVariables);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("warning.import.save.invalidCellValue"));
			throw e;
		}
	}

	@Test
	public void testValidateObservationsValuesDataTypesValidNumber() throws Exception {
		final Table<String, String, String> data = HashBasedTable.create();
		data.put("Obs1", "A", "1");
		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setName("A");
		measurementVariable.setDataType("Numeric");
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		measurementVariables.add(measurementVariable);
		observationsTableValidator.validateObservationsValuesDataTypes(data, measurementVariables);
	}

	@Test
	public void testValidateObservationsValuesDataTypesMissingValue() throws Exception {
		final Table<String, String, String> data = HashBasedTable.create();
		data.put("Obs1", "A", "missing");
		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setName("A");
		measurementVariable.setDataType("Numeric");
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		measurementVariables.add(measurementVariable);
		observationsTableValidator.validateObservationsValuesDataTypes(data, measurementVariables);
	}

	@Test(expected = ApiRequestValidationException.class)
	public void testValidateObservationsValuesDataTypesInvalidDate() throws Exception {
		final Table<String, String, String> data = HashBasedTable.create();
		data.put("Obs1", "A", "A");
		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setName("A");
		measurementVariable.setDataTypeId(TermId.DATE_VARIABLE.getId());
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		measurementVariables.add(measurementVariable);
		try {
			observationsTableValidator.validateObservationsValuesDataTypes(data, measurementVariables);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("warning.import.save.invalidCellValue"));
			throw e;
		}
	}

	@Test
	public void testValidateObservationsValuesDataTypesValidDate() throws Exception {
		final Table<String, String, String> data = HashBasedTable.create();
		data.put("Obs1", "A", "20181010");
		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setName("A");
		measurementVariable.setDataTypeId(TermId.DATE_VARIABLE.getId());
		final List<MeasurementVariable> measurementVariables = new ArrayList<>();
		measurementVariables.add(measurementVariable);
		observationsTableValidator.validateObservationsValuesDataTypes(data, measurementVariables);
	}

}
