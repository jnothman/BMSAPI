
package org.ibp.api.brapi.v1.study;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import liquibase.util.StringUtils;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.location.LocationDetailsDto;
import org.generationcp.middleware.service.api.location.LocationFilters;
import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.generationcp.middleware.service.api.study.StudyDto;
import org.generationcp.middleware.service.api.study.StudySearchFilter;
import org.generationcp.middleware.service.api.study.TrialObservationTable;
import org.ibp.api.brapi.v1.common.BrapiPagedResult;
import org.ibp.api.brapi.v1.common.EntityListResponse;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Pagination;
import org.ibp.api.brapi.v1.common.Result;
import org.ibp.api.brapi.v1.location.Location;
import org.ibp.api.brapi.v1.location.LocationMapper;
import org.ibp.api.domain.common.PagedResult;
import org.ibp.api.java.study.StudyService;
import org.ibp.api.rest.common.PaginatedSearch;
import org.ibp.api.rest.common.SearchSpec;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BMS implementation of the <a href="http://docs.brapi.apiary.io/">BrAPI</a> Study services.
 */
@Api(value = "BrAPI Study Services")
@Controller
public class StudyResourceBrapi {

	public static final String CSV = "csv";
	public static final String TSV = "tsv";

	public static final String CONTENT_TYPE = "Content-Type";
	public static final String CONTENT_DISPOSITION = "Content-Disposition";

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private StudyService studyService;

	@Autowired
	private LocationDataManager locationDataManager;

