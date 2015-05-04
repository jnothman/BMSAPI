
package org.ibp.api.java.impl.middleware.germplasm;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GermplasmServiceImpl implements GermplasmService {

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private PedigreeService pedigreeService;
	
	@Autowired
	private LocationDataManager locationDataManger;

	@Override
	public List<GermplasmSummary> searchGermplasm(String searchText) {
		List<GermplasmSummary> results = new ArrayList<GermplasmSummary>();
		try {
			List<Germplasm> searchResults = germplasmDataManager.searchForGermplasm(searchText, Operation.LIKE, false);
			for (Germplasm germplasm : searchResults) {

				GermplasmSummary summary = new GermplasmSummary();
				summary.setGid(germplasm.getGid());

				CrossExpansionProperties crossExpansionProperties = new CrossExpansionProperties();
				crossExpansionProperties.setDefaultLevel(1);
				crossExpansionProperties.setWheatLevel(1);
				summary.setCross(pedigreeService.getCrossExpansion(germplasm.getGid(), crossExpansionProperties));

				// FIXME - select in a loop ... Middleware service should handle all this in main query.
				List<Name> namesByGID = germplasmDataManager.getNamesByGID(new Integer(summary.getGid()), null, null);
				List<String> names = new ArrayList<String>();
				for (Name gpName : namesByGID) {
					names.add(gpName.getNval());
				}
				summary.addNames(names);

				Method germplasmMethod = germplasmDataManager.getMethodByID(germplasm.getMethodId());
				if (germplasmMethod != null && germplasmMethod.getMname() != null) {
					summary.setBreedingMethod(germplasmMethod.getMname());
				}

				Location germplasmLocation = locationDataManger.getLocationByID(germplasm.getLocationId());
				if (germplasmLocation != null && germplasmLocation.getLname() != null) {
					summary.setLocation(germplasmLocation.getLname());
				}

				results.add(summary);
			}
		} catch (MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
		return results;
	}

}