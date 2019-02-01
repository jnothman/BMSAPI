package org.ibp.api.rest.labelprinting;

import org.ibp.api.rest.labelprinting.domain.LabelType;
import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummary;
import org.ibp.api.rest.labelprinting.domain.LabelsInfoInput;
import org.ibp.api.rest.labelprinting.domain.LabelsNeededSummaryResponse;

import java.util.List;
import java.util.Map;

public interface LabelPrintingStrategy {

	void validateLabelsInfoInputData(final LabelsInfoInput labelsInfoInput);

	void validateLabelsGeneratorInputData(final LabelsGeneratorInput labelsGeneratorInput);

	LabelsNeededSummary getSummaryOfLabelsNeeded (final LabelsInfoInput labelsInfoInput);

	LabelsNeededSummaryResponse transformLabelsNeededSummary(final LabelsNeededSummary labelsNeededSummary);

	Map<String, String> getOriginResourceMetadata (final LabelsInfoInput labelsInfoInput);

	List<LabelType> getAvailableLabelFields(final LabelsInfoInput labelsInfoInput);

}
