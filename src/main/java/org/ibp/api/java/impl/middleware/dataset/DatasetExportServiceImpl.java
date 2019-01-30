package org.ibp.api.java.impl.middleware.dataset;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.generationcp.commons.util.FileUtils;
import org.generationcp.commons.util.ZipUtil;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.dataset.DatasetCollectionOrderService;
import org.ibp.api.java.dataset.DatasetExportService;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import com.google.common.io.Files;

import au.com.bytecode.opencsv.CSVWriter;

@Service
@Transactional
public class DatasetExportServiceImpl implements DatasetExportService {

	@Autowired
	private StudyValidator studyValidator;

	@Autowired
	private DatasetValidator datasetValidator;

	@Autowired
	private InstanceValidator instanceValidator;

	@Autowired
	private DatasetService studyDatasetService;

	@Autowired
	private DatasetCollectionOrderService datasetCollectionOrderService;

	@Resource
	private StudyDataManager studyDataManager;

	@Resource
	private DatasetCSVGenerator datasetCSVGenerator;

	@Resource
	private org.generationcp.middleware.service.api.dataset.DatasetService datasetService;

	private ZipUtil zipUtil = new ZipUtil();
	
	@Override
	public File exportAsCSV(final int studyId, final int datasetId, final Set<Integer> instanceIds, final int collectionOrderId, final boolean isExportInSingleFile) {
		this.studyValidator.validate(studyId, false);
		this.datasetValidator.validateDataset(studyId, datasetId, false);
		this.instanceValidator.validate(datasetId, instanceIds);

		final Study study = this.studyDataManager.getStudy(studyId);
		final DatasetDTO dataSet = this.datasetService.getDataset(datasetId);
		final Map<Integer, StudyInstance>  selectedDatasetInstancesMap = getSelectedDatasetInstancesMap(dataSet.getInstances(),
				instanceIds);

		// Get all variables for the dataset
		final List<MeasurementVariable> columns = this.reorderColumns(this.studyDatasetService.getAllDatasetVariables(study.getId(), dataSet.getDatasetId()));
		final int trialDatasetId = this.studyDataManager.getDataSetsByType(study.getId(), DataSetType.SUMMARY_DATA).get(0).getId();
		final DatasetCollectionOrderServiceImpl.CollectionOrder collectionOrder = DatasetCollectionOrderServiceImpl.CollectionOrder.findById(collectionOrderId);

		final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap =
			this.studyDatasetService.getInstanceObservationUnitRowsMap(study.getId(), dataSet.getDatasetId(), new ArrayList<>(
				instanceIds));
		this.datasetCollectionOrderService.reorder(collectionOrder, trialDatasetId, selectedDatasetInstancesMap, observationUnitRowMap);

		try {
			if(isExportInSingleFile) {
				return this.generateCSVFileInSingleFile(study, observationUnitRowMap, columns);
			} else  {				
				return this.generateCSVFiles(study, dataSet, selectedDatasetInstancesMap, observationUnitRowMap, columns);
			}
		} catch (final IOException e) {
			final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
			errors.reject("cannot.exportAsCSV.dataset", "");
			throw new ResourceNotFoundException(errors.getAllErrors().get(0));
		}
	}

	protected File generateCSVFiles(final Study study, final DatasetDTO dataSetDto,
			final Map<Integer, StudyInstance> selectedDatasetInstancesMap,
			final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap, final List<MeasurementVariable> columns)
			throws IOException {
		final List<File> csvFiles = new ArrayList<>();
		final File temporaryFolder = Files.createTempDir();
		for(final Integer instanceDBID: observationUnitRowMap.keySet()) {
			// Build the filename with the following format:
			// study_name + TRIAL_INSTANCE number + location_abbr +  dataset_type + dataset_name
			final String sanitizedFileName = FileUtils.sanitizeFileName(String
				.format(
					"%s_%s_%s_%s_%s.csv", study.getName(), selectedDatasetInstancesMap.get(instanceDBID).getInstanceNumber(), selectedDatasetInstancesMap.get(instanceDBID).getLocationAbbreviation(),
					DataSetType.findById(dataSetDto.getDatasetTypeId()).name(), dataSetDto.getName()));

			final String fileNameFullPath = temporaryFolder.getAbsolutePath() + File.separator + sanitizedFileName;
			final CSVWriter csvWriter =
				new CSVWriter(new OutputStreamWriter(new FileOutputStream(fileNameFullPath), StandardCharsets.UTF_8), ',');
			final File csvFile = this.datasetCSVGenerator.generateCSVFileWithHeaders(columns, fileNameFullPath, csvWriter);
			this.datasetCSVGenerator.writeInstanceObservationUnitRowsToCSVFile(columns, observationUnitRowMap.get(instanceDBID), csvWriter);
			csvFiles.add(csvFile);
			csvWriter.close();
		}

		if (csvFiles.size() == 1) {
			return csvFiles.get(0);
		} else {
			return this.zipUtil.zipFiles(study.getName(), csvFiles);
		}

	}

	protected File generateCSVFileInSingleFile(final Study study,
			final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap, final List<MeasurementVariable> columns)
			throws IOException {
		
		final File temporaryFolder = Files.createTempDir();
		final String sanitizedFileName = FileUtils.sanitizeFileName(String.format("%s_AllInstances.csv", study.getName()));
		final String fileNameFullPath = temporaryFolder.getAbsolutePath() + File.separator + sanitizedFileName;

		final CSVWriter csvWriter =
			new CSVWriter(new OutputStreamWriter(new FileOutputStream(fileNameFullPath), StandardCharsets.UTF_8), ',');
		final File csvFile = this.datasetCSVGenerator.generateCSVFileWithHeaders(columns, fileNameFullPath, csvWriter);
		for(final Integer instanceDBID: observationUnitRowMap.keySet()) {
			final List<ObservationUnitRow> observationUnitRows = observationUnitRowMap.get(instanceDBID);
			this.datasetCSVGenerator.writeInstanceObservationUnitRowsToCSVFile(columns, observationUnitRows, csvWriter);
		}
		csvWriter.close();
		return csvFile;

	}

	protected Map<Integer, StudyInstance> getSelectedDatasetInstancesMap(final List<StudyInstance> studyInstances, final Set<Integer> instanceIds) {
		Map<Integer, StudyInstance> studyInstanceMap = new HashMap<>();
		for(StudyInstance studyInstance: studyInstances) {
			if (instanceIds.contains(studyInstance.getInstanceDbId())) {
				studyInstanceMap.put(studyInstance.getInstanceDbId(), studyInstance);
			}
		}
		return studyInstanceMap;
	}

	protected void setZipUtil(final ZipUtil zipUtil) {
		this.zipUtil = zipUtil;
	}

	protected List<MeasurementVariable> reorderColumns(List<MeasurementVariable> columns) {
		int trialInstanceIndex = 0;
		for(MeasurementVariable column: columns) {
			if(TermId.TRIAL_INSTANCE_FACTOR.getId() == column.getTermId()) {
				final MeasurementVariable trialInstanceMeasurementVariable = columns.remove(trialInstanceIndex);
				columns.add(0, trialInstanceMeasurementVariable);
				break;
			}
			trialInstanceIndex++;
		}
		return columns;

	}
}
