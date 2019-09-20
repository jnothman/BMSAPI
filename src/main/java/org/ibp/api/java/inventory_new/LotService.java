package org.ibp.api.java.inventory_new;

import org.generationcp.middleware.domain.inventory_new.LotDto;
import org.generationcp.middleware.domain.inventory_new.LotsSearchDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LotService {

	List<LotDto> searchLots(LotsSearchDto lotsSearchDto, Pageable pageable);

	long countSearchLots(LotsSearchDto lotsSearchDto);

}