	@ApiOperation(value = "List of studies", notes = "Get a list of studies.")
	@RequestMapping(value = "/{crop}/brapi/v1/studies", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<EntityListResponse<StudyDto>> listStudies(@PathVariable final String crop,
		@ApiParam(value = "Common name for the crop associated with this study.") @RequestParam(value = "commonCropName", required = false)
		final String commonCropName,
		@ApiParam(value = "Filter based on study type unique identifier") @RequestParam(value = "studyTypeDbId.", required = false)
		final String studyTypeDbId,
		@ApiParam(value = "Program filter to only return studies associated with given program id.")
		@RequestParam(value = "programDbId", required = false) final String programDbId,
		@ApiParam(value = "Filter by location.") @RequestParam(value = "locationDbId", required = false) final String locationDbId,
		@ApiParam(value = "Filter by season.") @RequestParam(value = "seasonDbId", required = false) final String seasonDbId,
		@ApiParam(
			value =
				"Filter by trial.") @RequestParam(value = "trialDbId", required = false) final String trialDbId,
		@ApiParam(value = "Filter by study DbId") @RequestParam(value = "studyDbId", required = false) final String studyDbId,
		@ApiParam(value = "Filter active status true/false.") @RequestParam(value = "active", required = false) final Boolean active,
		@ApiParam(value = "Name of the field to sort by.") @RequestParam(value = "sortBy", required = false) final String sortBy,
		@ApiParam(value = "Sort order direction. Ascending/Descending.") @RequestParam(value = "sortOrder", required = false)
		final String sortOrder,
		@ApiParam(value = BrapiPagedResult.CURRENT_PAGE_DESCRIPTION, required = false) @RequestParam(value = "page",
			required = false) final Integer currentPage,
		@ApiParam(value = BrapiPagedResult.PAGE_SIZE_DESCRIPTION, required = false) @RequestParam(value = "pageSize",
			required = false) final Integer pageSize) {

		final StudySearchFilter studySearchFilter = new StudySearchFilter();
		final int finalPageNumber = currentPage == null ? BrapiPagedResult.DEFAULT_PAGE_NUMBER : currentPage;
		final int finalPageSize = pageSize == null ? BrapiPagedResult.DEFAULT_PAGE_SIZE : pageSize;
		studySearchFilter.setCommonCropName(crop);
		studySearchFilter.setStudyTypeDbId(studyTypeDbId);
		studySearchFilter.setProgramDbId(programDbId);
		studySearchFilter.setLocationDbId(locationDbId);
		studySearchFilter.setSeasonDbId(seasonDbId);
		studySearchFilter.setTrialDbId(trialDbId);
		studySearchFilter.setActive(active);
		studySearchFilter.getSortedRequest().setPageNumber(finalPageNumber);
		studySearchFilter.getSortedRequest().setPageSize(finalPageSize);
		studySearchFilter.getSortedRequest().setSortBy(sortBy);
		studySearchFilter.getSortedRequest().setSortOrder(sortOrder);

		final PagedResult<StudyDto> resultPage =
			new PaginatedSearch().executeBrapiSearch(currentPage, pageSize, new SearchSpec<StudyDto>() {

				@Override
				public long getCount() {
					return StudyResourceBrapi.this.studyService.countStudyDTOs(studySearchFilter);
				}

				@Override
				public List<StudyDto> getResults(final PagedResult<StudyDto> pagedResult) {
					return StudyResourceBrapi.this.studyService.getStudyDTOs(studySearchFilter);
				}
			});

		final List<StudyDto> summaryDtoList = resultPage.getPageResults();

		final Result<StudyDto> result = new Result<StudyDto>().withData(summaryDtoList);
		final Pagination pagination = new Pagination().withPageNumber(resultPage.getPageNumber()).withPageSize(resultPage.getPageSize())
			.withTotalCount(resultPage.getTotalResults()).withTotalPages(resultPage.getTotalPages());

		final Metadata metadata = new Metadata().withPagination(pagination);
		final EntityListResponse<StudyDto> entityListResponse = new EntityListResponse<>(metadata, result);

		return new ResponseEntity<>(entityListResponse, HttpStatus.OK);
	}

	@ApiOperation(value = "Get study observation details as table", notes = "Get study observation details as table")
	@RequestMapping(value = "/{crop}/brapi/v1/studies/{studyDbId}/table", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<StudyObservations> getStudyObservationsAsTable(final HttpServletResponse response,
		@PathVariable final String crop, @PathVariable final int studyDbId,
		@ApiParam(value = "The format parameter will cause the data to be dumped to a file in the specified format",
			required = false) @RequestParam(value = "format", required = false) final String format)
		throws Exception {

		if (!StringUtils.isEmpty(format)) {
			if (StudyResourceBrapi.CSV.equalsIgnoreCase(format.trim())) {
				response.sendRedirect("/bmsapi/" + crop + "/brapi/v1/studies/" + studyDbId + "/table/csv");
				return new ResponseEntity<>(HttpStatus.OK);
			} else if (StudyResourceBrapi.TSV.equalsIgnoreCase(format.trim())) {

				response.sendRedirect("/bmsapi/" + crop + "/brapi/v1/studies/" + studyDbId + "/table/tsv");

				return new ResponseEntity<>(HttpStatus.OK);
			} else {
				final List<Map<String, String>> status = Collections.singletonList(ImmutableMap.of("message", "Incorrect format"));
				final Metadata metadata = new Metadata(null, status);
				final StudyObservations observations = new StudyObservations().setMetadata(metadata);
				return new ResponseEntity<>(observations, HttpStatus.NOT_FOUND);
			}
		}
		return new ResponseEntity<>(this.getStudyObservations(studyDbId), HttpStatus.OK);
	}

	private StudyObservations getStudyObservations(final int studyDbId) throws Exception {
		StudyObservationTable studyObservationsTable = new StudyObservationTable();

		final Integer trialDbId = this.studyDataManager.getProjectIdByStudyDbId(studyDbId);

		if (trialDbId == null) {
			throw new Exception("studyDbId " + studyDbId + " does not exist");
		}

		final TrialObservationTable trialObservationTable = this.studyService.getTrialObservationTable(trialDbId, studyDbId);

		final int resultNumber = trialObservationTable == null ? 0 : 1;

		if (resultNumber != 0) {
			final ModelMapper modelMapper = new ModelMapper();
			studyObservationsTable = modelMapper.map(trialObservationTable, StudyObservationTable.class);
		}

		final Pagination pagination =
			new Pagination().withPageNumber(1).withPageSize(resultNumber).withTotalCount((long) resultNumber).withTotalPages(1);

		final Metadata metadata = new Metadata().withPagination(pagination);
		final StudyObservations studyObservations = new StudyObservations().setMetadata(metadata).setResult(studyObservationsTable);

		return studyObservations;
	}

	@ApiOperation(value = "Get study details", notes = "Get study details")
	@RequestMapping(value = "/{crop}/brapi/v1/studies/{studyDbId}", method = RequestMethod.GET)
	public ResponseEntity<StudyDetails> getStudyDetails(@PathVariable final String crop, @PathVariable final Integer studyDbId) {

		final StudyDetailsDto mwStudyDetails = this.studyService.getStudyDetailsForGeolocation(studyDbId);

		if (mwStudyDetails != null) {
			final StudyDetails studyDetails = new StudyDetails();
			final Metadata metadata = new Metadata();
			final Pagination pagination = new Pagination().withPageNumber(1).withPageSize(1).withTotalCount(1L).withTotalPages(1);
			metadata.setPagination(pagination);
			metadata.setStatus(Collections.singletonList(new HashMap<>()));
			studyDetails.setMetadata(metadata);
			final ModelMapper studyMapper = StudyMapper.getInstance();
			final StudyDetailsData result = studyMapper.map(mwStudyDetails, StudyDetailsData.class);

			if (mwStudyDetails.getMetadata().getLocationId() != null) {
				final Map<LocationFilters, Object> filters = new EnumMap<>(LocationFilters.class);
				filters.put(LocationFilters.LOCATION_ID, String.valueOf(mwStudyDetails.getMetadata().getLocationId()));
				final List<LocationDetailsDto> locations = this.locationDataManager.getLocationsByFilter(0, 1, filters);
				if (!locations.isEmpty()) {
					final ModelMapper locationMapper = LocationMapper.getInstance();
					final Location location = locationMapper.map(locations.get(0), Location.class);
					result.setLocation(location);
				}
			}
			studyDetails.setResult(result);

			return ResponseEntity.ok(studyDetails);
		} else {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}

	}

	@ApiOperation(value = "", hidden = true)
	@RequestMapping(value = "/{crop}/brapi/v1/studies/{studyDbId}/table/csv", method = RequestMethod.GET)
	private ResponseEntity<FileSystemResource> streamCSV(@PathVariable final String crop, @PathVariable final Integer studyDbId)
		throws Exception {

		final File file = this.createDownloadFile(this.getStudyObservations(studyDbId).getResult(), ',', "studyObservations.csv");
		return StudyResourceBrapi.createResponseEntityForFileDownload(file);
	}

	@ApiOperation(value = "", hidden = true)
	@RequestMapping(value = "/{crop}/brapi/v1/studies/{studyDbId}/table/tsv", method = RequestMethod.GET)
	private ResponseEntity<FileSystemResource> streamTSV(@PathVariable final String crop, @PathVariable final Integer studyDbId)
		throws Exception {
		final File file = this.createDownloadFile(this.getStudyObservations(studyDbId).getResult(), '\t', "studyObservations.tsv");

		return StudyResourceBrapi.createResponseEntityForFileDownload(file);
	}

	private File createDownloadFile(final StudyObservationTable table, final char sep, final String pathname) throws IOException {
		// create mapper and schema
		final CsvMapper mapper = new CsvMapper();
		CsvSchema schema = mapper.schemaFor(List.class);
		schema = schema.withColumnSeparator(sep);

		// output writer
		final ObjectWriter myObjectWriter = mapper.writer(schema);
		final File resultFile = new File(pathname);
		final List<String> header = new ArrayList<>();

		for (final String headerName : table.getHeaderRow()) {
			header.add(headerName);
		}

		final Object[] variableIds = table.getObservationVariableDbIds().toArray();
		final Object[] variableNames = table.getObservationVariableNames().toArray();
		for (int i = 0; i < variableIds.length; i++) {
			header.add(variableNames[i] + "|" + variableIds[i]);
		}

		final List<List<String>> data = table.getData();
		data.add(0, header);

		mapper.writeValue(resultFile, data);

		final FileOutputStream tempFileOutputStream = new FileOutputStream(resultFile);
		final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(tempFileOutputStream, 1024);
		final OutputStreamWriter writerOutputStream = new OutputStreamWriter(bufferedOutputStream, StandardCharsets.UTF_8);

		myObjectWriter.writeValue(writerOutputStream, data);
		return resultFile;
	}

	/**
	 * Creates ResponseEntity to download a file from a controller.
	 *
	 * @param file - file to be downloaded
	 * @return
	 */
	private static ResponseEntity<FileSystemResource> createResponseEntityForFileDownload(final File file)
		throws UnsupportedEncodingException {

		final String filename = file.getName();
		final String fileWithFullPath = file.getAbsolutePath();
		final HttpHeaders respHeaders = new HttpHeaders();

		final File resource = new File(fileWithFullPath);
		final FileSystemResource fileSystemResource = new FileSystemResource(resource);

		final String mimeType = FileUtils.detectMimeType(filename);
		final String sanitizedFilename = FileUtils.sanitizeFileName(filename);

		respHeaders.set(StudyResourceBrapi.CONTENT_TYPE, String.format("%s;charset=utf-8", mimeType));
		respHeaders.set(StudyResourceBrapi.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"; filename*=utf-8\'\'%s",
			sanitizedFilename, FileUtils.encodeFilenameForDownload(sanitizedFilename)));

		return new ResponseEntity<>(fileSystemResource, respHeaders, HttpStatus.OK);

	}
}
