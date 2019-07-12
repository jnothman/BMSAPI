package org.ibp.api.java.impl.middleware.dataset;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import org.generationcp.middleware.domain.dataset.ObservationDto;
import org.generationcp.middleware.domain.dms.DatasetTypeDTO;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.operation.transformer.etl.MeasurementVariableTransformer;
import org.generationcp.middleware.service.api.dataset.DatasetTypeService;
import org.generationcp.middleware.service.api.dataset.FilteredPhenotypesInstancesCountDTO;
import org.generationcp.middleware.service.api.dataset.ObservationUnitsParamDTO;
import org.generationcp.middleware.service.api.dataset.ObservationUnitsSearchDTO;
import org.generationcp.middleware.service.api.study.MeasurementVariableDto;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ConflictException;
import org.ibp.api.exception.NotSupportedException;
import org.ibp.api.exception.PreconditionFailedException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetGeneratorInputValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.ObservationValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.ObservationsTableValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.rest.dataset.DatasetDTO;
import org.ibp.api.rest.dataset.DatasetGeneratorInput;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.dataset.ObservationsPutRequestInput;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Service
@Transactional
public class DatasetServiceImpl implements DatasetService {

	@Autowired
	private org.generationcp.middleware.service.api.dataset.DatasetService middlewareDatasetService;

	@Autowired
	private StudyValidator studyValidator;

	@Autowired
	private DatasetValidator datasetValidator;

	@Autowired
	private ObservationValidator observationValidator;

	@Autowired
	private InstanceValidator instanceValidator;

	@Autowired
	private MeasurementVariableTransformer measurementVariableTransformer;

	@Autowired
	private DatasetGeneratorInputValidator datasetGeneratorInputValidator;

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private ObservationsTableValidator observationsTableValidator;

	@Autowired
	private DatasetTypeService datasetTypeService;

	private static String PLOT_DATASET_NAME = "Observations";

	@Override
	public List<MeasurementVariable> getObservationSetColumns(
		final Integer studyId, final Integer datasetId, final Boolean draftMode) {

		this.studyValidator.validate(studyId, false);

		// TODO generalize to any obs dataset (plot/subobs), make 3rd param false
		this.datasetValidator.validateDataset(studyId, datasetId, false);

		return this.middlewareDatasetService.getObservationSetColumns(datasetId, draftMode);
	}

	@Override
	public List<MeasurementVariable> getSubObservationSetVariables(
		final Integer studyId, final Integer datasetId) {

		this.studyValidator.validate(studyId, false);

		// TODO generalize to any obs dataset (plot/subobs), make 3rd param false
		this.datasetValidator.validateDataset(studyId, datasetId, false);

		return this.middlewareDatasetService.getObservationSetVariables(datasetId);
	}

	@Override
	public long countObservationsByVariables(final Integer studyId, final Integer datasetId, final List<Integer> variableIds) {
		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId, false);

