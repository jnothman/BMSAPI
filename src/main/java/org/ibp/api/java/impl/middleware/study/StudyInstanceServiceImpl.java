package org.ibp.api.java.impl.middleware.study;

import org.fest.util.Collections;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.ibp.api.domain.study.StudyInstance;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.dataset.DatasetService;
import org.ibp.api.java.impl.middleware.dataset.validator.DatasetValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.java.study.StudyInstanceService;
import org.ibp.api.rest.dataset.DatasetDTO;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
public class StudyInstanceServiceImpl implements StudyInstanceService {

	@Resource
	private org.generationcp.middleware.service.api.study.StudyInstanceService studyInstanceMiddlewareService;

	@Resource
	private WorkbenchDataManager workbenchDataManager;

	@Resource
	private DatasetService datasetService;

	@Resource
	private StudyValidator studyValidator;

	@Resource
	private DatasetValidator datasetValidator;

	@Resource
	private InstanceValidator instanceValidator;

	@Override
	public StudyInstance createStudyInstance(final String cropName, final Integer studyId, final String instanceNumber) {

		this.studyValidator.validate(studyId, true);
		this.instanceValidator.checkStudyInstanceAlreadyExists(studyId, instanceNumber);

		final CropType cropType = this.workbenchDataManager.getCropTypeByName(cropName);

		final List<DatasetDTO> datasets = this.datasetService.getDatasets(studyId, Collections.set(DatasetTypeEnum.SUMMARY_DATA.getId()));
		if (!datasets.isEmpty()) {
			// Add Study Instance in Environment (Summary Data) Dataset
			final org.generationcp.middleware.service.impl.study.StudyInstance studyInstance =
				this.studyInstanceMiddlewareService.createStudyInstance(cropType, datasets.get(0).getDatasetId(), instanceNumber);
			final ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
			return mapper.map(studyInstance, StudyInstance.class);
		} else {
			throw new ApiRuntimeException("No Environment Dataset by the supplied studyId [" + studyId + "] was found.");
		}

	}

	@Override
	public void removeStudyInstance(final String cropName, final Integer studyId, final String instanceNumber) {

		this.studyValidator.validate(studyId, true);
		// TODO: add validation to check if the instance number exists before removing.

		final CropType cropType = this.workbenchDataManager.getCropTypeByName(cropName);

		final List<DatasetDTO> datasets = this.datasetService.getDatasets(studyId, Collections.set(DatasetTypeEnum.SUMMARY_DATA.getId()));
		if (!datasets.isEmpty()) {
			// TODO: To be implemented in IBP-3160
			this.studyInstanceMiddlewareService.removeStudyInstance(cropType, datasets.get(0).getDatasetId(), instanceNumber);
		}
	}
}
