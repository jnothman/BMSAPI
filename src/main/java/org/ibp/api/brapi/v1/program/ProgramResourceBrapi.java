
package org.ibp.api.brapi.v1.program;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.service.api.program.ProgramDetailsDto;
import org.generationcp.middleware.service.api.program.ProgramFilters;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.program.ProgramService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * BMS implementation of the <a href="http://docs.brapi.apiary.io/">BrAPI</a> Location services.
 *
 * @author Diego Cuenya
 */
@Api(value = "BrAPI Program Services")
@Controller
public class ProgramResourceBrapi {

	@Autowired
	private ProgramService programService;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@ApiOperation(value = "List Programs", notes = "Get a list of programs.")
	@RequestMapping(value = "/{crop}/brapi/v1/programs", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Programs> listPrograms(@PathVariable final String crop,
			@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION, required = false) @RequestParam(value = "page",
					required = false) final Integer currentPage,
			@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false) @RequestParam(value = "pageSize",
					required = false) final Integer pageSize,
			@ApiParam(value = "Filter by program name. Exact match.", required = false) @RequestParam(value = "programName",
					required = false) final String programName,
			@ApiParam(value = "Filter by program abbreviation. Exact match.", required = false) @RequestParam(value = "abbreviation",
					required = false) final String abbreviation) {

		final Map<ProgramFilters, Object> filters = new EnumMap<>(ProgramFilters.class);
		PagedResult<ProgramDetailsDto> resultPage = null;
		this.setFilters(filters, crop, programName);

		if (filters.get(ProgramFilters.CROP_TYPE) == null) {
			final List<Map<String, String>> status = Collections.singletonList(ImmutableMap.of("message",  "crop " + crop + " doesn't exist"));
			final Metadata metadata = new Metadata(null, status);
			final Programs programList = new Programs().withMetadata(metadata);
			return new ResponseEntity<>(programList, HttpStatus.NOT_FOUND);
		}

		/**
		 * At the moment we don't have the abbreviation field. When this filter is used will be returned "not found programs".
		 **/
		if (StringUtils.isBlank(abbreviation)) {

			resultPage = new PaginatedSearch().executeBrapiSearch(currentPage, pageSize, new SearchSpec<ProgramDetailsDto>() {

				@Override
				public long getCount() {
					return ProgramResourceBrapi.this.programService.countProgramsByFilter(filters);
				}

				@Override
				public List<ProgramDetailsDto> getResults(final PagedResult<ProgramDetailsDto> pagedResult) {
					// BRAPI services have zero-based indexing for pages but paging for Middleware method starts at 1
					final int pageNumber = pagedResult.getPageNumber() + 1;
					return ProgramResourceBrapi.this.programService.getProgramsByFilter(pageNumber, pagedResult.getPageSize(), filters);
				}
			});
		}

		if (resultPage != null && resultPage.getTotalResults() > 0) {
			final ModelMapper mapper = ProgramMapper.getInstance();
			final List<Program> programs = new ArrayList<>();

			for (final ProgramDetailsDto programDetailsDto : resultPage.getPageResults()) {
				final Program program = mapper.map(programDetailsDto, Program.class);
				programs.add(program);
			}

			final Result<Program> results = new Result<Program>().withData(programs);
			final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
					.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

			final Metadata metadata = new Metadata().withPagination(pagination);
			final Programs programList = new Programs().withMetadata(metadata).withResult(results);
			return new ResponseEntity<>(programList, HttpStatus.OK);
		}
		final List<Map<String, String>> status = Collections.singletonList(ImmutableMap.of("message",  "program not found."));
		final Metadata metadata = new Metadata(null, status);
		final Programs programList = new Programs().withMetadata(metadata);
		return new ResponseEntity<>(programList, HttpStatus.NOT_FOUND);
	}

	private void setFilters(final Map<ProgramFilters, Object> filters, final String crop, final String programName) {
		List<CropType> cropTypeList;
		cropTypeList = this.workbenchDataManager.getInstalledCropDatabses();

		for (final CropType cropType : cropTypeList) {
			if (cropType.getCropName().equalsIgnoreCase(crop)) {
				filters.put(ProgramFilters.CROP_TYPE, cropType);
			}
		}

		if (!StringUtils.isBlank(programName)) {
			filters.put(ProgramFilters.PROGRAM_NAME, programName);
		}
	}
}
