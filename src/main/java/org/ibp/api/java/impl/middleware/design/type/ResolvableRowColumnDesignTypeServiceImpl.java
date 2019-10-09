package org.ibp.api.java.impl.middleware.design.type;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.exception.BVDesignException;
import org.ibp.api.java.design.type.ExperimentDesignTypeService;
import org.ibp.api.java.impl.middleware.design.generator.ExperimentDesignGenerator;
import org.ibp.api.java.impl.middleware.design.validator.ExperimentDesignTypeValidator;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentDesignInput;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ResolvableRowColumnDesignTypeServiceImpl implements ExperimentDesignTypeService {

	private static final List<Integer> DESIGN_FACTOR_VARIABLES =
		Arrays.asList(TermId.REP_NO.getId(), TermId.PLOT_NO.getId(), TermId.ENTRY_NO.getId(), TermId.ROW.getId(), TermId.COL.getId());

	private final List<Integer> EXPERIMENT_DESIGN_VARIABLES_LATINIZED = Arrays
		.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId(), TermId.NO_OF_ROWS_IN_REPS.getId(),
			TermId.NO_OF_COLS_IN_REPS.getId(), TermId.NO_OF_CROWS_LATINIZE.getId(), TermId.NO_OF_CCOLS_LATINIZE.getId(),
			TermId.REPLICATIONS_MAP.getId(), TermId.NO_OF_REPS_IN_COLS.getId());

	private final List<Integer> EXPERIMENT_DESIGN_VARIABLES = Arrays
		.asList(TermId.EXPERIMENT_DESIGN_FACTOR.getId(), TermId.NUMBER_OF_REPLICATES.getId(), TermId.NO_OF_ROWS_IN_REPS.getId(),
			TermId.NO_OF_COLS_IN_REPS.getId());

	@Resource
	private ExperimentDesignTypeValidator experimentDesignTypeValidator;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private ExperimentDesignGenerator experimentDesignGenerator;

	@Override
	public List<ObservationUnitRow> generateDesign(final int studyId, final ExperimentDesignInput experimentDesignInput,
		final String programUUID, final List<ImportedGermplasm> germplasmList) throws BVDesignException {

		this.experimentDesignTypeValidator.validateResolvableIncompleteBlockDesign(experimentDesignInput, germplasmList);

		final int nTreatments = germplasmList.size();
		final String rows = experimentDesignInput.getRowsPerReplications();
		final String cols = experimentDesignInput.getColsPerReplications();
		final String replicates = experimentDesignInput.getReplicationsCount();
		final int environments = Integer.parseInt(experimentDesignInput.getNoOfEnvironments());
		final int environmentsToAdd = Integer.parseInt(experimentDesignInput.getNoOfEnvironmentsToAdd());

		final Map<Integer, StandardVariable> standardVariablesMap =
			this.ontologyDataManager.getStandardVariables(DESIGN_FACTOR_VARIABLES, programUUID).stream()
				.collect(Collectors.toMap(StandardVariable::getId, standardVariable -> standardVariable));

		final String entryNumberName = standardVariablesMap.get(TermId.ENTRY_NO.getId()).getName();
		final String replicateNumberName = standardVariablesMap.get(TermId.REP_NO.getId()).getName();
		final String plotNumberName = standardVariablesMap.get(TermId.PLOT_NO.getId()).getName();
		final String rowName = standardVariablesMap.get(TermId.ROW.getId()).getName();
		final String colName = standardVariablesMap.get(TermId.COL.getId()).getName();

		if (experimentDesignInput.getUseLatenized() != null && experimentDesignInput.getUseLatenized().booleanValue()) {
			if (experimentDesignInput.getReplicationsArrangement() != null) {
				if (experimentDesignInput.getReplicationsArrangement().intValue() == 1) {
					// column
					experimentDesignInput.setReplatinGroups(experimentDesignInput.getReplicationsCount());
				} else if (experimentDesignInput.getReplicationsArrangement().intValue() == 2) {
					// rows
					final StringBuilder rowReplatingGroupStringBuilder = new StringBuilder();
					for (int i = 0; i < Integer.parseInt(experimentDesignInput.getReplicationsCount()); i++) {
						if (StringUtils.isEmpty(rowReplatingGroupStringBuilder.toString())) {
							rowReplatingGroupStringBuilder.append(",");
						}
						rowReplatingGroupStringBuilder.append("1");
					}
					experimentDesignInput.setReplatinGroups(rowReplatingGroupStringBuilder.toString());
				}
			}
		}

		final Integer plotNo = StringUtil.parseInt(experimentDesignInput.getStartingPlotNo(), null);
		final Integer entryNo = StringUtil.parseInt(experimentDesignInput.getStartingEntryNo(), null);

		final MainDesign mainDesign = this.experimentDesignGenerator
			.createResolvableRowColDesign(Integer.toString(nTreatments), replicates, rows, cols, entryNumberName,
				replicateNumberName, rowName, colName, plotNumberName, plotNo,
				entryNo,
				experimentDesignInput.getNrlatin(), experimentDesignInput.getNclatin(), experimentDesignInput.getReplatinGroups(), "",
				experimentDesignInput.getUseLatenized());

		final List<MeasurementVariable> measurementVariables = new ArrayList<>(this.getMeasurementVariablesMap(studyId, programUUID).values());
		return  this.experimentDesignGenerator
		 .generateExperimentDesignMeasurements(environments, environmentsToAdd, measurementVariables, germplasmList, mainDesign, entryNumberName, null,
		 new HashMap<Integer, Integer>());
	}

	@Override
	public Boolean requiresBreedingViewLicence() {
		return Boolean.TRUE;
	}

	@Override
	public Integer getDesignTypeId() {
		return ExperimentDesignType.ROW_COL.getId();
	}

	@Override
	public Map<Integer, MeasurementVariable> getMeasurementVariablesMap(final int studyId, final String programUUID) {
		return this.experimentDesignGenerator.getMeasurementVariablesMap(studyId, programUUID, DESIGN_FACTOR_VARIABLES, new ArrayList<>());
	}

}
