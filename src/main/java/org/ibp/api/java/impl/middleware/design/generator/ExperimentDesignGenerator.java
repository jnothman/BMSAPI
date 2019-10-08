package org.ibp.api.java.impl.middleware.design.generator;

import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.util.StringUtil;
import org.ibp.api.domain.design.BVDesignOutput;
import org.ibp.api.domain.design.BVDesignTrialInstance;
import org.ibp.api.domain.design.ExpDesign;
import org.ibp.api.domain.design.ExpDesignParameter;
import org.ibp.api.domain.design.ListItem;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.exception.BVDesignException;
import org.ibp.api.java.impl.middleware.design.runner.BVDesignRunner;
import org.ibp.api.java.impl.middleware.design.transformer.StandardVariableTransformer;
import org.ibp.api.java.impl.middleware.design.util.ExpDesignUtil;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ExperimentDesignGenerator {

	public static final String NCLATIN_PARAM = "nclatin";
	public static final String NRLATIN_PARAM = "nrlatin";
	public static final String REPLATINGROUPS_PARAM = "replatingroups";
	public static final String COLUMNFACTOR_PARAM = "columnfactor";
	public static final String ROWFACTOR_PARAM = "rowfactor";
	public static final String NCOLUMNS_PARAM = "ncolumns";
	public static final String NROWS_PARAM = "nrows";
	public static final String NBLATIN_PARAM = "nblatin";
	public static final String REPLICATEFACTOR_PARAM = "replicatefactor";
	public static final String TREATMENTFACTOR_PARAM = "treatmentfactor";
	public static final String INITIAL_TREATMENT_NUMBER_PARAM = "initialtreatnum";
	public static final String NREPLICATES_PARAM = "nreplicates";
	public static final String NTREATMENTS_PARAM = "ntreatments";
	public static final String BLOCKSIZE_PARAM = "blocksize";
	public static final String TIMELIMIT_PARAM = "timelimit";
	public static final String LEVELS_PARAM = "levels";
	public static final String TREATMENTFACTORS_PARAM = "treatmentfactors";
	public static final String PLOTFACTOR_PARAM = "plotfactor";
	public static final String INITIAL_PLOT_NUMBER_PARAM = "initialplotnum";
	public static final String BLOCKFACTOR_PARAM = "blockfactor";
	public static final String NBLOCKS_PARAM = "nblocks";
	public static final String OUTPUTFILE_PARAM = "outputfile";
	public static final String SEED_PARAM = "seed";
	public static final String NCONTROLS_PARAM = "ncontrols";
	public static final String NUMBER_TRIALS_PARAM = "numbertrials";
	public static final String NREPEATS_PARAM = "nrepeats";

	public static final String RANDOMIZED_COMPLETE_BLOCK_DESIGN = "RandomizedBlock";
	public static final String RESOLVABLE_INCOMPLETE_BLOCK_DESIGN = "ResolvableIncompleteBlock";
	public static final String RESOLVABLE_ROW_COL_DESIGN = "ResolvableRowColumn";
	public static final String AUGMENTED_RANDOMIZED_BLOCK_DESIGN = "Augmented";
	public static final String P_REP_DESIGN = "Prep";

	private static final Logger LOG = LoggerFactory.getLogger(ExperimentDesignGenerator.class);
	private static final List<Integer> EXP_DESIGN_VARIABLE_IDS =
		Arrays.asList(TermId.PLOT_NO.getId(), TermId.REP_NO.getId(), TermId.BLOCK_NO.getId(), TermId.ROW.getId(), TermId.COL.getId());

	@Resource
	private BVDesignRunner bvDesignRunner;

	@Resource
	private StudyDataManager studyDataManager;

	@Resource
	private DatasetService datasetService;

	@Resource
	private OntologyDataManager ontologyDataManager;

	@Resource
	private StandardVariableTransformer standardVariableTransformer;

	private final Random random = new Random();

	public MainDesign createRandomizedCompleteBlockDesign(
		final String nBlock, final String blockFactor, final String plotFactor,
		final Integer initialPlotNumber, final Integer initialEntryNumber, final String entryNoVarName, final List<String> treatmentFactors,
		final List<String> levels, final String outputfile) {

		final String timeLimit = AppConstants.EXP_DESIGN_TIME_LIMIT.getString();

		final List<ExpDesignParameter> paramList = new ArrayList<>();
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.SEED_PARAM, "", null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NBLOCKS_PARAM, nBlock, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.BLOCKFACTOR_PARAM, blockFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.PLOTFACTOR_PARAM, plotFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM,
			this.getPlotNumberStringValueOrDefault(initialPlotNumber), null));

		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM, null,
			this.getInitialTreatNumList(treatmentFactors, initialEntryNumber, entryNoVarName)));

		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.TREATMENTFACTORS_PARAM, null,
			this.convertToListItemList(treatmentFactors)));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.LEVELS_PARAM, null, this.convertToListItemList(levels)));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.TIMELIMIT_PARAM, timeLimit, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.OUTPUTFILE_PARAM, outputfile, null));

		final ExpDesign design = new ExpDesign(ExperimentDesignGenerator.RANDOMIZED_COMPLETE_BLOCK_DESIGN, paramList);

		return new MainDesign(design);
	}

	public MainDesign createResolvableIncompleteBlockDesign(
		final String blockSize, final String nTreatments, final String nReplicates,
		final String treatmentFactor, final String replicateFactor, final String blockFactor, final String plotFactor,
		final Integer initialPlotNumber, final Integer initialEntryNumber, final String nBlatin, final String replatingGroups,
		final String outputfile, final boolean useLatinize) {

		final String timeLimit = AppConstants.EXP_DESIGN_TIME_LIMIT.getString();

		final List<ExpDesignParameter> paramList = new ArrayList<>();
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.SEED_PARAM, "", null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.BLOCKSIZE_PARAM, blockSize, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NTREATMENTS_PARAM, nTreatments, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NREPLICATES_PARAM, nReplicates, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.TREATMENTFACTOR_PARAM, treatmentFactor, null));

		this.addInitialTreatmenNumberIfAvailable(initialEntryNumber, paramList);

		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.REPLICATEFACTOR_PARAM, replicateFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.BLOCKFACTOR_PARAM, blockFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.PLOTFACTOR_PARAM, plotFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM,
			this.getPlotNumberStringValueOrDefault(initialPlotNumber), null));

		this.addLatinizeParametersForResolvableIncompleteBlockDesign(useLatinize, paramList, nBlatin, replatingGroups);

		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.TIMELIMIT_PARAM, timeLimit, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.OUTPUTFILE_PARAM, outputfile, null));

		final ExpDesign design = new ExpDesign(ExperimentDesignGenerator.RESOLVABLE_INCOMPLETE_BLOCK_DESIGN, paramList);

		return new MainDesign(design);
	}

	public MainDesign createResolvableRowColDesign(
		final String nTreatments, final String nReplicates, final String nRows,
		final String nColumns, final String treatmentFactor, final String replicateFactor, final String rowFactor,
		final String columnFactor, final String plotFactor, final Integer initialPlotNumber, final Integer initialEntryNumber,
		final String nrLatin, final String ncLatin, final String replatingGroups, final String outputfile, final Boolean useLatinize) {

		final String timeLimit = AppConstants.EXP_DESIGN_TIME_LIMIT.getString();

		final String plotNumberStrValue = (initialPlotNumber == null) ? "1" : String.valueOf(initialPlotNumber);

		final List<ExpDesignParameter> paramList = new ArrayList<>();
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.SEED_PARAM, "", null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NTREATMENTS_PARAM, nTreatments, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NREPLICATES_PARAM, nReplicates, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NROWS_PARAM, nRows, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NCOLUMNS_PARAM, nColumns, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.TREATMENTFACTOR_PARAM, treatmentFactor, null));

		this.addInitialTreatmenNumberIfAvailable(initialEntryNumber, paramList);

		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.REPLICATEFACTOR_PARAM, replicateFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.ROWFACTOR_PARAM, rowFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.COLUMNFACTOR_PARAM, columnFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.PLOTFACTOR_PARAM, plotFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM, plotNumberStrValue, null));

		this.addLatinizeParametersForResolvableRowAndColumnDesign(useLatinize, paramList, replatingGroups, nrLatin, ncLatin);

		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.TIMELIMIT_PARAM, timeLimit, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.OUTPUTFILE_PARAM, outputfile, null));

		final ExpDesign design = new ExpDesign(ExperimentDesignGenerator.RESOLVABLE_ROW_COL_DESIGN, paramList);

		return new MainDesign(design);
	}

	public MainDesign createAugmentedRandomizedBlockDesign(
		final Integer numberOfBlocks, final Integer numberOfTreatments,
		final Integer numberOfControls, final Integer startingPlotNumber, final Integer startingEntryNumber, final String treatmentFactor,
		final String blockFactor,
		final String plotFactor) {

		final List<ExpDesignParameter> paramList = new ArrayList<>();

		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NTREATMENTS_PARAM, String.valueOf(numberOfTreatments), null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NCONTROLS_PARAM, String.valueOf(numberOfControls), null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NBLOCKS_PARAM, String.valueOf(numberOfBlocks), null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.TREATMENTFACTOR_PARAM, treatmentFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.BLOCKFACTOR_PARAM, blockFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.PLOTFACTOR_PARAM, plotFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM,
			this.getPlotNumberStringValueOrDefault(startingPlotNumber), null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.SEED_PARAM, "", null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.OUTPUTFILE_PARAM, "", null));

		this.addInitialTreatmenNumberIfAvailable(startingEntryNumber, paramList);

		final ExpDesign design = new ExpDesign(ExperimentDesignGenerator.AUGMENTED_RANDOMIZED_BLOCK_DESIGN, paramList);

		return new MainDesign(design);
	}

	public MainDesign createPRepDesign(
		final Integer numberOfBlocks, final Integer nTreatments, final List<ListItem> nRepeatsListItem,
		final String treatmentFactor, final String blockFactor, final String plotFactor,
		final Integer initialPlotNumber, final Integer initialEntryNumber) {

		final String timeLimit = AppConstants.EXP_DESIGN_TIME_LIMIT.getString();

		final List<ExpDesignParameter> paramList = new ArrayList<>();
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.SEED_PARAM, "", null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NTREATMENTS_PARAM, String.valueOf(nTreatments), null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NBLOCKS_PARAM, String.valueOf(numberOfBlocks), null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NREPEATS_PARAM, null, nRepeatsListItem));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.TREATMENTFACTOR_PARAM, treatmentFactor, null));

		this.addInitialTreatmenNumberIfAvailable(initialEntryNumber, paramList);

		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.BLOCKFACTOR_PARAM, blockFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.PLOTFACTOR_PARAM, plotFactor, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.INITIAL_PLOT_NUMBER_PARAM,
			this.getPlotNumberStringValueOrDefault(initialPlotNumber), null));

		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.TIMELIMIT_PARAM, timeLimit, null));
		paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.OUTPUTFILE_PARAM, "", null));

		final ExpDesign design = new ExpDesign(ExperimentDesignGenerator.P_REP_DESIGN, paramList);

		return new MainDesign(design);
	}

	public List<ListItem> createReplicationListItemForPRepDesign(
		final List<ImportedGermplasm> germplasmList, final float replicationPercentage,
		final int replicationNumber) {

		// Count how many test entries we have in the germplasm list.
		int testEntryCount = 0;

		// Determine which of the germplasm entries are test entries
		final List<Integer> testEntryNumbers = new ArrayList<>();

		for (final ImportedGermplasm importedGermplasm : germplasmList) {
			if (SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId() == importedGermplasm.getEntryTypeCategoricalID()) {
				testEntryCount++;
				testEntryNumbers.add(importedGermplasm.getEntryId());
			}
		}

		// Compute how may test entries we can replicate based on replicationPercentage (% of test entries to replicate)
		final float noOfTestEntriesToReplicate = Math.round((float) testEntryCount * (replicationPercentage / 100));
		// Pick any random test entries to replicate
		final Set<Integer> randomTestEntryNumbers = new HashSet<>();
		while (randomTestEntryNumbers.size() < noOfTestEntriesToReplicate) {
			randomTestEntryNumbers.add(testEntryNumbers.get(this.random.nextInt(testEntryNumbers.size())));
		}

		final List<ListItem> replicationListItem = new LinkedList<>();
		for (final ImportedGermplasm importedGermplasm : germplasmList) {
			if (SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId() != importedGermplasm.getEntryTypeCategoricalID()) {
				// All Check Entries in the list should be replicated
				replicationListItem.add(new ListItem(String.valueOf(replicationNumber)));
			} else if (randomTestEntryNumbers.contains(importedGermplasm.getEntryId())) {
				// Randomized Test Entries should be replicated
				replicationListItem.add(new ListItem(String.valueOf(replicationNumber)));
			} else {
				// Default replication number is 1
				replicationListItem.add(new ListItem(String.valueOf(1)));
			}
		}

		return replicationListItem;
	}

	public List<ObservationUnitRow> generateExperimentDesignMeasurements(
		final int noOfExistingEnvironments, final int noOfEnvironmentsToAdd, final List<MeasurementVariable> generateDesignVariables,
		final List<ImportedGermplasm> germplasmList, final MainDesign mainDesign, final String entryNumberIdentifier,
		final Map<String, List<String>> treatmentFactorValues, final Map<Integer, Integer> designExpectedEntriesMap)
		throws BVDesignException {

		// Specify number of study instances for BVDesign generation
		mainDesign.getDesign().getParameters()
			.add(this.createExpDesignParameter(NUMBER_TRIALS_PARAM, String.valueOf(noOfEnvironmentsToAdd), null));
		BVDesignOutput bvOutput = null;
		try {
			bvOutput = this.bvDesignRunner.runBVDesign(mainDesign);
		} catch (final Exception e) {
			ExperimentDesignGenerator.LOG.error(e.getMessage(), e);
			throw new BVDesignException("experiment.design.bv.exe.error.generate.generic.error");
		}

		if (bvOutput == null || !bvOutput.isSuccess()) {
			throw new BVDesignException("experiment.design.generate.generic.error");
		}

		//Converting germplasm List to map
		final Map<Integer, ImportedGermplasm> importedGermplasmMap =
			germplasmList.stream().collect(Collectors.toMap(ImportedGermplasm::getEntryId,
				Function.identity()));
		final List<ObservationUnitRow> rows = new ArrayList<>();
		int trialInstanceNumber = noOfExistingEnvironments - noOfEnvironmentsToAdd + 1;
		for (final BVDesignTrialInstance instance : bvOutput.getTrialInstances()) {
			for (final Map<String, String> row : instance.getRows()) {
				final String entryNoValue = row.get(entryNumberIdentifier);
				final Integer entryNumber = StringUtil.parseInt(entryNoValue, null);
				if (entryNumber == null) {
					throw new BVDesignException("experiment.design.bv.exe.error.output.invalid.error");
				}
				final Optional<ImportedGermplasm> importedGermplasm =
					this.findImportedGermplasmByEntryNumberAndChecks(importedGermplasmMap, entryNumber, designExpectedEntriesMap);

				if (!importedGermplasm.isPresent()) {
					throw new BVDesignException("experiment.design.bv.exe.error.output.invalid.error");
				}
				final ObservationUnitRow observationUnitRow = this.createObservationUnitRow(generateDesignVariables, importedGermplasm.get(), row,
					treatmentFactorValues, trialInstanceNumber);
				rows.add(observationUnitRow);
			}
			trialInstanceNumber++;
		}
		return rows;
	}

	ObservationUnitRow createObservationUnitRow(
		final List<MeasurementVariable> headerVariable, final ImportedGermplasm germplasm,
		final Map<String, String> bvEntryMap, final Map<String, List<String>> treatmentFactorValues, final int trialNo) {
		final ObservationUnitRow observationUnitRow = new ObservationUnitRow();
		final Map<String, ObservationUnitData> observationUnitDataMap = new HashMap<>();
		ObservationUnitData treatmentLevelData = null;
		ObservationUnitData observationUnitData;

		observationUnitData = this.createObservationUnitData(TermId.TRIAL_INSTANCE_FACTOR.getId(), String.valueOf(trialNo));
		observationUnitDataMap.put(String.valueOf(observationUnitData.getVariableId()), observationUnitData);

		for (final MeasurementVariable var : headerVariable) {

			observationUnitData = null;

			final int termId = var.getTermId();

			if (termId == TermId.ENTRY_NO.getId()) {
				observationUnitData = this.createObservationUnitData(var.getTermId(), String.valueOf(germplasm.getEntryId()));
			} else if (termId == TermId.SOURCE.getId() || termId == TermId.GERMPLASM_SOURCE.getId()) {
				observationUnitData =
					this.createObservationUnitData(var.getTermId(), germplasm.getSource() != null ? germplasm.getSource() : "");
			} else if (termId == TermId.GROUPGID.getId()) {
				observationUnitData = this.createObservationUnitData(var.getTermId(),
					germplasm.getGroupId() != null ? germplasm.getGroupId().toString() : "");
			} else if (termId == TermId.STOCKID.getId()) {
				observationUnitData =
					this.createObservationUnitData(var.getTermId(), germplasm.getStockIDs() != null ? germplasm.getStockIDs() : "");
			} else if (termId == TermId.CROSS.getId()) {
				observationUnitData = this.createObservationUnitData(var.getTermId(), germplasm.getCross());
			} else if (termId == TermId.DESIG.getId()) {
				observationUnitData = this.createObservationUnitData(var.getTermId(), germplasm.getDesig());
			} else if (termId == TermId.GID.getId()) {
				observationUnitData = this.createObservationUnitData(var.getTermId(), germplasm.getGid());
			} else if (termId == TermId.ENTRY_CODE.getId()) {
				observationUnitData = this.createObservationUnitData(var.getTermId(), germplasm.getEntryCode());
			} else if (EXP_DESIGN_VARIABLE_IDS.contains(termId)) {
				observationUnitData = this.createObservationUnitData(var.getTermId(), bvEntryMap.get(var.getName()));
			} else if (termId == TermId.CHECK.getId()) {
				observationUnitData =
					this.createObservationUnitData(var.getTermId(), Integer.toString(germplasm.getEntryTypeCategoricalID()));
			} else if (termId == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
				observationUnitData = this.createObservationUnitData(var.getTermId(), Integer.toString(trialNo));
			} else if (StringUtils.isEmpty(var.getTreatmentLabel())) {
				if (treatmentLevelData == null) {
					observationUnitData = this.createObservationUnitData(var.getTermId(),
						bvEntryMap.get(ExpDesignUtil.cleanBVDesingKey(Integer.toString(var.getTermId()))));
					treatmentLevelData = observationUnitData;
				} else {
					final String level = treatmentLevelData.getValue();
					if (NumberUtils.isNumber(level)) {
						final int index = Integer.valueOf(level) - 1;
						if (treatmentFactorValues != null && treatmentFactorValues
							.containsKey(String.valueOf(treatmentLevelData.getVariableId()))) {
							final Object tempObj =
								treatmentFactorValues.get(String.valueOf(treatmentLevelData.getVariableId()))
									.get(index);
							String value = "";
							if (tempObj != null) {
								if (tempObj instanceof String) {
									value = (String) tempObj;
								} else {
									value = Integer.toString((Integer) tempObj);
								}
							}
							if (var.getDataTypeId() != null && var.getDataTypeId().intValue() == TermId.DATE_VARIABLE.getId()) {
								value = DateUtil.convertToDBDateFormat(var.getDataTypeId(), value);
								observationUnitData = this.createObservationUnitData(var.getTermId(), value);
							} else if (var.getPossibleValues() != null && !var.getPossibleValues().isEmpty() && NumberUtils
								.isNumber(value)) {
								observationUnitData = this.createObservationUnitData(var.getTermId(), value);
							} else {
								observationUnitData = this.createObservationUnitData(var.getTermId(), value);
							}
						}
					}
					treatmentLevelData = null;
				}

			} else {
				// meaning non factor
				observationUnitData = this.createObservationUnitData(var.getTermId(), "");
			}

			observationUnitDataMap.put(String.valueOf(observationUnitData.getVariableId()), observationUnitData);
		}
		observationUnitRow.setVariables(observationUnitDataMap);
		return observationUnitRow;
	}

	ObservationUnitData createObservationUnitData(final Integer variableId, final String value) {
		final ObservationUnitData observationUnitData = new ObservationUnitData();
		observationUnitData.setVariableId(variableId);
		observationUnitData.setValue(value);
		return observationUnitData;
	}

	ExpDesignParameter createExpDesignParameter(final String name, final String value, final List<ListItem> items) {

		final ExpDesignParameter designParam = new ExpDesignParameter(name, value);
		if (items != null && !items.isEmpty()) {
			designParam.setListItem(items);
		}
		return designParam;
	}

	Optional<ImportedGermplasm> findImportedGermplasmByEntryNumberAndChecks(
		final Map<Integer, ImportedGermplasm> importedGermplasmMap,
		final Integer entryNumber, final Map<Integer, Integer> designExpectedEntriesMap) {

		final Integer resolvedEntryNumber = this.resolveMappedEntryNumber(entryNumber, designExpectedEntriesMap);

		if (importedGermplasmMap.containsKey(resolvedEntryNumber)) {
			return Optional.of(importedGermplasmMap.get(resolvedEntryNumber));
		}

		return Optional.absent();

	}

	Integer resolveMappedEntryNumber(final Integer entryNumber, final Map<Integer, Integer> designExpectedEntriesMap) {

		if (designExpectedEntriesMap.containsKey(entryNumber)) {
			return designExpectedEntriesMap.get(entryNumber);
		}

		return entryNumber;

	}

	String getPlotNumberStringValueOrDefault(final Integer initialPlotNumber) {
		return (initialPlotNumber == null) ? "1" : String.valueOf(initialPlotNumber);
	}

	void addInitialTreatmenNumberIfAvailable(final Integer initialEntryNumber, final List<ExpDesignParameter> paramList) {

		if (initialEntryNumber != null) {
			paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.INITIAL_TREATMENT_NUMBER_PARAM,
				String.valueOf(initialEntryNumber), null));
		}

	}

	void addLatinizeParametersForResolvableIncompleteBlockDesign(
		final boolean useLatinize, final List<ExpDesignParameter> paramList,
		final String nBlatin, final String replatingGroups) {

		if (useLatinize) {
			paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NBLATIN_PARAM, nBlatin, null));
			// we add the string tokenize replating groups
			// we tokenize the replating groups
			final StringTokenizer tokenizer = new StringTokenizer(replatingGroups, ",");
			final List<ListItem> replatingList = new ArrayList<>();
			while (tokenizer.hasMoreTokens()) {
				replatingList.add(new ListItem(tokenizer.nextToken()));
			}
			paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.REPLATINGROUPS_PARAM, null, replatingList));
		} else {
			paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NBLATIN_PARAM, "0", null));
		}

	}

	void addLatinizeParametersForResolvableRowAndColumnDesign(
		final Boolean useLatinize, final List<ExpDesignParameter> paramList,
		final String replatingGroups, final String nrLatin, final String ncLatin) {

		if (useLatinize != null && useLatinize.booleanValue()) {
			paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NRLATIN_PARAM, nrLatin, null));
			paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NCLATIN_PARAM, ncLatin, null));
			// we tokenize the replating groups
			final StringTokenizer tokenizer = new StringTokenizer(replatingGroups, ",");
			final List<ListItem> replatingList = new ArrayList<>();
			while (tokenizer.hasMoreTokens()) {
				replatingList.add(new ListItem(tokenizer.nextToken()));
			}
			paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.REPLATINGROUPS_PARAM, null, replatingList));
		} else {
			paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NRLATIN_PARAM, "0", null));
			paramList.add(this.createExpDesignParameter(ExperimentDesignGenerator.NCLATIN_PARAM, "0", null));
		}

	}

	List<ListItem> convertToListItemList(final List<String> listString) {

		final List<ListItem> listItemList = new ArrayList<>();
		for (final String value : listString) {
			listItemList.add(new ListItem(value));
		}
		return listItemList;

	}

	List<ListItem> getInitialTreatNumList(final List<String> treatmentFactors, final Integer initialTreatNum, final String entryNoVarName) {

		final List<ListItem> listItemList = new ArrayList<>();
		for (final String treatmentFactor : treatmentFactors) {
			if (treatmentFactor.equals(entryNoVarName)) {
				listItemList.add(new ListItem(String.valueOf(initialTreatNum)));
			} else {
				listItemList.add(new ListItem("1"));
			}
		}
		return listItemList;

	}

	public Map<Integer, MeasurementVariable> getMeasurementVariablesMap(final int studyId, final String programUUID,
		final List<Integer> designFactors, final List<Integer> treatmentFactors) {

		final int plotDatasetId =
			this.studyDataManager.getDataSetsByType(studyId, DatasetTypeEnum.PLOT_DATA.getId()).get(0).getId();

		final List<MeasurementVariable> measurementVariables =
			this.datasetService.getDatasetMeasurementVariablesByVariableType(plotDatasetId,
				Arrays.asList(VariableType.ENVIRONMENT_DETAIL.getId(), VariableType.GERMPLASM_DESCRIPTOR.getId()));

		final Map<Integer, MeasurementVariable> measurementVariablesMap = measurementVariables.stream()
			.collect(Collectors.toMap(MeasurementVariable::getTermId, measurementVariable -> measurementVariable));

		final List<StandardVariable> designFactorStandardVariables =
			this.ontologyDataManager.getStandardVariables(designFactors, programUUID);

		for (final StandardVariable standardVariable : designFactorStandardVariables) {
			measurementVariablesMap.put(standardVariable.getId(),
				this.standardVariableTransformer.convert(standardVariable, VariableType.EXPERIMENTAL_DESIGN));
		}

		final List<StandardVariable> treatmentFactorStandardVariables =
			this.ontologyDataManager.getStandardVariables(treatmentFactors, programUUID);

		for (final StandardVariable standardVariable : treatmentFactorStandardVariables) {
			measurementVariablesMap.put(standardVariable.getId(),
				this.standardVariableTransformer.convert(standardVariable, VariableType.TREATMENT_FACTOR));
		}

		return measurementVariablesMap;
	}

}