		return this.middlewareDatasetService.countObservationsByVariables(datasetId, variableIds);
	}

	@Override
	public long countObservationsByInstance(final Integer studyId, final Integer datasetId, final Integer instanceId) {
		this.validateStudyDatasetAndInstances(studyId, datasetId, Arrays.asList(instanceId), false);
		return this.middlewareDatasetService.countObservationsByInstance(datasetId, instanceId);
	}

	@Override
	public MeasurementVariable addDatasetVariable(final Integer studyId, final Integer datasetId, final DatasetVariable datasetVariable) {
		this.studyValidator.validate(studyId, true);
		final Integer variableId = datasetVariable.getVariableId();
		final StandardVariable traitVariable =
			this.datasetValidator.validateDatasetVariable(studyId, datasetId, false, datasetVariable, false);

		final String alias = datasetVariable.getStudyAlias() != null ? datasetVariable.getStudyAlias() : traitVariable.getName();
		final VariableType type = VariableType.getById(datasetVariable.getVariableTypeId());
		this.middlewareDatasetService.addDatasetVariable(datasetId, variableId, type, alias);
		final MeasurementVariable measurementVariable = this.measurementVariableTransformer.transform(traitVariable, false);
		measurementVariable.setName(alias);
		measurementVariable.setVariableType(type);
		measurementVariable.setRequired(false);
		return measurementVariable;

	}

	@Override
	public List<MeasurementVariableDto> getDatasetVariablesByType(
		final Integer studyId, final Integer datasetId, final VariableType variableType) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId, false);
		return this.middlewareDatasetService.getDatasetVariablesByType(datasetId, variableType);
	}

	@Override
	public void removeDatasetVariables(final Integer studyId, final Integer datasetId, final List<Integer> variableIds) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateExistingDatasetVariables(studyId, datasetId, false, variableIds);
		this.middlewareDatasetService.removeDatasetVariables(datasetId, variableIds);
	}

	@Override
	public ObservationDto createObservation(
		final Integer studyId, final Integer datasetId, final Integer observationUnitId, final ObservationDto observation) {

		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateExistingDatasetVariables(studyId, datasetId, false, Arrays.asList(observation.getVariableId()));
		this.observationValidator.validateObservationUnit(datasetId, observationUnitId);
		this.observationValidator.validateObservationValue(studyId, observation.getVariableId(), observation.getValue());
		return this.middlewareDatasetService.createObservation(observation);

	}

	@Override
	public ObservationDto updateObservation(
		final Integer studyId, final Integer datasetId, final Integer observationId, final Integer observationUnitId,
		final ObservationDto observationDto) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId, false);
		this.observationValidator.validateObservation(studyId, datasetId, observationUnitId, observationId, observationDto);
		observationDto.setObservationUnitId(observationUnitId);
		observationDto.setObservationId(observationId);
		return this.middlewareDatasetService.updatePhenotype(observationId, observationDto);

	}

	@Override
	public List<DatasetDTO> getDatasets(final Integer studyId, final Set<Integer> datasetTypeIds) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		final Set<Integer> datasetTypeIdList = new TreeSet<>();
		final Study study = this.studyDataManager.getStudy(studyId);

		if (study == null) {
			errors.reject("study.not.exist", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}

		final Map<Integer, DatasetTypeDTO> datasetTypeMap = this.datasetTypeService.getAllDatasetTypesMap();
		if (datasetTypeIds != null) {
			for (final Integer dataSetTypeId : datasetTypeIds) {
				if (!datasetTypeMap.containsKey(dataSetTypeId)) {
					errors.reject("dataset.type.id.not.exist", new Object[] {dataSetTypeId}, "");
					throw new ResourceNotFoundException(errors.getAllErrors().get(0));
				} else {
					datasetTypeIdList.add(dataSetTypeId);
				}
			}
		}

		final List<org.generationcp.middleware.domain.dms.DatasetDTO> datasetDTOS =
			this.middlewareDatasetService.getDatasets(studyId, datasetTypeIdList);

		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		final List<DatasetDTO> datasetDTOs = new ArrayList<>();
		for (final org.generationcp.middleware.domain.dms.DatasetDTO datasetDTO : datasetDTOS) {
			final DatasetDTO datasetDto = mapper.map(datasetDTO, DatasetDTO.class);
			if (datasetDto.getDatasetTypeId().equals(DatasetTypeEnum.PLOT_DATA.getId())) {
				datasetDto.setName(PLOT_DATASET_NAME);
			}
			datasetDTOs.add(datasetDto);
		}
		return datasetDTOs;
	}

	@Override
	public DatasetDTO getDataset(final String crop, final Integer studyId, final Integer datasetId) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		final Study study = this.studyDataManager.getStudy(studyId);

		if (study == null) {
			errors.reject("study.not.exist", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}

		this.datasetValidator.validateDataset(studyId, datasetId, false);
		final org.generationcp.middleware.domain.dms.DatasetDTO datasetDTO = this.middlewareDatasetService.getDataset(datasetId);

		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		final DatasetDTO datasetDto = mapper.map(datasetDTO, DatasetDTO.class);
		if (datasetDto.getDatasetTypeId().equals(DatasetTypeEnum.PLOT_DATA.getId())) {
			datasetDto.setName(PLOT_DATASET_NAME);
		}
		datasetDto.setInstances(this.convertToStudyInstances(mapper, datasetDTO.getInstances()));
		datasetDto.setStudyId(studyId);
		datasetDto.setCropName(crop);

		return datasetDto;
	}

	@Override
	public Integer countAllObservationUnitsForDataset(
		final Integer datasetId, final Integer instanceId, final Boolean draftMode) {
		return this.middlewareDatasetService.countAllObservationUnitsForDataset(datasetId, instanceId, draftMode);
	}

	@Override
	public long countFilteredObservationUnitsForDataset(
		final Integer datasetId, final Integer instanceId, final Boolean draftMode,
		final ObservationUnitsSearchDTO.Filter filter) {
		return this.middlewareDatasetService.countFilteredObservationUnitsForDataset(datasetId, instanceId, draftMode, filter);
	}

	@Override
	public List<StudyInstance> getDatasetInstances(final Integer studyId, final Integer datasetId) {
		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId, false);
		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		return this.convertToStudyInstances(mapper, this.middlewareDatasetService.getDatasetInstances(datasetId));
	}

	@Override
	public Map<Integer, List<ObservationUnitRow>> getInstanceObservationUnitRowsMap(
		final int studyId, final int datasetId, final List<Integer> instanceIds) {
		this.validateStudyDatasetAndInstances(studyId, datasetId, instanceIds, false);
		final Map<Integer, List<org.generationcp.middleware.service.api.dataset.ObservationUnitRow>> observationUnitRowsMap =
			this.middlewareDatasetService.getInstanceIdToObservationUnitRowsMap(studyId, datasetId, instanceIds);
		final ModelMapper observationUnitRowMapper = new ModelMapper();
		observationUnitRowMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		final Map<Integer, List<ObservationUnitRow>> map = new HashMap<>();
		for (final Integer instanceNumber : observationUnitRowsMap.keySet()) {
			final List<org.generationcp.middleware.service.api.dataset.ObservationUnitRow> observationUnitRows =
				observationUnitRowsMap.get(instanceNumber);
			final List<ObservationUnitRow> list = new ArrayList<>();
			this.mapObservationUnitRows(observationUnitRowMapper, observationUnitRows, list);
			map.put(instanceNumber, list);
		}
		return map;
	}

	void validateStudyDatasetAndInstances(
		final int studyId, final int datasetId, final List<Integer> instanceIds, final boolean shouldBeSubObservation) {
		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId, shouldBeSubObservation);
		if (instanceIds != null) {
			this.instanceValidator.validate(datasetId, new HashSet<>(instanceIds));
		}
	}

	@Override
	public List<ObservationUnitRow> getObservationUnitRows(
		final int studyId, final int datasetId, final ObservationUnitsSearchDTO searchDTO) {

		List<Integer> instanceIds = null;
		if (searchDTO.getInstanceId() != null) {
			instanceIds = Arrays.asList(searchDTO.getInstanceId());
		}
		this.validateStudyDatasetAndInstances(studyId, datasetId, instanceIds, false);

		final List<org.generationcp.middleware.service.api.dataset.ObservationUnitRow> observationUnitRows =
			this.middlewareDatasetService.getObservationUnitRows(studyId, datasetId, searchDTO);

		final ModelMapper observationUnitRowMapper = new ModelMapper();
		observationUnitRowMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		final List<ObservationUnitRow> list = new ArrayList<>();
		this.mapObservationUnitRows(observationUnitRowMapper, observationUnitRows, list);

		return list;
	}

	@Override
	public DatasetDTO generateSubObservationDataset(
		final String cropName, final Integer studyId, final Integer parentId, final DatasetGeneratorInput datasetGeneratorInput) {

		// checks that study exists and it is not locked
		this.studyValidator.validate(studyId, true);

		// checks input matches validation rules
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), DatasetGeneratorInput.class.getName());

		this.datasetValidator.validateDatasetBelongsToStudy(studyId, parentId);
		this.instanceValidator.validate(null, Sets.newHashSet(datasetGeneratorInput.getInstanceIds()));
		this.datasetGeneratorInputValidator.validateBasicData(cropName, studyId, parentId, datasetGeneratorInput, bindingResult);

		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		// not implemented yet
		this.datasetGeneratorInputValidator.validateDatasetTypeIsImplemented(datasetGeneratorInput.getDatasetTypeId(), bindingResult);
		if (bindingResult.hasErrors()) {
			throw new NotSupportedException(bindingResult.getAllErrors().get(0));
		}

		// conflict
		this.datasetGeneratorInputValidator.validateDataConflicts(studyId, datasetGeneratorInput, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ConflictException(bindingResult.getAllErrors());
		}

		final org.generationcp.middleware.domain.dms.DatasetDTO datasetDTO = this.middlewareDatasetService
			.generateSubObservationDataset(studyId, datasetGeneratorInput.getDatasetName(), datasetGeneratorInput.getDatasetTypeId(),
				Arrays.asList(datasetGeneratorInput.getInstanceIds()), datasetGeneratorInput.getSequenceVariableId(),
				datasetGeneratorInput.getNumberOfSubObservationUnits(), parentId);
		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		return mapper.map(datasetDTO, DatasetDTO.class);
	}

	@Override
	public void deleteObservation(
		final Integer studyId, final Integer datasetId, final Integer observationUnitId, final Integer observationId) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId, false);
		this.observationValidator.validateObservation(studyId, datasetId, observationUnitId, observationId, null);
		this.middlewareDatasetService.deletePhenotype(observationId);

	}

	private List<StudyInstance> convertToStudyInstances(
		final ModelMapper mapper, final List<org.generationcp.middleware.service.impl.study.StudyInstance> middlewareStudyInstances) {

		final List<StudyInstance> instances = new ArrayList<>();
		for (final org.generationcp.middleware.service.impl.study.StudyInstance instance : middlewareStudyInstances) {
			final StudyInstance datasetInstance = mapper.map(instance, StudyInstance.class);
			instances.add(datasetInstance);
		}
		return instances;
	}

	@Override
	public void importObservations(final Integer studyId, final Integer datasetId, final ObservationsPutRequestInput input) {

		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), ObservationsPutRequestInput.class.getName());

		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId, false);

		this.observationsTableValidator.validateList(input.getData());

		final List<MeasurementVariable> datasetMeasurementVariables =
			this.middlewareDatasetService.getDatasetMeasurementVariables(datasetId);

		if (datasetMeasurementVariables.isEmpty()) {
			errors.reject("no.variables.dataset", null, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final ObservationUnitsTableBuilder observationUnitsTableBuilder = new ObservationUnitsTableBuilder();
		final Table<String, String, String> table = observationUnitsTableBuilder.build(input.getData(), datasetMeasurementVariables);

		// Get Map<OBS_UNIT_ID, Observations>
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitRow> storedData = this.middlewareDatasetService
			.getObservationUnitsAsMap(datasetId, datasetMeasurementVariables, new ArrayList<>(table.rowKeySet()));

		if (storedData.isEmpty()) {
			errors.reject("none.obs.unit.id.matches", null, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		final int rowsNotBelongingToDataset = table.rowKeySet().size() - storedData.size();

		// remove elements that does not belong to the dataset
		if (rowsNotBelongingToDataset != 0) {
			final List<String> obsUnitIdsList = new ArrayList<>(table.rowKeySet());
			obsUnitIdsList.removeAll(storedData.keySet());
			for (final String obsUnitId : obsUnitIdsList) {
				table.row(obsUnitId).clear();
			}
		}

		// Check for data issues
		this.observationsTableValidator.validateObservationsValuesDataTypes(table, datasetMeasurementVariables);

		// Processing warnings
		if (input.isProcessWarnings()) {
			errors = this.processObservationsDataWarningsAsErrors(table, storedData, rowsNotBelongingToDataset,
				observationUnitsTableBuilder.getDuplicatedFoundNumber(), input.isDraftMode());
		}
		if (!errors.hasErrors()) {
			this.middlewareDatasetService.importDataset(datasetId, table, input.isDraftMode());
		} else {
			throw new PreconditionFailedException(errors.getAllErrors());
		}

	}

	@Override
	public List<MeasurementVariable> getMeasurementVariables(final Integer projectId, final List<Integer> variableTypes) {
		return this.middlewareDatasetService.getObservationSetVariables(projectId, variableTypes);
	}

	@Override
	public Boolean hasDatasetDraftDataOutOfBounds(final Integer studyId, final Integer datasetId) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId, false);
		return this.middlewareDatasetService.hasDatasetDraftDataOutOfBounds(datasetId);
	}

	@Override
	public void acceptDraftDataAndSetOutOfBoundsToMissing(final Integer studyId, final Integer datasetId) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId, false);
		this.middlewareDatasetService.acceptDraftDataAndSetOutOfBoundsToMissing(studyId, datasetId);
	}

	@Override
	public void acceptDraftDataFilteredByVariable(
		final Integer studyId, final Integer datasetId,
		final ObservationUnitsSearchDTO searchDTO) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId, false);
		this.datasetValidator
			.validateExistingDatasetVariables(studyId, datasetId, false, Lists.newArrayList(searchDTO.getFilter().getVariableId()));
		this.middlewareDatasetService.acceptDraftDataFilteredByVariable(datasetId, searchDTO, studyId);
	}

	@Override
	public void setValueToVariable(
		final Integer studyId, final Integer datasetId, final ObservationUnitsParamDTO paramDTO) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId, false);
		final Integer variableId = paramDTO.getObservationUnitsSearchDTO().getFilter().getVariableId();
		this.datasetValidator
			.validateExistingDatasetVariables(
				studyId, datasetId, false, Lists.newArrayList(variableId));
		this.observationValidator.validateObservationValue(studyId, variableId, paramDTO.getNewValue());
		this.middlewareDatasetService.setValueToVariable(datasetId, paramDTO, studyId);
	}

	@Override
	public void acceptAllDatasetDraftData(final Integer studyId, final Integer datasetId) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId, false);
		this.middlewareDatasetService.acceptAllDatasetDraftData(studyId, datasetId);
	}

	@Override
	public void rejectDatasetDraftData(final Integer studyId, final Integer datasetId) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId, false);
		this.middlewareDatasetService.rejectDatasetDraftData(datasetId);
	}

	@Override
	public FilteredPhenotypesInstancesCountDTO countFilteredInstancesAndPhenotypes(
		final Integer studyId,
		final Integer datasetId, final ObservationUnitsSearchDTO observationUnitsSearchDTO) {
		this.studyValidator.validate(studyId, true);
		this.datasetValidator.validateDataset(studyId, datasetId, false);
		this.datasetValidator.validateExistingDatasetVariables(studyId, datasetId, false,
			Lists.newArrayList(observationUnitsSearchDTO.getFilter().getVariableId()));
		this.datasetValidator.validateDataset(studyId, datasetId, false);
		this.datasetValidator.validateExistingDatasetVariables(studyId, datasetId, false, Lists.newArrayList(observationUnitsSearchDTO.getFilter().getVariableId()));
		return this.middlewareDatasetService.countFilteredInstancesAndPhenotypes(datasetId, observationUnitsSearchDTO);
	}

	private BindingResult processObservationsDataWarningsAsErrors(
		final Table<String, String, String> table,
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitRow> storedData,
		final Integer rowsNotBelongingToDataset, final Integer duplicatedFoundNumber, final Boolean draftMode) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), ObservationsPutRequestInput.class.getName());
		if (duplicatedFoundNumber > 0) {
			errors.reject("duplicated.obs.unit.id", null, "");
		}

		if (rowsNotBelongingToDataset != 0) {
			errors.reject("some.obs.unit.id.matches", new String[] {String.valueOf(rowsNotBelongingToDataset)}, "");
		}

		if (this.isInputOverwritingData(table, storedData, draftMode)) {
			errors.reject("warning.import.overwrite.data", null, "");
		}

		return errors;
	}

	private Boolean isInputOverwritingData(
		final Table<String, String, String> table,
		final Map<String, org.generationcp.middleware.service.api.dataset.ObservationUnitRow> storedData, final Boolean draftMode) {
		boolean overwritingData = false;

		externalLoop:
		for (final String observationUnitId : table.rowKeySet()) {

			final org.generationcp.middleware.service.api.dataset.ObservationUnitRow storedObservations = storedData.get(observationUnitId);

			for (final String variableName : table.columnKeySet()) {

				final org.generationcp.middleware.service.api.dataset.ObservationUnitData observation =
					storedObservations.getVariables().get(variableName);

				if (observation == null) {
					continue;
				}

				if ((!draftMode && observation.getValue() != null //
					&& !observation.getValue().equalsIgnoreCase(table.get(observationUnitId, variableName))) //
					|| (draftMode && observation.getDraftValue() != null //
					&& !observation.getDraftValue().equalsIgnoreCase(table.get(observationUnitId, variableName)))) {

					overwritingData = true;

					break externalLoop;
				}
			}

		}
		return overwritingData;
	}

	private void mapObservationUnitRows(
		final ModelMapper observationUnitRowMapper,
		final List<org.generationcp.middleware.service.api.dataset.ObservationUnitRow> observationUnitRows,
		final List<ObservationUnitRow> list) {
		for (final org.generationcp.middleware.service.api.dataset.ObservationUnitRow dto : observationUnitRows) {
			final Map<String, ObservationUnitData> datas = new HashMap<>();
			for (final String data : dto.getVariables().keySet()) {
				datas.put(data, observationUnitRowMapper.map(dto.getVariables().get(data), ObservationUnitData.class));
			}
			final ObservationUnitRow observationUnitRow = observationUnitRowMapper.map(dto, ObservationUnitRow.class);
			observationUnitRow.setVariables(datas);
			list.add(observationUnitRow);
		}
	}
}
