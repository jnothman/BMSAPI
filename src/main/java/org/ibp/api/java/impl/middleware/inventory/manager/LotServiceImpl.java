package org.ibp.api.java.impl.middleware.inventory.manager;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.service.StockService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotGeneratorInputDto;
import org.generationcp.middleware.domain.inventory.manager.LotImportRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotItemDto;
import org.generationcp.middleware.domain.inventory.manager.LotSearchMetadata;
import org.generationcp.middleware.domain.inventory.manager.LotUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.ibp.api.java.impl.middleware.inventory.manager.common.InventoryLock;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.ExtendedLotListValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotImportRequestDtoValidator;
import org.ibp.api.java.impl.middleware.inventory.manager.validator.LotInputValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.java.inventory.manager.LotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class LotServiceImpl implements LotService {

	@Autowired
	private LotInputValidator lotInputValidator;

	@Autowired
	private LotImportRequestDtoValidator lotImportRequestDtoValidator;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private org.generationcp.middleware.service.api.inventory.LotService lotService;

	@Autowired
	private StockService stockService;

	@Resource
	private ContextUtil contextUtil;

	@Autowired
	private ExtendedLotListValidator extendedLotListValidator;

	@Autowired
	private InventoryLock inventoryLock;

	private static final String DEFAULT_STOCKID_PREFIX = "SID";

	@Override
	public List<ExtendedLotDto> searchLots(final LotsSearchDto lotsSearchDto, final Pageable pageable) {
		try {
			inventoryLock.lockRead();
			return lotService.searchLots(lotsSearchDto, pageable);
		} finally {
			inventoryLock.unlockRead();
		}
	}

	@Override
	public long countSearchLots(final LotsSearchDto lotsSearchDto) {
		return lotService.countSearchLots(lotsSearchDto);
	}

	@Override
	public List<UserDefinedField> getGermplasmAttributeTypes(final LotsSearchDto searchDto) {
		return this.lotService.getGermplasmAttributeTypes(searchDto);
	}

	@Override
	public Map<Integer, Map<Integer, String>> getGermplasmAttributeValues(final LotsSearchDto searchDto) {
		return this.lotService.getGermplasmAttributeValues(searchDto);
	}

	@Override
	public Integer saveLot(
		final LotGeneratorInputDto lotGeneratorInputDto) {
		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();
		lotInputValidator.validate(lotGeneratorInputDto);
		if (lotGeneratorInputDto.getGenerateStock()) {
			final String nextStockIDPrefix;
			if (lotGeneratorInputDto.getStockPrefix() == null || lotGeneratorInputDto.getStockPrefix().isEmpty()) {
				nextStockIDPrefix = this.stockService.calculateNextStockIDPrefix(DEFAULT_STOCKID_PREFIX, "-");
			} else {
				nextStockIDPrefix = this.stockService.calculateNextStockIDPrefix(lotGeneratorInputDto.getStockPrefix(), "-");
			}
			lotGeneratorInputDto.setStockId(nextStockIDPrefix + "1");
		}

		return lotService.saveLot(this.contextUtil.getProjectInContext().getCropType(), loggedInUser.getUserid(), lotGeneratorInputDto);
	}


	@Override
	public void updateLots(final List<ExtendedLotDto> lotDtos, final LotUpdateRequestDto lotRequest) {
		try {
			inventoryLock.lockWrite();
			this.lotInputValidator.validate(lotDtos, lotRequest);
			this.lotService.updateLots(lotDtos, lotRequest);
		} finally {
			inventoryLock.unlockWrite();
		}
	}

	@Override
	public void importLotsWithInitialTransaction(final LotImportRequestDto lotImportRequestDto) {
		final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();
		final CropType cropType = this.contextUtil.getProjectInContext().getCropType();
		this.lotImportRequestDtoValidator.validate(lotImportRequestDto);
		final List<LotItemDto> lotsWithNoStockId =
			lotImportRequestDto.getLotList().stream().filter(x -> StringUtils.isEmpty(x.getStockId())).collect(Collectors.toList());
		if (!lotsWithNoStockId.isEmpty()) {
			if (StringUtils.isEmpty(lotImportRequestDto.getStockIdPrefix())) {
				lotImportRequestDto.setStockIdPrefix(DEFAULT_STOCKID_PREFIX);
			}
			final String nextStockIDPrefix = this.stockService.calculateNextStockIDPrefix(lotImportRequestDto.getStockIdPrefix(), "-");
			int i = 0;
			for (final LotItemDto lotItemDto : lotsWithNoStockId) {
				lotItemDto.setStockId(nextStockIDPrefix + ++i);
			}
		}
		this.lotService.saveLotsWithInitialTransaction(cropType, loggedInUser.getUserid(), lotImportRequestDto.getLotList());
	}

	@Override
	public LotSearchMetadata getLotsSearchMetadata(final LotsSearchDto lotsSearchDto) {
		try {
			inventoryLock.lockRead();
			return lotService.getLotSearchMetadata(lotsSearchDto);
		} finally {
			inventoryLock.unlockRead();
		}
	}

	@Override
	public void closeLots(final LotsSearchDto searchDTO) {
		try {
			inventoryLock.lockWrite();
			final List<ExtendedLotDto> lotDtos = this.lotService.searchLots(searchDTO, null);
			extendedLotListValidator.validateClosedLots(lotDtos.stream().collect(Collectors.toList()));
			final WorkbenchUser loggedInUser = this.securityService.getCurrentlyLoggedInUser();
			lotService.closeLots(loggedInUser.getUserid(), lotDtos.stream().map(ExtendedLotDto::getLotId).collect(Collectors.toList()));
		} finally {
			inventoryLock.unlockWrite();
		}
	}

}
