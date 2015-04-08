package org.ibp.api.rest.ontology;

import java.util.HashMap;
import java.util.List;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.ibp.api.domain.ontology.GenericResponse;
import org.ibp.api.domain.ontology.PropertyRequest;
import org.ibp.api.domain.ontology.PropertyResponse;
import org.ibp.api.domain.ontology.PropertySummary;
import org.ibp.api.domain.ontology.TermRequest;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.ontology.validator.PropertyRequestValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.RequestIdValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermDeletableValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.ibp.api.java.ontology.OntologyModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Strings;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Api(value = "Ontology Property Service")
@Controller
@RequestMapping("/ontology")
public class OntologyPropertyResource {

	@Autowired
	private RequestIdValidator requestIdValidator;

	@Autowired
	private TermValidator termValidator;

	@Autowired
	private PropertyRequestValidator propertyRequestValidator;

	@Autowired
	private TermDeletableValidator deletableValidator;

	@Autowired
	private OntologyModelService ontologyModelService;

	/**
	 * @param cropname
	 *            The crop for which this rest call is being made
	 */
	@ApiOperation(value = "All properties or filter by class name", notes = "Get all properties or filter by class name")
	@RequestMapping(value = "/{cropname}/properties", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<PropertySummary>> listAllPropertyByClass(
			@PathVariable String cropname,
			@RequestParam(value = "class", defaultValue = "", required = false) String className)
					throws MiddlewareQueryException {
		if (Strings.isNullOrEmpty(className)) {
			List<PropertySummary> propertyList = this.ontologyModelService.getAllProperties();
			return new ResponseEntity<>(propertyList, HttpStatus.OK);
		}
		List<PropertySummary> propertyList = this.ontologyModelService
				.getAllPropertiesByClass(className);
		return new ResponseEntity<>(propertyList, HttpStatus.OK);

	}

	/**
	 * @param cropname
	 *            The crop for which this rest call is being made
	 */
	@ApiOperation(value = "Get Property by id", notes = "Get Property using given Property id")
	@RequestMapping(value = "/{cropname}/properties/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<PropertyResponse> getPropertyById(@PathVariable String cropname,
			@PathVariable String id) throws MiddlewareQueryException, MiddlewareException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Property");
		this.requestIdValidator.validate(id, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		TermRequest request = new TermRequest(Integer.valueOf(id), "property",
				CvId.PROPERTIES.getId());
		this.termValidator.validate(request, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		return new ResponseEntity<>(this.ontologyModelService.getProperty(Integer.valueOf(id)),
				HttpStatus.OK);
	}

	/**
	 * @param cropname
	 *            The crop for which this rest call is being made
	 */
	// TODO: 403 response for user without permission
	@ApiOperation(value = "Add Property", notes = "Add a Property using Given Data")
	@RequestMapping(value = "/{cropname}/properties", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<GenericResponse> addProperty(@PathVariable String cropname,
			@RequestBody PropertyRequest request, BindingResult bindingResult)
					throws MiddlewareQueryException, MiddlewareException {
		this.propertyRequestValidator.validate(request, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		return new ResponseEntity<>(this.ontologyModelService.addProperty(request),
				HttpStatus.CREATED);
	}

	/**
	 * @param cropname
	 *            The crop for which this rest call is being made
	 */
	// TODO: 403 response for user without permission
	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Delete Property", notes = "Delete Property using Given Id")
	@RequestMapping(value = "/{cropname}/properties/{id:.+}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity deleteProperty(@PathVariable String cropname, @PathVariable String id)
			throws MiddlewareQueryException, MiddlewareException {
		BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(),
				"Property");

		this.requestIdValidator.validate(id, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}

		this.deletableValidator.validate(new TermRequest(Integer.valueOf(id), "property",
				CvId.PROPERTIES.getId()), bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		this.ontologyModelService.deleteProperty(Integer.valueOf(id));
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	/**
	 * @param cropname
	 *            The name of the crop which this rest call is being made
	 */
	// TODO: 403 response for user without permission
	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "Update Property", notes = "Update Property using Given Data")
	@RequestMapping(value = "/{cropname}/properties/{id:.+}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity updateProperty(@PathVariable String cropname, @PathVariable String id,
			@RequestBody PropertyRequest request, BindingResult bindingResult)
					throws MiddlewareQueryException, MiddlewareException {
		this.requestIdValidator.validate(id, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		request.setId(Integer.valueOf(id));
		this.propertyRequestValidator.validate(request, bindingResult);
		if (bindingResult.hasErrors()) {
			throw new ApiRequestValidationException(bindingResult.getAllErrors());
		}
		this.ontologyModelService.updateProperty(Integer.valueOf(id), request);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
