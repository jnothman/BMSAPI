package org.ibp.api.java.dataset;

import java.util.List;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.ibp.api.domain.dataset.DatasetVariable;
import org.ibp.api.domain.dataset.Observation;
import org.ibp.api.domain.dataset.ObservationValue;

public interface DatasetService {

	long countPhenotypes(Integer studyId, Integer datasetId, List<Integer> traitIds);

	MeasurementVariable addDatasetVariable(Integer studyId, Integer datasetId, DatasetVariable datasetVariable);

	Observation updatePhenotype(Integer observationId, ObservationValue observationValue);
}