package org.ibp.api.rest.dataset;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.ibp.api.domain.study.Observation;
import org.ibp.api.java.dataset.DatasetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Api(value = "Dataset Services")
@Controller
public class DatasetResource {

	@Autowired
	private DatasetService datasetService;

	@ApiOperation(value = "Generate and save a dataset", notes = "Returns the basic information for the generated dataset")
	@RequestMapping(value = "/crops/{cropName}/studies/{studyId}/datasets/generation", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Observation> generateDataset(@PathVariable
	final String cropName, @PathVariable final Integer studyId, @RequestBody final DatasetGeneratorInput datasetGeneratorInput) {
		datasetService.generateSubObservationDataset(studyId, datasetGeneratorInput);
		return null;
	}
}
