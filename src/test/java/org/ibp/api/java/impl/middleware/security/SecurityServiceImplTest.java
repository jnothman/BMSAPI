package org.ibp.api.java.impl.middleware.security;

import com.google.common.collect.Lists;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.study.StudySummary;
import org.generationcp.middleware.service.api.user.UserService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;

public class SecurityServiceImplTest {

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@Mock
	private UserService userService;

	@Mock
	private HttpServletRequest httpServletRequest;

	@InjectMocks
	private final SecurityServiceImpl securityServiceImpl = new SecurityServiceImpl();

	private WorkbenchUser me;
	private WorkbenchUser otherBreeder;
	private UsernamePasswordAuthenticationToken loggedInUser;

	private final String programUUID = "fb0783d2-dc82-4db6-a36e-7554d3740092";
	private final String cropname = "maize";

	@Before
	public void beforeEachTest() {
		MockitoAnnotations.initMocks(this);

		this.me = new WorkbenchUser();
		this.me.setName("Mr. Breeder");
		this.me.setUserid(1);

		this.otherBreeder = new WorkbenchUser();
		this.otherBreeder.setName("Other Breeder");
		this.otherBreeder.setUserid(2);

		this.loggedInUser = new UsernamePasswordAuthenticationToken(this.me.getName(), this.me.getPassword());
		SecurityContextHolder.getContext().setAuthentication(this.loggedInUser);

		Mockito.when(this.userService.getUserById(this.me.getUserid())).thenReturn(this.me);
		Mockito.when(this.userService.getUserById(this.otherBreeder.getUserid())).thenReturn(this.otherBreeder);

		Mockito.when(this.userService.getUserByUsername(this.me.getName())).thenReturn(this.me);
		Mockito.when(this.userService.getUserByUsername(this.otherBreeder.getName())).thenReturn(this.otherBreeder);
	}

	@After
	public void afterEachTest() {
		SecurityContextHolder.getContext().setAuthentication(null);
	}

	/**
	 * Case 1 Study in a program that logged in user is member of.
	 */
	@Test
	public void testIsAccessibleStudySummaryMine() {

		final StudySummary summaryStudy = new StudySummary();
		summaryStudy.setProgramUUID(this.programUUID);

		final Project summaryStudyProgram = new Project();
		summaryStudyProgram.setProjectId(2L);
		summaryStudyProgram.setUniqueID(summaryStudy.getProgramUUID());

		Mockito.when(this.workbenchDataManager.getProjectByUuidAndCrop(summaryStudy.getProgramUUID(), this.cropname))
			.thenReturn(summaryStudyProgram);

		// Logged in user = me is a the member
		Mockito.when(this.userService.getUsersByProjectId(summaryStudyProgram.getProjectId())).thenReturn(
			Lists.newArrayList(this.me));

		// Hence accessible
		Assert.assertTrue(
			"Studies that are part of programs created by me, or I am a meber of, should be accessible to me.",
			this.securityServiceImpl.isAccessible(summaryStudy, this.cropname));
	}

	/**
	 * Case 2 Study in a program that logged in user is NOT member of.
	 */
	@Test
	public void testIsAccessibleStudySummaryOthers() {

		final StudySummary summaryStudy = new StudySummary();
		summaryStudy.setProgramUUID(this.programUUID);

		final Project summaryStudyProgram = new Project();
		summaryStudyProgram.setProjectId(2L);
		summaryStudyProgram.setUniqueID(summaryStudy.getProgramUUID());

		Mockito.when(this.workbenchDataManager.getProjectByUuidAndCrop(summaryStudy.getProgramUUID(), this.cropname))
			.thenReturn(summaryStudyProgram);

		// Logged in user = me is not the member, some other breeder is
		Mockito.when(this.userService.getUsersByProjectId(summaryStudyProgram.getProjectId())).thenReturn(

			Lists.newArrayList(this.otherBreeder));

		// Hence not accessible
		Assert.assertFalse(
			"Studies that are part of programs I have not created nor I am a member of,  should not be accessible to me.",
			this.securityServiceImpl.isAccessible(summaryStudy, this.cropname));
	}

