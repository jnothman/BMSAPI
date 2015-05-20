
package org.ibp.api.rest.ontology;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.ibp.api.domain.ontology.IdName;
import org.ibp.api.java.ontology.OntologyModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Api(value = "Ontology Data Type Service")
@Controller
@RequestMapping("/ontology")
public class DataTypeResource {

	@Autowired
	private OntologyModelService ontologyModelService;

	@ApiOperation(value = "All Data Types", notes = "Get all Data Types")
	@RequestMapping(value = "/datatypes", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<IdName>> listAllDataTypes() {
		return new ResponseEntity<>(this.ontologyModelService.getAllDataTypes(), HttpStatus.OK);
	}
}