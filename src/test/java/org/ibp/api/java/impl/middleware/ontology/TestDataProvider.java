
package org.ibp.api.java.impl.middleware.ontology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.ibp.api.domain.ontology.*;

public class TestDataProvider {

	private static final Integer variableId = 15;
	private static final String variableName = "Variable_Name";
	private static final String variableDescription = "Variable Description";
	private static final String variableAlias = "VA";
	private static final boolean variableIsFavourite = false;
	private static final String programUUID = "abcd";
	private static final Integer variableObservations = 0;
	private static final Integer variableStudies = 0;
	private static final String variableExpectedMin = "12";
	private static final String variableExpectedMax = "16";

	private static final Integer methodId = 10;
	private static final String methodName = "Method Name";
	private static final String methodDescription = "Method Description";

	private static final Integer propertyId = 11;
	private static final String propertyName = "Property Name";
	private static final String propertyDescription = "Property Description";
	private static final String cropOntologyId = "CO:1234567890";

	private static final Integer scaleId = 12;
	private static final String scaleName = "Scale Name";
	private static final String scaleDescription = "Scale Description";
	private static final Integer scaleVocabularyId = 1030;

	private static final String scaleMinValue = "10.01";
	private static final String scaleMaxValue = "20.02";
	private static final List<TermSummary> scaleCategoricalValues = new ArrayList<>(Arrays.asList(new TermSummary(120, "1", "One"), new TermSummary(121, "2", "Two")));

	private static final String className1 = "Agronomic";
	private static final String className2 = "Biotic Stress";
	private static final String className3 = "Study condition";

	public static final List<Term> mwTermList = new ArrayList<>(Arrays.asList(new Term(1, TestDataProvider.className1, ""), new Term(2, TestDataProvider.className2, ""), new Term(3, TestDataProvider.className3, "")));

	public static final org.ibp.api.domain.ontology.DataType numericalDataType = new org.ibp.api.domain.ontology.DataType(String.valueOf(DataType.NUMERIC_VARIABLE.getId()), DataType.NUMERIC_VARIABLE.getName(), false);
	public static final org.ibp.api.domain.ontology.DataType categoricalDataType = new org.ibp.api.domain.ontology.DataType(String.valueOf(DataType.CATEGORICAL_VARIABLE.getId()), DataType.CATEGORICAL_VARIABLE.getName(), false);

	public static final org.ibp.api.domain.ontology.VariableType traitVariable = new org.ibp.api.domain.ontology.VariableType("1808",	"Trait Variable", "Variable for trait study");

	public static Term getMethodTerm() {
		return new Term(TestDataProvider.methodId, TestDataProvider.methodName, TestDataProvider.methodDescription, CvId.METHODS.getId(),null);
	}

	public static Term getPropertyTerm() {
		return new Term(TestDataProvider.propertyId, TestDataProvider.propertyName, TestDataProvider.propertyDescription, CvId.PROPERTIES.getId(), null);
	}

	public static Term getScaleTerm() {
		return new Term(TestDataProvider.scaleId, TestDataProvider.scaleName, TestDataProvider.scaleDescription, CvId.SCALES.getId(), null);
	}

	public static Term getVariableTerm() {
		return new Term(TestDataProvider.variableId, TestDataProvider.variableName, TestDataProvider.variableDescription, CvId.VARIABLES.getId(), false);
	}

	public static Date getDateCreated() {
		Calendar dateCreated = Calendar.getInstance();
		dateCreated.set(2015, Calendar.JANUARY, 1);
		return dateCreated.getTime();
	}

	public static Date getDateModified() {
		Calendar dateLastModified = Calendar.getInstance();
		dateLastModified.set(2015, Calendar.JANUARY, 2);
		return dateLastModified.getTime();
	}

	public static Method getTestMethod() {
		Method method = new Method();
		method.setId(TestDataProvider.methodId);
		method.setName(TestDataProvider.methodName);
		method.setDefinition(TestDataProvider.methodDescription);
		method.setDateCreated(TestDataProvider.getDateCreated());
		method.setDateLastModified(TestDataProvider.getDateModified());
		return method;
	}

	public static List<Method> getTestMethodList(Integer elements) {
		List<Method> methods = new ArrayList<>();
		for (Integer count = 0; count < elements; count++) {
			Integer methodId = 100 + count;
			Method method = new Method();
			method.setId(methodId);
			method.setName(TestDataProvider.methodName + methodId);
			method.setDefinition(TestDataProvider.methodDescription);
			method.setDateCreated(TestDataProvider.getDateCreated());
			method.setDateLastModified(TestDataProvider.getDateModified());
			methods.add(method);
		}
		return methods;
	}

