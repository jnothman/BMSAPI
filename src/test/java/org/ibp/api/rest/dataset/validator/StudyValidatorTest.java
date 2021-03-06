package org.ibp.api.rest.dataset.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ForbiddenException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.UserTestDataGenerator;
import org.ibp.api.java.impl.middleware.dataset.validator.StudyValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Random;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class StudyValidatorTest {

	private static final int USER_ID = 10;
	private static final String PROGRAM_UUID = RandomStringUtils.randomAlphabetic(10);
	@Mock
	private SecurityService securityService;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private org.generationcp.middleware.service.api.study.StudyInstanceService studyInstanceMiddlewareService;

	@InjectMocks
	private StudyValidator studyValidator;

	@Before
	public void setup() {
		ContextHolder.setCurrentProgram(PROGRAM_UUID);
		ContextHolder.setCurrentCrop("maize");
	}

	@Test (expected = ResourceNotFoundException.class)
	public void testStudyDoesNotExist() {
		final Random ran = new Random();
		final int studyId = ran.nextInt();
		Mockito.when(this.studyDataManager.getStudy(studyId)).thenReturn(null);
		this.studyValidator.validate(studyId, ran.nextBoolean());
	}

	@Test (expected = ForbiddenException.class)
	public void testStudyIsLocked() {
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(USER_ID, new Role(2, "Breeder"));
		doReturn(user).when(this.securityService).getCurrentlyLoggedInUser();
		final Random ran = new Random();
		final int studyId = ran.nextInt();
		final Study study = new Study();
		study.setId(studyId);
		study.setLocked(true);
		study.setCreatedBy("1");
		Mockito.when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		this.studyValidator.validate(studyId, true);
	}

	@Test
	public void testStudyIsLockedButUserIsOwner() {
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(USER_ID, new Role(2, "Breeder"));
		doReturn(user).when(this.securityService).getCurrentlyLoggedInUser();
		final Random ran = new Random();
		final int studyId = ran.nextInt();
		final Study study = new Study();
		study.setId(studyId);
		study.setLocked(true);
		study.setCreatedBy(String.valueOf(USER_ID));
		study.setProgramUUID(PROGRAM_UUID);
		Mockito.when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		this.studyValidator.validate(studyId, true);
	}

	@Test
	public void testStudyIsLockedButUserIsSuperAdmin() {
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(USER_ID, new Role(2, Role.SUPERADMIN));
		doReturn(user).when(this.securityService).getCurrentlyLoggedInUser();

		final Random ran = new Random();
		final int studyId = ran.nextInt();
		final Study study = new Study();
		study.setId(studyId);
		study.setLocked(true);
		study.setCreatedBy("1");
		study.setProgramUUID(PROGRAM_UUID);

		Mockito.when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		this.studyValidator.validate(studyId, true);
	}

	@Test
	public void testStudyAllInstancesMustBeDeletableButOneIsNot() {
		final Random ran = new Random();
		final int studyId = ran.nextInt();
		final Study study = new Study();
		study.setId(studyId);
		study.setLocked(false);
		study.setCreatedBy("1");
		study.setProgramUUID(PROGRAM_UUID);

		Mockito.when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		final StudyInstance studyInstance = new StudyInstance(ran.nextInt(), ran.nextInt(), ran.nextInt(),
			RandomStringUtils.randomAlphabetic(10),
			RandomStringUtils.randomAlphabetic(10),
			1,
			RandomStringUtils.randomAlphabetic(10), false);
		studyInstance.setCanBeDeleted(false);
		final StudyInstance studyInstance2 = new StudyInstance(ran.nextInt(), ran.nextInt(), ran.nextInt(),
			RandomStringUtils.randomAlphabetic(10),
			RandomStringUtils.randomAlphabetic(10),
			1,
			RandomStringUtils.randomAlphabetic(10), false);
		studyInstance2.setCanBeDeleted(true);
		Mockito.when(this.studyInstanceMiddlewareService.getStudyInstances(studyId))
			.thenReturn(Arrays.asList(studyInstance, studyInstance2));
		try {
			this.studyValidator.validate(studyId, ran.nextBoolean(), true);
			Assert.fail("Expected validation exception to be thrown but was not.");
		} catch (final ApiRequestValidationException e) {
			Assert.assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("at.least.one.instance.cannot.be.deleted"));
		}
	}

	@Test
	public void testStudyAllInstancesMustBeDeletableSuccess() {
		final Random ran = new Random();
		final int studyId = ran.nextInt();
		final Study study = new Study();
		study.setId(studyId);
		study.setLocked(false);
		study.setCreatedBy("1");
		study.setProgramUUID(PROGRAM_UUID);

		Mockito.when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		final StudyInstance studyInstance = new StudyInstance(ran.nextInt(), ran.nextInt(), ran.nextInt(),
			RandomStringUtils.randomAlphabetic(10),
			RandomStringUtils.randomAlphabetic(10),
			1,
			RandomStringUtils.randomAlphabetic(10), false);
		studyInstance.setCanBeDeleted(true);
		final StudyInstance studyInstance2 = new StudyInstance(ran.nextInt(), ran.nextInt(), ran.nextInt(),
			RandomStringUtils.randomAlphabetic(10),
			RandomStringUtils.randomAlphabetic(10),
			1,
			RandomStringUtils.randomAlphabetic(10), false);
		studyInstance2.setCanBeDeleted(true);
		Mockito.when(this.studyInstanceMiddlewareService.getStudyInstances(studyId))
			.thenReturn(Arrays.asList(studyInstance, studyInstance2));

		this.studyValidator.validate(studyId, ran.nextBoolean(), true);
	}

	@Test
	public void testStudyNotAllInstancesMustBeDeletable() {
		final Random ran = new Random();
		final int studyId = ran.nextInt();
		final Study study = new Study();
		study.setId(studyId);
		study.setLocked(false);
		study.setCreatedBy("1");
		study.setProgramUUID(PROGRAM_UUID);

		Mockito.when(this.studyDataManager.getStudy(studyId)).thenReturn(study);
		this.studyValidator.validate(studyId, ran.nextBoolean(), false);
		Mockito.verifyZeroInteractions(this.studyInstanceMiddlewareService);
	}




}
