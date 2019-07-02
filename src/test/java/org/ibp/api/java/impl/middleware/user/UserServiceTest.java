
package org.ibp.api.java.impl.middleware.user;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.UserDto;
import org.ibp.api.domain.common.ErrorResponse;
import org.ibp.api.domain.user.UserDetailDto;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.UserTestDataGenerator;
import org.ibp.api.java.impl.middleware.manager.UserValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

	private UserServiceImpl userServiceImpl;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@Mock
	private SecurityService securityService;

	private final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

	@InjectMocks
	protected UserValidator userValidator;

	private static final String projectUUID = "d8d59d89-f4ca-4b83-90e2-be2d82407146";

	@Before
	public void beforeEachTest() {
		// We cannot mock resourceBundleMessageSource.getMessage because the method is marked as final.
		// So as a workaround, use a real class instance and set setUseCodeAsDefaultMessage to true
		this.messageSource.setUseCodeAsDefaultMessage(true);
		MockitoAnnotations.initMocks(this);
		this.userServiceImpl = new UserServiceImpl();
		this.userServiceImpl.setWorkbenchDataManager(this.workbenchDataManager);
		this.userValidator.setWorkbenchDataManager(this.workbenchDataManager);
		this.userServiceImpl.setUserValidator(this.userValidator);
		this.userServiceImpl.setSecurityService(this.securityService);
		this.userServiceImpl.setMessageSource(this.messageSource);

		Mockito.doReturn(null).when(this.workbenchDataManager).getProjectsByUser(ArgumentMatchers.any(WorkbenchUser.class), ArgumentMatchers.any(String.class));
	}

	/**
	 * Should return the list of users.
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetAllUsers() throws Exception {
		final List<UserDto> usersDto = UserTestDataGenerator.getAllListUser();

		Mockito.when(this.workbenchDataManager.getAllUsersSortedByLastName()).thenReturn(usersDto);
		final List<UserDetailDto> usersDtlsDto = this.userServiceImpl.getAllUsersSortedByLastName();

		Mockito.verify(this.securityService).requireCurrentUserIsAdmin();
		assertThat(usersDto.get(0).getFirstName(), equalTo(usersDtlsDto.get(0).getFirstName()));
		assertThat(usersDto.get(0).getLastName(), equalTo(usersDtlsDto.get(0).getLastName()));
		assertThat(usersDto.get(0).getUsername(), equalTo(usersDtlsDto.get(0).getUsername()));
		assertThat(usersDto.get(0).getUserId(), equalTo(usersDtlsDto.get(0).getId()));
		assertThat(usersDto.get(0).getEmail(), equalTo(usersDtlsDto.get(0).getEmail()));
		assertThat("true", equalTo(usersDtlsDto.get(0).getStatus()));
		assertThat(usersDto.get(0).getUserRoles().get(0).getRole().getName(),
			equalTo(usersDtlsDto.get(0).getUserRoles().get(0).getRole().getName()));
	}

	/**
	 * Should create the new user, and return the new userId.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateUser() throws Exception {
		final UserDetailDto usrDtlDto = UserTestDataGenerator.initializeUserDetailDto(0);
		final UserDto userDto = UserTestDataGenerator.initializeUserDto(0);

		Mockito.when(this.workbenchDataManager.createUser(userDto)).thenReturn(new Integer(7));

		final Map<String, Object> mapResponse = this.userServiceImpl.createUser(usrDtlDto);

		Mockito.verify(this.securityService).requireCurrentUserIsAdmin();
		assertThat((String) mapResponse.get("id"), equalTo("7"));
	}

	/**
	 * Should return the id 0, because happened a error during the creation user.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateUserError() throws Exception {
		final UserDetailDto usrDtlDto = UserTestDataGenerator.initializeUserDetailDto(0);
		final UserDto userDto = UserTestDataGenerator.initializeUserDto(0);

		Mockito.when(this.workbenchDataManager.isUsernameExists(usrDtlDto.getUsername())).thenReturn(false);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(usrDtlDto.getEmail())).thenReturn(false);
		Mockito.when(this.workbenchDataManager.createUser(userDto)).thenThrow(new MiddlewareQueryException(
			"Error encountered while saving User: UserDataManager.addUser(user=" + userDto.getUsername() + "): "));

		final Map<String, Object> mapResponse = this.userServiceImpl.createUser(usrDtlDto);
		final ErrorResponse error = (ErrorResponse) mapResponse.get("ERROR");

		Mockito.verify(this.securityService).requireCurrentUserIsAdmin();
		assertThat((String) mapResponse.get("id"), equalTo("0"));
		assertThat(error.getErrors().size(), equalTo(1));
		assertThat(error.getErrors().get(0).getFieldNames()[0], equalTo("userId"));
		assertThat(error.getErrors().get(0).getMessage(), equalTo("DB error"));
	}

	/**
	 * Should return the id 0, because happened a error during the creation user.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateUserValidateError() throws Exception {
		final UserDetailDto usrDtlDto = UserTestDataGenerator.initializeUserDetailDto(0);

		Mockito.when(this.workbenchDataManager.isUsernameExists(usrDtlDto.getUsername())).thenReturn(true);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(usrDtlDto.getEmail())).thenReturn(true);

		final Map<String, Object> mapResponse = this.userServiceImpl.createUser(usrDtlDto);
		final ErrorResponse error = (ErrorResponse) mapResponse.get("ERROR");

		Mockito.verify(this.securityService).requireCurrentUserIsAdmin();
		assertThat((String) mapResponse.get("id"), equalTo("0"));
		assertThat(error.getErrors().size(), equalTo(2));
	}

	/**
	 * Should update the user, and return the same userId.
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateUser() throws Exception {
		final UserDetailDto usrDtlDto = UserTestDataGenerator.initializeUserDetailDto(8);
		final UserDto userDto = UserTestDataGenerator.initializeUserDto(8);
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(8);

		Mockito.when(this.workbenchDataManager.updateUser(userDto)).thenReturn(new Integer(8));
		Mockito.when(this.workbenchDataManager.getUserById(8)).thenReturn(user);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		final Map<String, Object> mapResponse = this.userServiceImpl.updateUser(usrDtlDto);

		Mockito.verify(this.securityService).requireCurrentUserIsAdmin();
		assertThat((String) mapResponse.get("id"), equalTo("8"));
	}

	/**
	 * Should return the id 0, because happened a error during the update user.
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateUserValidateError() throws Exception {
		final UserDetailDto usrDtlDto = UserTestDataGenerator.initializeUserDetailDto(10);
		final WorkbenchUser usr = UserTestDataGenerator.initializeWorkbenchUser(10);
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(8);

		usr.getPerson().setEmail("diego.nicolas.cuenya@leafnode.io");
		Mockito.when(this.workbenchDataManager.getUserById(usrDtlDto.getId())).thenReturn(usr);
		Mockito.when(this.workbenchDataManager.isUsernameExists(usrDtlDto.getUsername())).thenReturn(false);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(usrDtlDto.getEmail())).thenReturn(true);
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		final Map<String, Object> mapResponse = this.userServiceImpl.updateUser(usrDtlDto);
		final ErrorResponse error = (ErrorResponse) mapResponse.get("ERROR");

		Mockito.verify(this.securityService).requireCurrentUserIsAdmin();
		assertThat((String) mapResponse.get("id"), equalTo("0"));
		assertThat(error.getErrors().size(), equalTo(1));
	}

	/**
	 * Should return the id 0, because happened a error during the update user.
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateUserError() throws Exception {
		final UserDetailDto usrDtlDto = UserTestDataGenerator.initializeUserDetailDto(10);
		final WorkbenchUser usr = UserTestDataGenerator.initializeWorkbenchUser(10);
		final UserDto userDto = UserTestDataGenerator.initializeUserDto(10);
		final WorkbenchUser user = UserTestDataGenerator.initializeWorkbenchUser(8);

		usr.getPerson().setEmail("diego.nicolas.cuenya@leafnode.io");
		Mockito.when(this.workbenchDataManager.getUserById(usrDtlDto.getId())).thenReturn(usr);
		Mockito.when(this.workbenchDataManager.isUsernameExists(usrDtlDto.getUsername())).thenReturn(false);
		Mockito.when(this.workbenchDataManager.isPersonWithEmailExists(usrDtlDto.getEmail())).thenReturn(false);
		Mockito.when(this.workbenchDataManager.updateUser(userDto)).thenThrow(new MiddlewareQueryException(
			"Error encountered while saving User: UserDataManager.addUser(user=" + userDto.getUsername() + "): "));
		Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(user);

		final Map<String, Object> mapResponse = this.userServiceImpl.updateUser(usrDtlDto);
		final ErrorResponse error = (ErrorResponse) mapResponse.get("ERROR");

		Mockito.verify(this.securityService).requireCurrentUserIsAdmin();
		assertThat((String) mapResponse.get("id"), equalTo("0"));
		assertThat(error.getErrors().size(), equalTo(1));
		assertThat(error.getErrors().get(0).getFieldNames()[0], equalTo("userId"));
		assertThat(error.getErrors().get(0).getMessage(), equalTo("DB error"));
	}

	/**
	 * Should return a map with a List of UserDetailDto by the USER Key.
	 *
	 * @throws Exception
	 */
	@Test
	public void getUsersByProjectUUID() throws Exception {
		final List<UserDto> usersDto = UserTestDataGenerator.getAllListUser();
		final String cropName = "maize";
		Mockito.when(this.workbenchDataManager.getUsersByProjectUuid(projectUUID, cropName)).thenReturn(usersDto);
		final List<UserDetailDto> userDetailDtoList = this.userServiceImpl.getUsersByProjectUUID(projectUUID);
		assertThat(usersDto.get(0).getFirstName(), equalTo(userDetailDtoList.get(0).getFirstName()));
		assertThat(usersDto.get(0).getLastName(), equalTo(userDetailDtoList.get(0).getLastName()));
		assertThat(usersDto.get(0).getUsername(), equalTo(userDetailDtoList.get(0).getUsername()));
		assertThat(usersDto.get(0).getUserId(), equalTo(userDetailDtoList.get(0).getId()));
		assertThat(usersDto.get(0).getEmail(), equalTo(userDetailDtoList.get(0).getEmail()));
		assertThat("true", equalTo(userDetailDtoList.get(0).getStatus()));
	}

	/**
	 * Should return a map with a description by the ERROR Key.
	 *
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getUsersByProjectUUIDWithOutProjectUUID() throws Exception {
		this.userServiceImpl.getUsersByProjectUUID("");
	}

	/**
	 * Should return a map with a description by the ERROR Key.
	 *
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public void getUsersByProjectUUIDWithOutUsers() throws Exception {
		this.userServiceImpl.getUsersByProjectUUID(projectUUID);
	}

	/**
	 * Should return a map with a description by the ERROR Key.
	 *
	 * @throws Exception
	 */
	@Test(expected = ApiRuntimeException.class)
	public void getUsersByProjectUUIDErrorQuery() throws Exception {
		final String cropName = "maize";
		Mockito.when(this.workbenchDataManager.getUsersByProjectUuid(projectUUID, cropName))
			.thenThrow(new MiddlewareQueryException("Error in getUsersByProjectUUId()"));
		this.userServiceImpl.getUsersByProjectUUID(projectUUID);
	}

}
