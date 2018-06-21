package org.ibp.api.domain.user;

import org.generationcp.middleware.service.api.user.UserDto;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

public class UserMapper {

	private static ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	private static Converter<Integer, String> toStatusConvert = new AbstractConverter<Integer, String>() {

		protected String convert(final Integer statusId) {
			return statusId.equals(0) ? "true" : "false";

		}
	};

	private UserMapper() {

	}

	static {
		UserMapper.addUserDetailsDataMapping(UserMapper.applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return UserMapper.applicationWideModelMapper;
	}

	private static void addUserDetailsDataMapping(final ModelMapper mapper) {

		mapper.addMappings(new PropertyMap<UserDto, UserDetailDto>() {

			@Override
			protected void configure() {
				this.map().setFirstName(this.source.getFirstName());
				this.map().setLastName(this.source.getLastName());
				this.map().setId(this.source.getUserId());
				this.map().setUsername(this.source.getUsername());
				this.map().setRole(this.source.getRole());
				using(toStatusConvert).map().setStatus(this.source.getStatus().toString());
				this.map().setEmail(this.source.getEmail());
			}
		});
	}
}
