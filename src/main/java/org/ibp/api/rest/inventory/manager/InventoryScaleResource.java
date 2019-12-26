package org.ibp.api.rest.inventory.manager;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.domain.oms.TermId;
import org.ibp.api.domain.ontology.VariableDetails;
import org.ibp.api.domain.ontology.VariableFilter;
import org.ibp.api.java.ontology.VariableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by clarysabel on 10/28/19.
 */
@Api(value = "Inventory Scale Services")
@RestController
public class InventoryScaleResource {

	@Autowired
	private VariableService variableService;

	@ApiOperation(value = "It will retrieve all inventory scales", notes = "It will retrieve all inventory scales")
	@RequestMapping(value = "/crops/{cropName}/inventory-scales", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<VariableDetails>> getLots(
			@PathVariable
			final String cropName) {
		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addPropertyId(TermId.INVENTORY_AMOUNT_PROPERTY.getId());
		List<VariableDetails> variables = this.variableService.getVariablesByFilter(variableFilter);
		return new ResponseEntity<>(variables, HttpStatus.OK);
	}
}