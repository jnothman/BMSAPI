package org.ibp.api.rest.rcall;

import com.wordnik.swagger.annotations.ApiOperation;
import org.ibp.api.domain.rpackage.RCallDTO;
import org.ibp.api.java.rpackage.RPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rpackage")
public class RCallResource {

	@Autowired
	private RPackageService rPackageService;

	@ApiOperation(value = "Get all R Calls", notes = "Get all R Calls")
	@RequestMapping(value = "/rcalls", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<RCallDTO>> getAllRCalls() {
		return new ResponseEntity<>(this.rPackageService.getAllRCalls(), HttpStatus.OK);
	}

}
