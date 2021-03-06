
package org.ibp.api.java.impl.middleware.germplasm;

import com.google.common.collect.Sets;
import org.generationcp.middleware.domain.germplasm.AttributeDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmDTO;
import org.generationcp.middleware.domain.germplasm.PedigreeDTO;
import org.generationcp.middleware.domain.germplasm.ProgenyDTO;
import org.generationcp.middleware.domain.gms.search.GermplasmSearchParameter;
import org.generationcp.middleware.domain.search_request.brapi.v1.GermplasmSearchRequestDto;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.PedigreeDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmPedigreeTree;
import org.generationcp.middleware.pojos.GermplasmPedigreeTreeNode;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.service.api.GermplasmGroupingService;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.domain.germplasm.DescendantTree;
import org.ibp.api.domain.germplasm.DescendantTreeTreeNode;
import org.ibp.api.domain.germplasm.GermplasmName;
import org.ibp.api.domain.germplasm.GermplasmSummary;
import org.ibp.api.domain.germplasm.PedigreeTree;
import org.ibp.api.domain.germplasm.PedigreeTreeNode;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.java.impl.middleware.common.validator.AttributeValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.ibp.api.java.impl.middleware.dataset.validator.InstanceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class GermplasmServiceImpl implements GermplasmService {

	@Autowired
	private GermplasmValidator germplasmValidator;

	@Autowired
	private AttributeValidator attributeValidator;

	private BindingResult errors;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private PedigreeService pedigreeService;

	@Autowired
	private PedigreeDataManager pedigreeDataManager;

	@Autowired
	private CrossExpansionProperties crossExpansionProperties;

	@Autowired
	private LocationDataManager locationDataManger;

	@Autowired
	private GermplasmGroupingService germplasmGroupingService;

	@Autowired
	private StudyDataManager studyDataManager;

	@Autowired
	private InstanceValidator instanceValidator;

	@Override
	public List<GermplasmSummary> searchGermplasm(final String searchText, final int pageNumber, final int pageSize) {
		final List<GermplasmSummary> results = new ArrayList<GermplasmSummary>();
		try {
			final GermplasmSearchParameter searchParams = new GermplasmSearchParameter(searchText, Operation.LIKE);
			final int start = pageSize * (pageNumber - 1);
			final int numOfRows = pageSize;
			searchParams.setStartingRow(start);
			searchParams.setNumberOfEntries(numOfRows);
			final List<Germplasm> searchResults = this.germplasmDataManager.searchForGermplasm(searchParams);
			for (final Germplasm germplasm : searchResults) {
				results.add(this.populateGermplasmSummary(germplasm));
			}
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
		return results;
	}

	private GermplasmSummary populateGermplasmSummary(final Germplasm germplasm) throws MiddlewareQueryException {
		if (germplasm == null) {
			return null;
		}
		final GermplasmSummary summary = new GermplasmSummary();
		summary.setGermplasmId(germplasm.getGid().toString());
		summary.setParent1Id(germplasm.getGpid1() != null && germplasm.getGpid1() != 0 ? germplasm.getGpid1().toString() : "Unknown");
		summary.setParent2Id(germplasm.getGpid2() != null && germplasm.getGpid2() != 0 ? germplasm.getGpid2().toString() : "Unknown");

		summary.setPedigreeString(this.pedigreeService.getCrossExpansion(germplasm.getGid(), this.crossExpansionProperties));

		// FIXME - select in a loop ... Middleware service should handle all this in main query.
		final List<Name> namesByGID = this.germplasmDataManager.getNamesByGID(new Integer(germplasm.getGid()), null, null);
		final List<GermplasmName> names = new ArrayList<GermplasmName>();
		for (final Name gpName : namesByGID) {
			final GermplasmName germplasmName = new GermplasmName();
			germplasmName.setName(gpName.getNval());
			final UserDefinedField nameType = this.germplasmDataManager.getUserDefinedFieldByID(gpName.getTypeId());
			if (nameType != null) {
				germplasmName.setNameTypeCode(nameType.getFcode());
				germplasmName.setNameTypeDescription(nameType.getFname());
			}
			names.add(germplasmName);
		}
		summary.addNames(names);

		final Method germplasmMethod = this.germplasmDataManager.getMethodByID(germplasm.getMethodId());
		if (germplasmMethod != null && germplasmMethod.getMname() != null) {
			summary.setBreedingMethod(germplasmMethod.getMname());
		}

		final Location germplasmLocation = this.locationDataManger.getLocationByID(germplasm.getLocationId());
		if (germplasmLocation != null && germplasmLocation.getLname() != null) {
			summary.setLocation(germplasmLocation.getLname());
		}
		return summary;
	}

	@Override
	public GermplasmSummary getGermplasm(final String germplasmId) {
		final Germplasm germplasm;
		try {
			germplasm = this.germplasmDataManager.getGermplasmByGID(Integer.valueOf(germplasmId));
			return this.populateGermplasmSummary(germplasm);
		} catch (final NumberFormatException | MiddlewareQueryException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}

	void setPedigreeService(final PedigreeService pedigreeService) {
		this.pedigreeService = pedigreeService;
	}

	void setLocationDataManger(final LocationDataManager locationDataManger) {
		this.locationDataManger = locationDataManger;
	}

	void setCrossExpansionProperties(final CrossExpansionProperties crossExpansionProperties) {
		this.crossExpansionProperties = crossExpansionProperties;
	}
	
	@Override
	public PedigreeDTO getPedigree(final Integer germplasmDbId, final String notation, final Boolean includeSiblings) {
		final PedigreeDTO pedigreeDTO;
		try {
			pedigreeDTO = this.germplasmDataManager.getPedigree(germplasmDbId, notation, includeSiblings);
			if (pedigreeDTO != null) {
				pedigreeDTO.setPedigree(this.pedigreeService.getCrossExpansion(germplasmDbId, this.crossExpansionProperties));
			}
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("An error has occurred when trying to get the pedigree", e);
		}
		return pedigreeDTO;
	}

	@Override
	public ProgenyDTO getProgeny(final Integer germplasmDbId) {
		final ProgenyDTO progenyDTO;
		try {
			progenyDTO = this.germplasmDataManager.getProgeny(germplasmDbId);
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("An error has occurred when trying to get the progeny", e);
		}
		return progenyDTO;
	}

	@Override
	public PedigreeTree getPedigreeTree(final String germplasmId, Integer levels) {

		if (levels == null) {
			levels = DEFAULT_PEDIGREE_LEVELS;
		}
		final GermplasmPedigreeTree mwTree = this.pedigreeDataManager.generatePedigreeTree(Integer.valueOf(germplasmId), levels);

		final PedigreeTree pedigreeTree = new PedigreeTree();
		pedigreeTree.setRoot(this.traversePopulate(mwTree.getRoot()));

		return pedigreeTree;
	}

	@Override
	public DescendantTree getDescendantTree(final String germplasmId) {
		final Germplasm germplasm = this.germplasmDataManager.getGermplasmByGID(Integer.valueOf(germplasmId));
		final GermplasmPedigreeTree mwTree = this.germplasmGroupingService.getDescendantTree(germplasm);

		final DescendantTree descendantTree = new DescendantTree();
		descendantTree.setRoot(this.traversePopulateDescendatTree(mwTree.getRoot()));

		return descendantTree;
	}

	private DescendantTreeTreeNode traversePopulateDescendatTree(final GermplasmPedigreeTreeNode mwTreeNode) {
		final DescendantTreeTreeNode treeNode = new DescendantTreeTreeNode();
		treeNode.setGermplasmId(mwTreeNode.getGermplasm().getGid());
		treeNode.setProgenitors(mwTreeNode.getGermplasm().getGnpgs());
		treeNode.setMethodId(mwTreeNode.getGermplasm().getMethodId());
		treeNode.setParent1Id(mwTreeNode.getGermplasm().getGpid1());
		treeNode.setParent2Id(mwTreeNode.getGermplasm().getGpid2());
		treeNode.setManagementGroupId(mwTreeNode.getGermplasm().getMgid());

		final Name preferredName = mwTreeNode.getGermplasm().findPreferredName();
		treeNode.setName(preferredName != null ? preferredName.getNval() : null);

		final List<DescendantTreeTreeNode> nodeChildren = new ArrayList<>();
		for (final GermplasmPedigreeTreeNode mwChild : mwTreeNode.getLinkedNodes()) {
			nodeChildren.add(this.traversePopulateDescendatTree(mwChild));
		}
		treeNode.setChildren(nodeChildren);
		return treeNode;
	}

	private PedigreeTreeNode traversePopulate(final GermplasmPedigreeTreeNode mwTreeNode) {
		final PedigreeTreeNode treeNode = new PedigreeTreeNode();
		treeNode.setGermplasmId(mwTreeNode.getGermplasm().getGid().toString());
		treeNode.setName(mwTreeNode.getGermplasm().getPreferredName() != null ? mwTreeNode.getGermplasm().getPreferredName().getNval()
				: null);

		final List<PedigreeTreeNode> nodeParents = new ArrayList<>();
		for (final GermplasmPedigreeTreeNode mwParent : mwTreeNode.getLinkedNodes()) {
			nodeParents.add(this.traversePopulate(mwParent));
		}
		treeNode.setParents(nodeParents);
		return treeNode;
	}

	@Override
	public int searchGermplasmCount(final String searchText) {

		final GermplasmSearchParameter searchParameter = new GermplasmSearchParameter(searchText, Operation.LIKE, false, false, false);

		return this.germplasmDataManager.countSearchForGermplasm(searchParameter);
	}

	@Override
	public GermplasmDTO getGermplasmDTObyGID (final Integer germplasmId) {
		final GermplasmDTO germplasmDTO;
		try {
			germplasmDTO = this.germplasmDataManager.getGermplasmDTOByGID(germplasmId);
			if (germplasmDTO != null) {
				germplasmDTO.setPedigree(this.pedigreeService.getCrossExpansion(germplasmId, this.crossExpansionProperties));
			}
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("An error has occurred when trying to get a germplasm", e);
		}
		return germplasmDTO;
	}

	@Override
	public List<GermplasmDTO> searchGermplasmDTO(
		final GermplasmSearchRequestDto germplasmSearchRequestDTO, final Integer page, final Integer pageSize) {
		try {

			final List<GermplasmDTO> germplasmDTOList = this.germplasmDataManager
				.searchGermplasmDTO(germplasmSearchRequestDTO, page, pageSize);
			if (germplasmDTOList != null) {
				final Set<Integer> gids = germplasmDTOList.stream().map(germplasmDTO -> Integer.valueOf(germplasmDTO.getGermplasmDbId()))
					.collect(Collectors.toSet());
				final Map<Integer, String> crossExpansionsMap =
					this.pedigreeService.getCrossExpansions(gids, null, this.crossExpansionProperties);
				for (final GermplasmDTO germplasmDTO : germplasmDTOList) {
					germplasmDTO.setPedigree(crossExpansionsMap.get(Integer.valueOf(germplasmDTO.getGermplasmDbId())));
				}
			}
			return germplasmDTOList;
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("An error has occurred when trying to search germplasms", e);
		}
	}

	@Override
	public long countGermplasmDTOs(final GermplasmSearchRequestDto germplasmSearchRequestDTO) {
		try {
			return this.germplasmDataManager.countGermplasmDTOs(germplasmSearchRequestDTO);
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("An error has occurred when trying to count germplasms", e);
		}
	}

	@Override
	public long countGermplasmByStudy(final Integer studyDbId) {
		try {
			return this.germplasmDataManager.countGermplasmByStudy(studyDbId);
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("An error has occurred when trying to count germplasms", e);
		}
	}

	@Override
	public List<GermplasmDTO> getGermplasmByStudy(final int studyDbId, final int pageSize, final int pageNumber) {
		try {

			this.instanceValidator.validateStudyDbId(studyDbId);

			final List<GermplasmDTO> germplasmDTOList = this.germplasmDataManager
				.getGermplasmByStudy(studyDbId, pageNumber, pageSize);
			if (germplasmDTOList != null) {
				final Set<Integer> gids = germplasmDTOList.stream().map(germplasmDTO -> Integer.valueOf(germplasmDTO.getGermplasmDbId()))
					.collect(Collectors.toSet());
				final Map<Integer, String> crossExpansionsMap =
					this.pedigreeService.getCrossExpansions(gids, null, this.crossExpansionProperties);
				for (final GermplasmDTO germplasmDTO : germplasmDTOList) {
					germplasmDTO.setPedigree(crossExpansionsMap.get(Integer.valueOf(germplasmDTO.getGermplasmDbId())));
				}
			}
			return germplasmDTOList;
		} catch (final MiddlewareQueryException e) {
			throw new ApiRuntimeException("An error has occurred when trying to search germplasms", e);
		}
	}

	@Override
	public List<AttributeDTO> getAttributesByGid(
		final String gid, final List<String> attributeDbIds, final Integer pageSize, final Integer pageNumber) {
		this.validateGidAndAttributes(gid, attributeDbIds);
		return this.germplasmDataManager.getAttributesByGid(gid, attributeDbIds, pageSize, pageNumber);
	}

	@Override
	public long countAttributesByGid(final String gid, final List<String> attributeDbIds) {
		return this.germplasmDataManager.countAttributesByGid(gid, attributeDbIds);
	}

	private void validateGidAndAttributes(final String gid, final List<String> attributeDbIds) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), AttributeDTO.class.getName());
		this.germplasmValidator.validateGermplasmId(this.errors, Integer.valueOf(gid));
		if (this.errors.hasErrors()) {
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}
		this.attributeValidator.validateAttributeIds(this.errors, attributeDbIds);
		if (this.errors.hasErrors()) {
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}
	}
}
