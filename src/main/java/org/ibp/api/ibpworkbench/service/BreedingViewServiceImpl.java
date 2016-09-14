/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.ibp.api.ibpworkbench.service;

import com.rits.cloning.Cloner;
import org.generationcp.middleware.domain.dms.ExperimentValues;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.ibp.api.ibpworkbench.constants.WebAPIConstants;
import org.ibp.api.ibpworkbench.exceptions.IBPWebServiceException;
import org.ibp.api.ibpworkbench.util.ObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BreedingViewServiceImpl implements BreedingViewService {

	@Autowired
	private BreedingViewImportService importService;
	
	@Autowired
	private WorkbenchDataManager workbenchDataManager;
	
	@Autowired
	private Cloner cloner;

	private Map<String, String> nameToAliasMapping;

	private VariableTypeList variableTypeListSummaryStats;

	private List<ExperimentValues> experimentValuesList;
	private List<ExperimentValues> summaryStatsExperimentValuesList;

	private static final Logger LOG = LoggerFactory.getLogger(BreedingViewServiceImpl.class);

	@Override
	@Transactional
	public void execute(Map<String, String> params, List<String> errors) throws IBPWebServiceException {

		try {

			String mainOutputFilePath = params.get(WebAPIConstants.MAIN_OUTPUT_FILE_PATH.getParamValue());
			String summaryOutputFilePath = params.get(WebAPIConstants.SUMMARY_OUTPUT_FILE_PATH.getParamValue());
			String outlierOutputFilePath = params.get(WebAPIConstants.OUTLIER_OUTPUT_FILE_PATH.getParamValue());
			int studyId = Integer.valueOf(params.get(WebAPIConstants.STUDY_ID.getParamValue()));

			this.nameToAliasMapping = this.getNameToAliasMapping();

			this.importService.importMeansData(new File(mainOutputFilePath), studyId, this.nameToAliasMapping);

			if (outlierOutputFilePath != null && !outlierOutputFilePath.equals("")) {
				this.importService.importOutlierData(new File(outlierOutputFilePath), studyId, this.nameToAliasMapping);
			}

			if (summaryOutputFilePath != null && !summaryOutputFilePath.equals("")) {
				this.importService.importSummaryStatsData(new File(summaryOutputFilePath), studyId, this.nameToAliasMapping);
			}

		} catch (Exception e) {
			BreedingViewServiceImpl.LOG.error("ERROR:", e);
			throw new IBPWebServiceException(e.getMessage());
		}

	}

	protected Map<String, String> getNameToAliasMapping() throws MiddlewareQueryException {

		Map<String, String> map = new HashMap<String, String>();

		String fileName =
				String.format("%s\\Temp\\%s", this.workbenchDataManager.getWorkbenchSetting().getInstallationDirectory(), "mapping.ser");

		map = new ObjectUtil<HashMap<String, String>>().deserializeFromFile(fileName);

		return map;
	}

	protected void setWorkbenchDataManager(WorkbenchDataManager workbenchDataManager) {
		this.workbenchDataManager = workbenchDataManager;
	}

	protected void setNameToAliasMapping(Map<String, String> mapping) {
		this.nameToAliasMapping = mapping;
	}

	protected Cloner getCloner() {
		return this.cloner;
	}

	protected void setCloner(Cloner cloner) {
		this.cloner = cloner;
	}

	protected VariableTypeList getMeansVariableTypeList() {
		return new VariableTypeList();
	}

	protected List<ExperimentValues> getExperimentValuesList() {
		return this.experimentValuesList;
	}

	protected List<ExperimentValues> getSummaryStatsExperimentValuesList() {
		return this.summaryStatsExperimentValuesList;
	}

	protected VariableTypeList getVariableTypeListSummaryStats() {
		return this.variableTypeListSummaryStats;
	}

}