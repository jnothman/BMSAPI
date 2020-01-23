
package org.ibp.api.security;

import com.google.common.collect.Lists;
import org.generationcp.middleware.domain.workbench.PermissionDto;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.pojos.workbench.UserRole;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.permission.PermissionService;
import org.generationcp.middleware.service.api.user.UserService;
import org.ibp.api.java.impl.middleware.common.ContextResolver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class WorkbenchUserDetailsServiceTest {

	private static final String TEST_USER = "testUser";
	public static final String CROP_NAME = "Maize";

	@Mock
	private UserService userService;

	@Mock
	private PermissionService permissionService;

	@Mock
	private ContextResolver contextResolver;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@InjectMocks
	private WorkbenchUserDetailsService service = new WorkbenchUserDetailsService();
	public static final String PROGRAM_UUID = "1234567";

	@Before
	public void setUp() {
		Mockito.when(this.contextResolver.resolveCropNameFromUrl()).thenReturn(CROP_NAME);
		Mockito.when(this.contextResolver.resolveProgramUuidFromRequest()).thenReturn(PROGRAM_UUID);

		final Project project = new Project();
		project.setProjectId(new Long(1));
		Mockito.when(this.workbenchDataManager.getProjectByUuid(PROGRAM_UUID)).thenReturn(project);
	}

	@Test
	public void testLoadUserByUserName() {
		try {
			List<WorkbenchUser> matchingUsers = new ArrayList<>();
			WorkbenchUser testUserWorkbench = new WorkbenchUser();
			testUserWorkbench.setName(WorkbenchUserDetailsServiceTest.TEST_USER);
			testUserWorkbench.setPassword("password");
			UserRole testUserRole = new UserRole(testUserWorkbench, new Role(1, "ADMIN"));
			testUserWorkbench.setRoles(Arrays.asList(testUserRole));
			matchingUsers.add(testUserWorkbench);

			Mockito.when(this.userService.getUserByName(WorkbenchUserDetailsServiceTest.TEST_USER, 0, 1, Operation.EQUAL))
					.thenReturn(matchingUsers);


			final PermissionDto permissionDto = new PermissionDto();
			permissionDto.setName("ADMIN");
			final List<PermissionDto> permissions = Lists.newArrayList(permissionDto);
			Mockito.when(this.permissionService.getPermissions(testUserWorkbench.getUserid(),CROP_NAME,1)).thenReturn(permissions);

			UserDetails userDetails = this.service.loadUserByUsername(WorkbenchUserDetailsServiceTest.TEST_USER);
			Assert.assertEquals(testUserWorkbench.getName(), userDetails.getUsername());
			Assert.assertEquals(testUserWorkbench.getPassword(), userDetails.getPassword());
			Assert.assertEquals(1, userDetails.getAuthorities().size());
			Assert.assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority(testUserRole.getRole().getCapitalizedRole())));
		} catch (MiddlewareQueryException e) {
			Assert.fail("Unexpected exception: " + e.getMessage());
		}
	}

	@Test
	public void testLoadUserByUsernameUTF8Support() throws Exception {
		String htmlEscaptedUTF8Username = "&#28900;&#29482;";
		String rawUTF8Username = "烤猪";

		List<WorkbenchUser> matchingUsers = new ArrayList<WorkbenchUser>();
		WorkbenchUser testUserWorkbench = new WorkbenchUser();
		testUserWorkbench.setName(rawUTF8Username);
		testUserWorkbench.setPassword("password");
		UserRole testUserRole = new UserRole(testUserWorkbench, new Role(1, "ADMIN"));
		testUserWorkbench.setRoles(Arrays.asList(testUserRole));
		matchingUsers.add(testUserWorkbench);

		Mockito.when(this.userService.getUserByName(rawUTF8Username, 0, 1, Operation.EQUAL)).thenReturn(matchingUsers);

		UserDetails userDetails = this.service.loadUserByUsername(htmlEscaptedUTF8Username);
		Assert.assertEquals(testUserWorkbench.getName(), userDetails.getUsername());
	}

	@Test(expected = UsernameNotFoundException.class)
	public void testLoadUserByNonExistantUserName() throws MiddlewareQueryException {
		Mockito.when(this.userService.getUserByName(WorkbenchUserDetailsServiceTest.TEST_USER, 0, 1, Operation.EQUAL)).thenReturn(
				Collections.<WorkbenchUser>emptyList());
		this.service.loadUserByUsername(WorkbenchUserDetailsServiceTest.TEST_USER);
	}

	@Test(expected = AuthenticationServiceException.class)
	public void testLoadUserDataAccessError() throws MiddlewareQueryException {
		Mockito.when(this.userService.getUserByName(WorkbenchUserDetailsServiceTest.TEST_USER, 0, 1, Operation.EQUAL)).thenThrow(
				new MiddlewareQueryException("Boom!"));
		this.service.loadUserByUsername(WorkbenchUserDetailsServiceTest.TEST_USER);
	}
}
