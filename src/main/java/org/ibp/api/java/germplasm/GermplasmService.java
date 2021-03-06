
package org.ibp.api.java.germplasm;

import org.generationcp.middleware.domain.germplasm.AttributeDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmDTO;
import org.generationcp.middleware.domain.germplasm.PedigreeDTO;
import org.generationcp.middleware.domain.germplasm.ProgenyDTO;
import org.generationcp.middleware.domain.search_request.brapi.v1.GermplasmSearchRequestDto;
import org.ibp.api.domain.germplasm.DescendantTree;
import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.ibp.api.domain.germplasm.PedigreeTree;

import java.util.List;

public interface GermplasmService {

	Integer DEFAULT_PEDIGREE_LEVELS = 20;

	int searchGermplasmCount(String searchText);

	List<GermplasmSummary> searchGermplasm(String searchText, int pageNumber, int pageSize);

	GermplasmSummary getGermplasm(String germplasmId);

	PedigreeDTO getPedigree(Integer germplasmDbId, String notation, Boolean includeSiblings);

	ProgenyDTO getProgeny(Integer germplasmDbId);

	PedigreeTree getPedigreeTree(String germplasmId, Integer levels);

	DescendantTree getDescendantTree(String germplasmId);

	GermplasmDTO getGermplasmDTObyGID (Integer germplasmId);

	List<GermplasmDTO> searchGermplasmDTO (GermplasmSearchRequestDto germplasmSearchRequestDTO, Integer page, Integer pageSize);

	long countGermplasmDTOs(GermplasmSearchRequestDto germplasmSearchRequestDTO);

	long countGermplasmByStudy(Integer studyDbId);

	List<GermplasmDTO> getGermplasmByStudy(int studyDbId, int pageSize, int pageNumber);

	List<AttributeDTO> getAttributesByGid(
		String gid, List<String> attributeDbIds, Integer pageSize, Integer pageNUmber);

	long countAttributesByGid(String gid, List<String> attributeDbIds);
}
