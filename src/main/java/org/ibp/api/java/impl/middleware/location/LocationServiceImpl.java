package org.ibp.api.java.impl.middleware.location;

import org.generationcp.middleware.manager.api.LocationDataManager;
import org.ibp.api.domain.location.LocationDto;
import org.ibp.api.domain.location.LocationMapper;
import org.ibp.api.java.location.LocationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LocationServiceImpl implements LocationService {

	@Autowired
	private LocationDataManager locationDataManager;

	@Override
	public List<LocationDto> getLocations(final Set<Integer> locationTypes, final String programUUID, final boolean favoriteLocations, final List<String> locationAbbreviations) {
		final List<org.generationcp.middleware.pojos.Location> locations;
		final ModelMapper mapper = LocationMapper.getInstance();
		List<Integer> locationIds = null;

		if (favoriteLocations) {
			locationIds = locationDataManager.getFavoriteProjectLocationIds(programUUID);
		}

		locations = locationDataManager.getFilteredLocations(locationTypes, locationIds, locationAbbreviations);

		if(locations.isEmpty()){
			return new ArrayList<>();
		}

		final List<LocationDto> locationList = locations.stream().map(o -> mapper.map(o, LocationDto.class)).collect(Collectors.toList());

		return locationList;
	}
}