	/**
	 * Case 3 Study templates case where there is no program uuid
	 */
	@Test
	public void testIsAccessibleStudySummaryTemplates() {
		final StudySummary summary = new StudySummary();
		summary.setProgramUUID(null);

		// Accessible to all
		Assert.assertTrue(
			"Studies with no pgoram reference (e.g. Templates) should be accessible to all.",
			this.securityServiceImpl.isAccessible(summary, this.cropname));
	}

	@Test
	public void testGetCurrentlyLoggedInUser() {
		// We setup authentication context in setup method.
		Assert.assertEquals(this.me, this.securityServiceImpl.getCurrentlyLoggedInUser());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetCurrentlyLoggedInUserErrorCase() {
		// We setup authentication context in setup method.
		// We want that cleared for this test case.
		SecurityContextHolder.getContext().setAuthentication(null);
		this.securityServiceImpl.getCurrentlyLoggedInUser();
	}

	/**
	 * Case 1 Logged in user is the list owner.
	 */
	@Test
	public void testGermplasmListIsAccessibleToOwner() {
		final GermplasmList list = new GermplasmList();
		list.setUserId(this.me.getUserid());
		Mockito.when(this.userService.getUserById(this.me.getUserid())).thenReturn(this.me);
		Assert
			.assertTrue("Lists owned by logged in user should be accessible.", this.securityServiceImpl.isAccessible(list, this.cropname));
	}

	/**
	 * Case 2 Logged in user is not the list owner.
	 */
	@Test
	public void testGermplasmListIsNotAccessibleIfNotOwner() {
		final GermplasmList list = new GermplasmList();
		list.setUserId(this.otherBreeder.getUserid());
		list.setProgramUUID(this.programUUID);

		final Project listProgram = new Project();
		listProgram.setProjectId(2L);
		listProgram.setUniqueID(list.getProgramUUID());
		Mockito.when(this.workbenchDataManager.getProjectByUuidAndCrop(list.getProgramUUID(), this.cropname)).thenReturn(listProgram);
		// Logged in user = me is not a the member
		Mockito.when(this.userService.getUsersByProjectId(listProgram.getProjectId())).thenReturn(
			Lists.newArrayList(this.otherBreeder));

		Mockito.when(this.userService.getUserById(this.otherBreeder.getUserid())).thenReturn(this.otherBreeder);
		Assert.assertFalse("Lists not owned by logged in user should not be accessible.",
			this.securityServiceImpl.isAccessible(list, this.cropname));
	}

	/**
	 * Case 3 Logged in user is not the list owner but is member of program where list belongs.
	 */
	@Test
	public void testGermplasmListIsAccessibleToProgramMembers() {
		final GermplasmList list = new GermplasmList();
		list.setUserId(this.otherBreeder.getUserid());
		list.setProgramUUID(this.programUUID);

		Mockito.when(this.userService.getUserById(this.otherBreeder.getUserid())).thenReturn(this.otherBreeder);

		final Project listProgram = new Project();
		listProgram.setProjectId(2L);
		listProgram.setUniqueID(list.getProgramUUID());

		Mockito.when(this.workbenchDataManager.getProjectByUuidAndCrop(list.getProgramUUID(), this.cropname)).thenReturn(listProgram);

		// Logged in user = me is a the member
		Mockito.when(this.userService.getUsersByProjectId(listProgram.getProjectId())).thenReturn(
			Lists.newArrayList(this.me));

		Assert.assertTrue(
			"Lists which are part of programs that logged in user is member of, should be accessible.",
			this.securityServiceImpl.isAccessible(list, this.cropname));
	}

	/**
	 * Case 4 Lists with no program reference.
	 */
	@Test
	public void testGermplasmListIsAccessibleIfNoProgramReference() {
		final GermplasmList list = new GermplasmList();
		list.setProgramUUID(null);
		Assert.assertTrue("Lists with no program reference should be accessible to all.",
			this.securityServiceImpl.isAccessible(list, this.cropname));
	}

}