	public static Property getTestProperty() {
		Property property = new Property();
		property.setId(TestDataProvider.propertyId);
		property.setName(TestDataProvider.propertyName);
		property.setDefinition(TestDataProvider.propertyDescription);
		property.setCropOntologyId(TestDataProvider.cropOntologyId);
		property.addClass(TestDataProvider.className1);
		property.setDateCreated(TestDataProvider.getDateCreated());
		property.setDateLastModified(TestDataProvider.getDateModified());
		return property;
	}

	public static List<Property> getTestProperties(Integer elements) {
		List<Property> properties = new ArrayList<>();

		for (Integer count = 0; count < elements; count++) {
			Property property = new Property();
			property.setId(TestDataProvider.propertyId);
			property.setName(TestDataProvider.propertyName);
			property.setDefinition(TestDataProvider.propertyDescription);
			property.setCropOntologyId(TestDataProvider.cropOntologyId);
			property.addClass(TestDataProvider.className1);
			property.setDateCreated(TestDataProvider.getDateCreated());
			property.setDateLastModified(TestDataProvider.getDateModified());
			properties.add(property);
		}
		return properties;
	}

	public static Scale getTestScale() {
		Scale scale = new Scale();
		scale.setId(TestDataProvider.scaleId);
		scale.setName(TestDataProvider.scaleName);
		scale.setVocabularyId(TestDataProvider.scaleVocabularyId);
		scale.setDefinition(TestDataProvider.scaleDescription);
		scale.setDataType(DataType.NUMERIC_VARIABLE);
		scale.setMinValue(TestDataProvider.scaleMinValue);
		scale.setMaxValue(TestDataProvider.scaleMaxValue);
		scale.addCategory(scaleCategoricalValues.get(0));
		scale.setDateCreated(TestDataProvider.getDateCreated());
		scale.setDateLastModified(TestDataProvider.getDateModified());
		return scale;
	}

	public static List<Scale> getTestScales(Integer elements) {
		List<Scale> scaleList = new ArrayList<>();

		for (Integer count = 0; count < elements; count++) {
			Scale scale = new Scale();
			scale.setId(TestDataProvider.scaleId);
			scale.setName(TestDataProvider.scaleName);
			scale.setDefinition(TestDataProvider.scaleDescription);
			scale.setDataType(DataType.NUMERIC_VARIABLE);
			scale.setMinValue(TestDataProvider.scaleMinValue);
			scale.setMaxValue(TestDataProvider.scaleMaxValue);
			scale.addCategory(scaleCategoricalValues.get(0));
			scale.setDateCreated(TestDataProvider.getDateCreated());
			scale.setDateLastModified(TestDataProvider.getDateModified());
			scaleList.add(scale);
		}
		return scaleList;
	}

	public static Variable getTestVariable() {
		Variable variable = new Variable();
		variable.setId(TestDataProvider.variableId);
		variable.setName(TestDataProvider.variableName);
		variable.setDefinition(TestDataProvider.variableDescription);
		variable.setObservations(TestDataProvider.variableObservations);
		variable.setProperty(TestDataProvider.getTestProperty());
		variable.setMethod(TestDataProvider.getTestMethod());
		variable.setScale(TestDataProvider.getTestScale());
		variable.setMinValue(TestDataProvider.variableExpectedMin);
		variable.setMaxValue(TestDataProvider.variableExpectedMax);
		variable.setAlias(TestDataProvider.variableAlias);
		variable.setIsFavorite(TestDataProvider.variableIsFavourite);
		variable.setDateCreated(TestDataProvider.getDateCreated());
		variable.setStudies(TestDataProvider.variableStudies);
		variable.addVariableType(VariableType.ANALYSIS);
		variable.setDateCreated(TestDataProvider.getDateCreated());
		variable.setDateLastModified(TestDataProvider.getDateModified());

		return variable;
	}

