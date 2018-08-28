
package org.ibp.api.java.germplasm;

import java.util.List;

import org.generationcp.middleware.domain.germplasm.ProgenyDTO;
import org.generationcp.middleware.dao.germplasm.GermplasmSearchRequestDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmDTO;
import org.generationcp.middleware.domain.germplasm.PedigreeDTO;
import org.ibp.api.domain.germplasm.DescendantTree;
import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.ibp.api.domain.germplasm.PedigreeTree;

public interface GermplasmService {

	Integer DEFAULT_PEDIGREE_LEVELS = 20;

	int searchGermplasmCount(String searchText);

	List<GermplasmSummary> searchGermplasm(String searchText, int pageNumber, int pageSize);

	GermplasmSummary getGermplasm(String germplasmId);

	PedigreeDTO getPedigree(Integer germplasmDbId, String notation);

	ProgenyDTO getProgeny(Integer germplasmDbId);

	PedigreeTree getPedigreeTree(String germplasmId, Integer levels);

	DescendantTree getDescendantTree(String germplasmId);

	GermplasmDTO getGermplasmDTObyGID (Integer germplasmId);

	List<GermplasmDTO> searchGermplasmDTO (GermplasmSearchRequestDTO germplasmSearchRequestDTO);

	long countGermplasmDTOs(GermplasmSearchRequestDTO germplasmSearchRequestDTO);

}
