package org.ibp.api.rest.sample;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.service.CsvExportSampleListService;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.generationcp.middleware.domain.samplelist.SampleListDTO;
import org.generationcp.middleware.enumeration.SampleListType;
import org.generationcp.middleware.pojos.SampleList;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.SampleListService;
import org.generationcp.middleware.service.impl.study.SamplePlateInfo;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionWithSize;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.impl.middleware.security.SecurityServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;


public class SampleListResourceTest extends ApiUnitTestBase {

	private static final String ADMIN = "admin";
	private static final String DESCRIPTION = "description";
	private static final String NOTES = "Notes";
	private static final String VALUE = "1";
	private static Locale locale = Locale.getDefault();

	private SampleListDto dto;
	private WorkbenchUser user;
	private String folderName;
	private Integer parentId;

	private static final SimpleDateFormat DATE_FORMAT = DateUtil.getSimpleDateFormat(DateUtil.FRONTEND_DATE_FORMAT_3);


	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public SampleListService service() {
			return Mockito.mock(SampleListService.class);
		}

		@Bean
		@Primary
		public CsvExportSampleListService getExportSampleListService() {
			return Mockito.mock(CsvExportSampleListService.class);
		}
	}


	@Autowired
	private SecurityServiceImpl securityService;

	@Autowired
	private org.generationcp.middleware.service.api.SampleListService sampleListServiceMW;

	@Autowired
	private CsvExportSampleListService csvExportSampleListService;

	@Resource
	private ResourceBundleMessageSource resourceBundleMessageSource;

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);
		this.dto = new SampleListDto();
		this.dto.setDescription(SampleListResourceTest.DESCRIPTION);
		this.dto.setNotes(SampleListResourceTest.NOTES);
		this.dto.setCreatedBy(SampleListResourceTest.ADMIN);
		this.dto.setSelectionVariableId(8263);
		final List<Integer> instanceIds = new ArrayList<>();
		instanceIds.add(1);
		this.dto.setInstanceIds(instanceIds);
		this.dto.setTakenBy(SampleListResourceTest.ADMIN);
		this.dto.setSamplingDate("2017-08-01");
		this.dto.setDatasetId(25025);
		this.dto.setCropName("maize");
		this.dto.setListName("SamplesTest");
		this.dto.setCreatedDate("2017-10-12");
		this.dto.setProgramUUID(this.programUuid);
		this.user = new WorkbenchUser();
		this.user.setName(SampleListResourceTest.ADMIN);

		this.folderName = "Folder Name";
		this.parentId = 1;

		ContextHolder.setCurrentCrop(this.cropName);
		ContextHolder.setCurrentProgram(this.programUuid);

	}

	@Test
	public void createNewSampleList() throws Exception {
		final HashMap<String, Object> result = new HashMap<>();
		result.put("id", SampleListResourceTest.VALUE);
		final SampleList sampleList = new SampleList();
		sampleList.setId(Integer.valueOf(SampleListResourceTest.VALUE));

		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(this.user);
		Mockito.when(this.sampleListServiceMW.createSampleList(org.mockito.Matchers.any(SampleListDTO.class))).thenReturn(sampleList);

		this.mockMvc.perform(MockMvcRequestBuilders.post("/crops/{crop}/sample-lists?programUUID=" + this.programUuid, this.cropName).contentType(this.contentType)
			.content(this.convertObjectToByte(this.dto))).andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(result.get("id"))));
	}

	@Test
	public void createSampleListFolder() throws Exception {
		final HashMap<String, Object> result = new HashMap<>();
		result.put("id", SampleListResourceTest.VALUE);
		final WorkbenchUser creatingBy = new WorkbenchUser();
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(creatingBy);
		Mockito.doReturn(Integer.valueOf(SampleListResourceTest.VALUE)).when(this.sampleListServiceMW).createSampleListFolder(org.mockito.Matchers.anyString(), org.mockito.Matchers.anyInt(),
			org.mockito.ArgumentMatchers.isNull(String.class), org.mockito.Matchers.anyString());

		this.mockMvc.perform(MockMvcRequestBuilders.post("/crops/{crop}/programs/{programUUID}/sample-list-folders?folderName="+ this.folderName + "&parentId=" + this.parentId, this.cropName, this.programUuid)).andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(result.get("id"))));
	}

	@Test
	public void updateSampleListFolder() throws Exception {
		final Integer folderId = 2;
		final Integer parentFolderId = 1;
		final String newFolderName = "NEW_NAME";
		final SampleList parentFolder = new SampleList();
		parentFolder.setId(parentFolderId);
		parentFolder.setType(SampleListType.FOLDER);
		final SampleList folder = new SampleList();
		folder.setId(folderId);
		folder.setHierarchy(parentFolder);
		folder.setType(SampleListType.FOLDER);

		Mockito.when(this.sampleListServiceMW.updateSampleListFolderName(org.mockito.Matchers.anyInt(), org.mockito.Matchers.anyString()))
			.thenReturn(folder);

		this.mockMvc.perform(MockMvcRequestBuilders.put("/crops/{crop}/programs/{programUUID}/sample-list-folders/{folderId}?newFolderName=" + newFolderName, this.cropName, this.programUuid, folderId)).andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(folder.getId().toString())));
	}

	@Test
	public void moveSampleListFolder() throws Exception {
		final Integer folderId = 2;

		final Integer newParentFolderId = 3;

		final SampleList parentFolder = new SampleList();
		parentFolder.setId(newParentFolderId);
		parentFolder.setType(SampleListType.FOLDER);

		final SampleList folder = new SampleList();
		folder.setId(folderId);
		folder.setHierarchy(parentFolder);
		folder.setType(SampleListType.FOLDER);

		Mockito.when(this.sampleListServiceMW.moveSampleList(folderId, newParentFolderId, false, programUuid)).thenReturn(folder);

		this.mockMvc.perform(MockMvcRequestBuilders.put("/crops/{crop}/programs/{programUUID}/sample-list-folders/{folderId}/move?newParentId="+ newParentFolderId+ "&isCropList=false", this.cropName, this.programUuid, folderId)).andExpect(MockMvcResultMatchers.status().isOk())
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$.parentId", Matchers.is(folder.getHierarchy().getId().toString())));
	}

	@Test
	public void deleteSampleListFolder() throws Exception {
		final Integer folderId = 2;

		Mockito.doAnswer(new Answer<Void>() {

			@Override
			public Void answer(final InvocationOnMock invocation) {
				return null;
			}
		}).when(this.sampleListServiceMW).deleteSampleListFolder(folderId);

		this.mockMvc.perform(MockMvcRequestBuilders.delete("/crops/{crop}/programs/{programUUID}/sample-list-folders/{folderId}", this.cropName, this.programUuid, folderId))
			.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print());
	}

	@Test
	public void testSearch() throws Exception {

		final String searchString = "ListName1";

		final List<SampleList> list = new ArrayList<>();
		final SampleList sampleList = new SampleList();
		sampleList.setId(1);
		sampleList.setListName(searchString);
		sampleList.setDescription("Description");
		list.add(sampleList);

		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(this.user);
		Mockito.doReturn(list).when(sampleListServiceMW
			).searchSampleLists(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
				Mockito.any(Pageable.class));

		this.mockMvc.perform(
			MockMvcRequestBuilders.get("/crops/{crop}/sample-lists/search?programUUID=" + this.programUuid, this.cropName)
				.param("exactMatch", "false")
				.param("searchString", searchString)
				.contentType(this.contentType).content(this.convertObjectToByte(this.dto)))
			.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(list.size())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].listName", Matchers.is(sampleList.getListName())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].description", Matchers.is(sampleList.getDescription())))
			.andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(sampleList.getId())));

	}

	@Test
	public void testSampleListDownload() throws Exception {

		final Integer listId = 1;
		final String listName = "listName";
		final String fileName = listName + ".csv";

		final File temporaryFile = new File(fileName);
		try {

			FileUtils.writeStringToFile(temporaryFile, "test");

			final FileExportInfo fileExportInfo = new FileExportInfo();
			fileExportInfo.setFilePath(temporaryFile.getAbsolutePath());
			fileExportInfo.setDownloadFileName(fileName);

			Mockito.when(this.csvExportSampleListService.export(Mockito.anyListOf(SampleDetailsDTO.class), Mockito.anyString(), Mockito.anyListOf(
					String.class), Mockito.anyString()))
				.thenReturn(fileExportInfo);

			this.mockMvc.perform(MockMvcRequestBuilders.get("/crops/{crop}/sample-lists/{listId}/download?programUUID=" + this.programUuid + "&listName=" + listName, this.cropName, listId)
				.contentType(this.csvContentType)).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

		} finally {
			temporaryFile.delete();
		}

	}

	@Test
	public void testImportPlateInformation() throws Exception {
		final Random random = new Random();
		final Integer listId = random.nextInt(10000);
		final String crop = "MAIZE";
		final List<SampleDTO> sampleDTOs = createSampleDto(2);
		Mockito.when(this.sampleListServiceMW.countSamplesByUIDs(ArgumentMatchers.<Set<String>>any(),ArgumentMatchers.any(Integer.class))).thenReturn(2l);

		this.mockMvc.perform(MockMvcRequestBuilders.patch("/crops/{crop}/sample-lists/{listId}/samples?programUUID=" +  this.programUuid, crop, listId)
			.content(this.convertObjectToByte(sampleDTOs))
			.contentType(this.contentType)).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

		Mockito.verify(this.sampleListServiceMW).updateSamplePlateInfo(ArgumentMatchers.any(Integer.class), ArgumentMatchers.<String, SamplePlateInfo>anyMap());

	}

	@Test

	public void testImportPlateInformation_NoSampleId() throws Exception {
		final Random random = new Random();
		final Integer listId = random.nextInt(10000);
		final String crop = "MAIZE";
		final List<SampleDTO> sampleDTOs = createSampleDto(1);
		sampleDTOs.get(0).setSampleBusinessKey(null);

		Mockito.when(this.sampleListServiceMW.countSamplesByUIDs(ArgumentMatchers.<Set<String>>any(), Mockito.anyInt())).thenReturn(2l);
		this.mockMvc.perform(MockMvcRequestBuilders.patch("/crops/{crop}/sample-lists/{listId}/samples?programUUID=" +  this.programUuid, crop, listId)
			.content(this.convertObjectToByte(sampleDTOs))
			//
			.contentType(this.contentType) //
			.locale(locale) //
			.content(this.convertObjectToByte(sampleDTOs))) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors", is(not(empty())))) //
			.andExpect(MockMvcResultMatchers
				.jsonPath("$.errors[0].message", is(this.getMessage("sample.record.not.include.sample.id.in.file")))) //
		;
	}

	@Test
	public void testImportPlateInformation_plate_id_exceeded_max_length() throws Exception {
		final Random random = new Random();
		final Integer listId = random.nextInt(10000);
		final String crop = "MAIZE";
		final List<SampleDTO> sampleDTOs = createSampleDto(1);
		SampleDTO sampleDTO = sampleDTOs.get(0);
		sampleDTO.setPlateId(RandomStringUtils.random(256));

		Mockito.when(this.sampleListServiceMW.countSamplesByUIDs(ArgumentMatchers.<Set<String>>any(), Mockito.anyInt())).thenReturn(2l);
		this.mockMvc.perform(MockMvcRequestBuilders.patch("/crops/{crop}/sample-lists/{listId}/samples?programUUID=" +  this.programUuid, crop, listId)
			.content(this.convertObjectToByte(sampleDTOs)) //
			.contentType(this.contentType) //
			.locale(locale) //
			.content(this.convertObjectToByte(sampleDTOs))) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors", is(not(empty())))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", is(this.getMessage("sample.plate.id.exceed.length")))) //
		;
	}

	@Test
	public void testImportPlateInformation_Well_exceeded_max_length() throws Exception {
		final Random random = new Random();
		final Integer listId = random.nextInt(10000);
		final String crop = "MAIZE";
		final List<SampleDTO> sampleDTOs = createSampleDto(1);
		SampleDTO sampleDTO = sampleDTOs.get(0);
		sampleDTO.setWell(RandomStringUtils.random(256));

		this.mockMvc.perform(MockMvcRequestBuilders.patch("/crops/{crop}/sample-lists/{listId}/samples?programUUID=" +  this.programUuid, crop, listId)
			.content(this.convertObjectToByte(sampleDTOs)) //
			.contentType(this.contentType) //
			.locale(locale) //
			.content(this.convertObjectToByte(sampleDTOs))) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors", is(not(empty())))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", is(this.getMessage("sample.well.exceed.length")))) //
		;
	}

	@Test
	public void testImportPlateInformation_SampleId_is_repeated_in_the_SampleList() throws Exception {
		final Random random = new Random();
		final Integer listId = random.nextInt(10000);
		final String crop = "MAIZE";
		final List<SampleDTO> sampleDTOs = createSampleDto(2);
		SampleDTO sampleDTO = sampleDTOs.get(1);
		sampleDTO.setSampleBusinessKey(sampleDTOs.get(0).getSampleBusinessKey());
		this.mockMvc.perform(MockMvcRequestBuilders.patch("/crops/{crop}/sample-lists/{listId}/samples?programUUID=" +  this.programUuid, crop, listId)
			.content(this.convertObjectToByte(sampleDTOs)) //
			.contentType(this.contentType) //
			.locale(locale)) //
			.andDo(MockMvcResultHandlers.print()) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors", is(not(empty())))) //
			.andExpect(MockMvcResultMatchers.jsonPath("$.errors[0].message", is(this.getMessage("sample.id.repeat.in.file")))) //
		;
	}

	private String getMessage(final String code) {
		return this.resourceBundleMessageSource.getMessage(code, null, LocaleContextHolder.getLocale());
	}

	private String getMessage(final String code, final Object[] arguments) {
		return this.resourceBundleMessageSource.getMessage(code, arguments, LocaleContextHolder.getLocale());
	}

	private List<SampleDTO> createSampleDto(final int numeberOfSamples) {
		final List<SampleDTO> sampleDTOs = new ArrayList<>();

		for (int i = 0; i < numeberOfSamples; i++) {
			SampleDTO sampleDTO = new SampleDTO();
			sampleDTO.setSampleBusinessKey("SampleId-" + i);
			sampleDTO.setPlateId("TestValue-" + i);
			sampleDTO.setWell("TestValue-" + i);
			sampleDTOs.add(sampleDTO);
		}
		return sampleDTOs;

	}

}
