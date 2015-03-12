package org.generationcp.bms.ontology;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.generationcp.bms.ontology.dto.GenericResponse;
import org.generationcp.bms.ontology.dto.MethodRequest;
import org.generationcp.bms.ontology.dto.MethodResponse;
import org.generationcp.bms.ontology.dto.MethodSummary;
import org.generationcp.bms.ontology.services.OntologyModelService;
import org.generationcp.bms.ontology.validator.DeletableValidator;
import org.generationcp.bms.ontology.validator.EditableValidator;
import org.generationcp.bms.ontology.validator.IntegerValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;


@Api(value = "Ontology Method Service")
@Controller
@RequestMapping("/ontology")
@SuppressWarnings("unused") // Added because it shows the cropname not used warning that is used in URL
public class OntologyMethodResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyMethodResource.class);

    @Autowired
    private IntegerValidator integerValidator;
    @Autowired
    private EditableValidator editableValidator;
    @Autowired
    private DeletableValidator deletableValidator;
    @Autowired
    private OntologyModelService ontologyModelService;

    @ApiOperation(value = "All Methods", notes = "Get all methods")
    @RequestMapping(value = "/{cropname}/methods", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<MethodSummary>> listAllMethods(@PathVariable String  cropname) throws Exception {
        List<MethodSummary> methodList = ontologyModelService.getAllMethods();
        if(methodList.isEmpty()){
            LOGGER.error("No Valid Method Found");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(methodList, HttpStatus.OK);
    }

    // TODO : editableFields and deletable need to be determined
    @ApiOperation(value = "Get method by id", notes = "Get method using given method id")
	@RequestMapping(value = "/{cropname}/methods/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<?> getMethodById(@PathVariable String cropname, @PathVariable String id) throws Exception {
        //FIXME : BindingResult does not work with @PathVariable in method argument so here initialize BindingResult with MapBindingResult

        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
        integerValidator.validate(id, bindingResult);
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(DefaultExceptionHandler.parseErrors(bindingResult), HttpStatus.BAD_REQUEST);
        }
        MethodResponse method = ontologyModelService.getMethod(Integer.valueOf(id));
        if(method == null) {
            LOGGER.error("No Valid Method Found using Id " + id);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
		return new ResponseEntity<>(method, HttpStatus.OK);
	}

    //TODO: 403 response for user without permission
    @ApiOperation(value = "Add Method", notes = "Add a Method using Given Data")
    @RequestMapping(value = "/{cropname}/methods", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<GenericResponse> addMethod(@PathVariable String  cropname,@RequestBody @Valid MethodRequest request) throws Exception {
        GenericResponse response = ontologyModelService.addMethod(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    //TODO: 403 response for user without permission
    @ApiOperation(value = "Update Method", notes = "Update Method using Given Data")
    @RequestMapping(value = "/{cropname}/methods/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<?> updateMethod(@PathVariable String  cropname,@PathVariable Integer id, @RequestBody MethodRequest request, BindingResult result) throws Exception {
        request.setId(id);
        editableValidator.validate(request, result);
        if(result.hasErrors()){
            return new ResponseEntity<>(DefaultExceptionHandler.parseErrors(result), HttpStatus.BAD_REQUEST);
        }
        ontologyModelService.updateMethod(request.getId(), request);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    //TODO: 403 response for user without permission
    @ApiOperation(value = "Delete Method", notes = "Delete Method using Given Id")
    @RequestMapping(value = "/{cropname}/methods/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity deleteMethod(@PathVariable String  cropname,@PathVariable Integer id) throws Exception {
        //FIXME : BindingResult does not work with @PathVariable in method argument so here initialize BindingResult with MapBindingResult
        BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), "Method");
        deletableValidator.validate(id, bindingResult);
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(DefaultExceptionHandler.parseErrors(bindingResult), HttpStatus.BAD_REQUEST);
        }
        ontologyModelService.deleteMethod(id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