	public static List<Variable> getTestVariables(Integer elements) {
		List<Variable> variableList = new ArrayList<>();

		for (Integer count = 0; count < elements; count++) {
			Variable variable = new Variable(new Term(TestDataProvider.variableId + count, TestDataProvider.variableName + count, TestDataProvider.variableDescription + count));
			variable.setMinValue(TestDataProvider.variableExpectedMin);
			variable.setMaxValue(TestDataProvider.variableExpectedMax);
			variable.setAlias(TestDataProvider.variableAlias + "_" + String.valueOf(count));
			variable.setIsFavorite(TestDataProvider.variableIsFavourite);
			variable.setDateCreated(TestDataProvider.getDateCreated());
			variable.setProperty(TestDataProvider.getTestProperty());
			variable.setMethod(TestDataProvider.getTestMethod());
			variable.setScale(TestDataProvider.getTestScale());
			variable.setDateCreated(TestDataProvider.getDateCreated());
			variable.setDateLastModified(TestDataProvider.getDateModified());
			variable.addVariableType(VariableType.ENVIRONMENT_DETAIL);
			variableList.add(variable);
		}
		return variableList;
	}

	public static MetadataDetails getTestMetadataDetails() {
		MetadataDetails metadataDetails = new MetadataDetails();
		metadataDetails.setDateCreated(TestDataProvider.getDateCreated());
		metadataDetails.setDateLastModified(TestDataProvider.getDateModified());
		return metadataDetails;
	}

	public static MethodDetails getTestMethodDetails() {
		MethodDetails method = new MethodDetails();
		method.setId(String.valueOf(TestDataProvider.methodId));
		method.setName(TestDataProvider.methodName);
		method.setDescription(TestDataProvider.methodDescription);
		method.setMetadata(TestDataProvider.getTestMetadataDetails());
		return method;
	}

	public static PropertyDetails getTestPropertyDetails() {
		PropertyDetails propertyDetails = new PropertyDetails();
		propertyDetails.setId(String.valueOf(TestDataProvider.propertyId));
		propertyDetails.setName(TestDataProvider.propertyName);
		propertyDetails.setDescription(TestDataProvider.propertyDescription);
		propertyDetails.setClasses(new HashSet<>(Collections.singletonList(TestDataProvider.className1)));
		propertyDetails.setCropOntologyId(TestDataProvider.cropOntologyId);
		propertyDetails.setMetadata(TestDataProvider.getTestMetadataDetails());
		return propertyDetails;
	}

	public static ScaleDetails getTestScaleDetails() {
		ScaleDetails scaleDetails = new ScaleDetails();
		scaleDetails.setId(String.valueOf(TestDataProvider.scaleId));
		scaleDetails.setName(TestDataProvider.scaleName);
		scaleDetails.setDescription(TestDataProvider.scaleDescription);
		scaleDetails.setDataType(TestDataProvider.numericalDataType);
		scaleDetails.setMaxValue(TestDataProvider.scaleMinValue);
		scaleDetails.setMaxValue(TestDataProvider.scaleMaxValue);
		scaleDetails.setMetadata(TestDataProvider.getTestMetadataDetails());
		return scaleDetails;
	}

	public static VariableDetails getTestVariableDetails() {
		VariableDetails variableDetails = new VariableDetails();
		variableDetails.setProgramUuid(TestDataProvider.programUUID);
		variableDetails.setId(String.valueOf(TestDataProvider.variableId));
		variableDetails.setName(TestDataProvider.variableName);
		variableDetails.setDescription(TestDataProvider.variableDescription);
		variableDetails.setAlias(TestDataProvider.variableAlias);
		variableDetails.setVariableTypes(new HashSet<>(Collections.singletonList(traitVariable)));
		variableDetails.setProperty(TestDataProvider.getTestPropertyDetails());
		variableDetails.setMethod(TestDataProvider.getTestMethodDetails());
		variableDetails.setScale(TestDataProvider.getTestScaleDetails());
		variableDetails.setExpectedMin(TestDataProvider.variableExpectedMin);
		variableDetails.setExpectedMax(TestDataProvider.variableExpectedMax);
		variableDetails.setFavourite(TestDataProvider.variableIsFavourite);
		return variableDetails;
	}

	public static VariableFilter getVariableFilterForVariableValidator(){
		VariableFilter variableFilter = new VariableFilter();
		variableFilter.addMethodId(methodId);
		variableFilter.addPropertyId(propertyId);
		variableFilter.addScaleId(scaleId);
		return variableFilter;
	}

	public static List<org.ibp.api.domain.ontology.VariableType> getVariableTypes(){
		List<org.ibp.api.domain.ontology.VariableType> variableTypes = new ArrayList<>();

		org.ibp.api.domain.ontology.VariableType variableType = new org.ibp.api.domain.ontology.VariableType("1", "Variable Type 1", "Variable Type Description 1");
		variableTypes.add(variableType);

		variableType = new org.ibp.api.domain.ontology.VariableType("2", "Variable Type 2", "Variable Type Description 2");
		variableTypes.add(variableType);
		return variableTypes;
	}
}

