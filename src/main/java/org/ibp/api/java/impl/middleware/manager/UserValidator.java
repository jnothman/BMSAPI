package org.ibp.api.java.impl.middleware.manager;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.generationcp.middleware.domain.workbench.RoleType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.RoleSearchDto;
import org.generationcp.middleware.service.api.user.UserDto;
import org.generationcp.middleware.service.api.user.UserRoleDto;
import org.generationcp.middleware.service.api.user.UserService;
import org.ibp.api.domain.user.UserDetailDto;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class UserValidator implements Validator {

	private static final Logger LOG = LoggerFactory.getLogger(UserValidator.class);

	public static final String SIGNUP_FIELD_INVALID_EMAIL_FORMAT = "signup.field.email.invalid";
	public static final String SIGNUP_FIELD_REQUIRED = "signup.field.required";
	public static final String SIGNUP_FIELD_LENGTH_EXCEED = "signup.field.length.exceed";
	public static final String SIGNUP_FIELD_EMAIL_EXISTS = "signup.field.email.exists";
	public static final String SIGNUP_FIELD_USERNAME_EXISTS = "signup.field.username.exists";
	public static final String SIGNUP_FIELD_INVALID_ROLE = "signup.field.invalid.role";
	public static final String SIGNUP_FIELD_INVALID_STATUS = "signup.field.invalid.status";
	public static final String SIGNUP_FIELD_INVALID_USER_ID = "signup.field.invalid.userId";
	/**
	 * TODO move to properties
	 *  See {@link org.ibp.api.java.impl.middleware.user.UserServiceImpl}
	 */
	public static final String USER_AUTO_DEACTIVATION = "users.user.can.not.be.deactivated";
	public static final String CANNOT_UPDATE_SUPERADMIN = "users.update.superadmin.not.allowed";
	public static final String CANNOT_UPDATE_PERSON_MULTIPLE_USERS = "users.can.not.be.edited.multiple.persons";

	public static final String DATABASE_ERROR = "database.error";

	public static final String FIRST_NAME_STR = "First Name";
	public static final String LAST_NAME_STR = "Last Name";
	public static final String USERNAME_STR = "Username";
	public static final String EMAIL_STR = "Email";
	public static final String ROLE_STR = "Role";
	public static final String STATUS_STR = "Status";

	public static final String FIRST_NAME = "firstName";
	public static final String LAST_NAME = "lastName";
	public static final String EMAIL = "email";
	public static final String USERNAME = "username";
	public static final String ROLE = "role";
	public static final String STATUS = "status";
	public static final String USER_ID = "userId";

	public static final String EMAIL_LOCAL_PART_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*";
	private static final Pattern USERNAME_PATTERN = Pattern.compile(EMAIL_LOCAL_PART_REGEX);
	private static final String EMAIL_REGEX = EMAIL_LOCAL_PART_REGEX
				+ "@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

	@Autowired
	protected WorkbenchDataManager workbenchDataManager;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private UserService userService;

	@Override
	public boolean supports(final Class<?> aClass) {
		return UserDetailDto.class.equals(aClass);
	}

	@Override
	public void validate(final Object o, final Errors errors) {

	}

	public void validate(final Object o, final Errors errors, final boolean createUser) {
		final UserDetailDto user = (UserDetailDto) o;

		if (!USERNAME_PATTERN.matcher(user.getUsername()).matches()) {
			errors.rejectValue(USERNAME, "user.invalid.username");
		}

		this.validateFieldLength(errors, user.getFirstName(), FIRST_NAME, FIRST_NAME_STR, 20);
		this.validateFieldLength(errors, user.getLastName(), LAST_NAME, LAST_NAME_STR, 50);
		this.validateFieldLength(errors, user.getUsername(), USERNAME, USERNAME_STR, 30);
		this.validateFieldLength(errors, user.getEmail(), EMAIL, EMAIL_STR, 40);
		this.validateFieldLength(errors, user.getStatus(), STATUS, STATUS_STR, 11);

		this.validateUserRoles(errors, user);
		this.validateEmailFormat(errors, user.getEmail());

		this.validateUserStatus(errors, user.getStatus());

		if (createUser) {
			this.validateUsernameIfExists(errors, user.getUsername());

			this.validatePersonEmailIfExists(errors, user.getEmail());
		} else {
			this.validateUserId(errors, user.getId());

			this.validateUserUpdate(errors, user);
		}

	}

	private void validateUserUpdate(final Errors errors, final UserDetailDto user) {
		WorkbenchUser userUpdate = null;
		if (null == errors.getFieldError(USER_ID)) {
			try {
				userUpdate = this.userService.getUserById(user.getId());
			} catch (final MiddlewareQueryException e) {
				errors.rejectValue(USER_ID, DATABASE_ERROR);
				LOG.error(e.getMessage(), e);
			}

			if (userUpdate != null) {
				if (userUpdate.isSuperAdmin()) {
					errors.reject(CANNOT_UPDATE_SUPERADMIN);
				}
//If person entity is associated to more than one user, block user edition
				//Temporary validation, it should be removed when we unify persons and users
				final List<UserDto> usersWithSamePersonId = this.userService.getUsersByPersonIds(Lists.newArrayList(userUpdate.getPerson().getId()));
				if (usersWithSamePersonId.size()>1) {
					errors.reject(CANNOT_UPDATE_PERSON_MULTIPLE_USERS);
				}
				final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();
				// TODO change frontend status type to integer
				if (loggedInUser.equals(userUpdate) && "false".equals(user.getStatus())) {
					errors.reject(USER_AUTO_DEACTIVATION);
				}

				if (!userUpdate.getName().equalsIgnoreCase(user.getUsername())) {
					this.validateUsernameIfExists(errors, user.getUsername());
				}

				if (!userUpdate.getPerson().getEmail().equalsIgnoreCase(user.getEmail())) {
					this.validatePersonEmailIfExists(errors, user.getEmail());
				}

			} else {
				errors.rejectValue(USER_ID, SIGNUP_FIELD_INVALID_USER_ID);
			}
		}
	}

	private void validateUserId(final Errors errors, final Integer userId) {
		if (null == userId) {
			errors.rejectValue(USER_ID, SIGNUP_FIELD_REQUIRED);
		}

	}

	protected void validateEmailFormat(final Errors errors, final String eMail) {
		if (null == errors.getFieldError(EMAIL) && null != eMail && !Pattern.compile(EMAIL_REGEX).matcher(eMail).matches()) {
			errors.rejectValue(EMAIL, SIGNUP_FIELD_INVALID_EMAIL_FORMAT);
		}
	}

	protected void validateFieldLength(final Errors errors, final String fieldValue, final String fieldProperty, final String fieldName,
			final Integer maxLength) {

		if (null == fieldValue || 0 == fieldValue.trim().length()) {
			errors.rejectValue(fieldProperty, SIGNUP_FIELD_REQUIRED, new String[] {Integer.toString(maxLength), fieldName}, null);
		}

		if (null != fieldValue && maxLength < fieldValue.length()) {
			errors.rejectValue(fieldProperty, SIGNUP_FIELD_LENGTH_EXCEED, new String[] {Integer.toString(maxLength), fieldName}, null);
		}

	}

	protected void validateUsernameIfExists(final Errors errors, final String userName) {
		try {
			if (null == errors.getFieldError(USERNAME) && this.userService.isUsernameExists(userName)) {
				errors.rejectValue(USERNAME, SIGNUP_FIELD_USERNAME_EXISTS, new String[] {userName}, null);
			}
		} catch (final MiddlewareQueryException e) {
			errors.rejectValue(USERNAME, DATABASE_ERROR);
			LOG.error(e.getMessage(), e);
		}
	}

	protected void validatePersonEmailIfExists(final Errors errors, final String eMail) {
		try {
			if (null == errors.getFieldError(EMAIL) && this.userService.isPersonWithEmailExists(eMail)) {
				errors.rejectValue(EMAIL, SIGNUP_FIELD_EMAIL_EXISTS);
			}
		} catch (final MiddlewareQueryException e) {
			errors.rejectValue(EMAIL, DATABASE_ERROR);
			LOG.error(e.getMessage(), e);
		}
	}

	protected void validateUserRoles(final Errors errors, final UserDetailDto user) {

		final List<UserRoleDto> userRoles = user.getUserRoles();

		if (!userRoles.isEmpty()) {

			// Roles in the list must exist
			final Set<Integer> roleIds = userRoles.stream().map(p -> p.getRole().getId()).collect(Collectors.toSet());

			final List<Role> savedRoles = workbenchDataManager.getRoles(new RoleSearchDto(null, null, roleIds));

			if (savedRoles.size() != roleIds.size()) {
				errors.reject("user.invalid.roles", new String[] {
					(String) CollectionUtils.subtract(roleIds, savedRoles.stream().map(x -> x.getId()).collect(Collectors.toSet())).stream()
						.map(x -> x.toString())
						.collect(Collectors.joining(" , "))}, "");
				return;
			}

			// Roles in the list MUST be assignable
			final Set<Role> notAssignableRoles = savedRoles.stream().filter(e -> !e.getAssignable()).collect(Collectors.toSet());

			if (!notAssignableRoles.isEmpty()) {
				errors.reject("user.not.assignable.roles",
					new String[] {notAssignableRoles.stream().map(x -> x.getId()).collect(Collectors.toSet()).toString()}, "");
				return;
			}

			// Crops in the list MUST exist
			final Set<String> cropsUsedInUserRoles =
				userRoles.stream().filter(p -> p.getCrop() != null).map(p -> p.getCrop().getCropName()).collect(Collectors.toSet());
			final List<String> installedCrops =
				this.workbenchDataManager.getInstalledCropDatabses().stream().map(p -> p.getCropName()).collect(Collectors.toList());

			if (!installedCrops.containsAll(cropsUsedInUserRoles)) {
				errors.reject("user.crop.not.exist",
					new String[] {
						(String) CollectionUtils.subtract(cropsUsedInUserRoles, installedCrops).stream().map(x -> x.toString())
							.collect(Collectors.joining(" , "))}, "");
				return;
			}

			// Crops in the list MUST be associated to the user
			final Set<String> userCrops =
				(user.getCrops() != null) ? user.getCrops().stream().map(p -> p.getCropName()).collect(Collectors.toSet()) :
					new HashSet<>();
			if (!userCrops.containsAll(cropsUsedInUserRoles)) {
				errors.reject("user.crop.not.associated.to.user", new String[] {
					(String) CollectionUtils.subtract(cropsUsedInUserRoles, userCrops).stream().map(x -> x.toString())
						.collect(Collectors.joining(" , "))}, "");
				return;
			}

			// ROLES must be consistent
			// Instance ROLE can not have neither crop nor program
			// Crop ROLE MUST have a crop and can not have a program
			// Program ROLE MUST have crop and program and program MUST belong to the specified crop
			Map<Integer, Role> savedRolesMap = savedRoles.stream().collect(
				Collectors.toMap(Role::getId, Function.identity()));
			for (final UserRoleDto userRoleDto : userRoles) {
				final Role role = savedRolesMap.get(userRoleDto.getRole().getId());
				if (role.getRoleType().getId().equals(RoleType.INSTANCE.getId())) {
					if (userRoleDto.getCrop() != null || userRoleDto.getProgram() != null) {
						errors.reject("user.invalid.instance.role", new String[] {role.getId().toString()}, "");
					}
				}
				if (role.getRoleType().getId().equals(RoleType.CROP.getId())) {
					if (userRoleDto.getCrop() == null || userRoleDto.getCrop().getCropName() == null || userRoleDto.getCrop().getCropName()
						.isEmpty() || userRoleDto.getProgram() != null) {
						errors.reject("user.invalid.crop.role", new String[] {role.getId().toString()}, "");

					}

				}
				if (role.getRoleType().getId().equals(RoleType.PROGRAM.getId())) {
					if (userRoleDto.getCrop() == null || userRoleDto.getCrop().getCropName() == null || userRoleDto.getCrop().getCropName()
						.isEmpty() ||
						userRoleDto.getProgram() == null || userRoleDto.getProgram().getUuid() == null || userRoleDto.getProgram().getUuid()
						.isEmpty()) {
						errors.reject("user.invalid.program.role", new String[] {role.getId().toString()}, "");

					} else {
						final Project project = workbenchDataManager
							.getProjectByUuidAndCrop(userRoleDto.getProgram().getUuid(), userRoleDto.getCrop().getCropName());
						if (project == null) {
							errors.reject("user.invalid.crop.program.pair",
								new String[] {userRoleDto.getProgram().getUuid(), userRoleDto.getCrop().getCropName()}, "");

						}
					}
				}
			}
			if (errors.getErrorCount() > 0) {
				return;
			}

			// ROLES must be unique in the context they are assigned.
			// User can have only one INSTANCE role
			int totalInstanceRoles = 0;
			final Set<Integer> instanceRoleIds =
				savedRoles.stream().filter(e -> e.getRoleType().getId().equals(RoleType.INSTANCE.getId())).map(p -> p.getId())
					.collect(Collectors.toSet());
			for (final UserRoleDto userRoleDto : userRoles) {
				if (instanceRoleIds.contains(userRoleDto.getRole().getId())) {
					totalInstanceRoles++;
				}
			}
			if (totalInstanceRoles > 1) {
				errors.reject("user.can.have.one.instance.role");
				return;
			}

			// User can have only one CROP role per CROP
			final Map<String, Integer> cropRolesPerCropMap = new HashMap<>();
			for (final UserRoleDto userRoleDto : userRoles) {
				final String cropName = (userRoleDto.getCrop() != null) ? userRoleDto.getCrop().getCropName() : null;
				if (cropName != null && userRoleDto.getProgram() == null) {
					cropRolesPerCropMap.putIfAbsent(cropName, 0);
					cropRolesPerCropMap.replace(cropName, cropRolesPerCropMap.get(cropName) + 1);
				}
			}
			if (!cropRolesPerCropMap.values().stream().filter(p -> p > 1).collect(Collectors.toList()).isEmpty()) {
				errors.reject("user.can.have.one.crop.role.per.crop");
				return;
			}

			// User can have only one PROGRAM role per PROGRAM
			final Map<String, Integer> programRolesPerProgram = new HashMap<>();
			for (final UserRoleDto userRoleDto : userRoles) {
				final String cropName = (userRoleDto.getCrop() != null) ? userRoleDto.getCrop().getCropName() : null;
				final String programUUID = (userRoleDto.getProgram() != null) ? userRoleDto.getProgram().getUuid() : null;

				if (cropName != null && programUUID != null) {
					final String key = cropName.concat(programUUID);
					programRolesPerProgram.putIfAbsent(key, 0);
					programRolesPerProgram.replace(key, programRolesPerProgram.get(key) + 1);
				}
			}
			if (!programRolesPerProgram.values().stream().filter(p -> p > 1).collect(Collectors.toList()).isEmpty()) {
				errors.reject("user.can.have.one.program.role.per.program");
				return;
			}
		}

	}
    
	protected void validateUserStatus(final Errors errors, final String fieldValue) {
		if (null == errors.getFieldError(STATUS) && fieldValue != null && !"true".equalsIgnoreCase(fieldValue)
				&& !"false".equalsIgnoreCase(fieldValue)) {
			errors.rejectValue(STATUS, SIGNUP_FIELD_INVALID_STATUS);
		}
	}

	public void setWorkbenchDataManager(WorkbenchDataManager workbenchDataManager) {
		this.workbenchDataManager = workbenchDataManager;
	}

	public void setSecurityService(final SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setUserService(final UserService userService) {
		this.userService = userService;
	}
}
