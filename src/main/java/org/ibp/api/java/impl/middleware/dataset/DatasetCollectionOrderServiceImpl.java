package org.ibp.api.java.impl.middleware.dataset;

import org.generationcp.middleware.domain.fieldbook.FieldmapBlockInfo;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.java.dataset.DatasetCollectionOrderService;
import org.ibp.api.java.impl.middleware.study.FieldMapService;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class DatasetCollectionOrderServiceImpl implements DatasetCollectionOrderService {

	@Resource
	private FieldMapService fieldMapService;

	@Resource
	private DataCollectionSorter dataCollectionSorter;

	@Override
	public void reorder(
		final CollectionOrder collectionOrder,
		final int trialDatasetId,  final Map<Integer, StudyInstance>  selectedDatasetInstancesMap, final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap) {

		for (final Integer instanceDBID : observationUnitRowMap.keySet()) {
			List<ObservationUnitRow> observationUnitRows = observationUnitRowMap.get(instanceDBID);
			reorder(collectionOrder, trialDatasetId, selectedDatasetInstancesMap.get(instanceDBID).getInstanceNumber(), observationUnitRows);
		}
	}

	@Override
	public void reorder(
		final CollectionOrder collectionOrder, final int trialDatasetId,
		final int instanceNumber, final List<ObservationUnitRow> observationUnitRows) {

		final String blockId =
		this.fieldMapService.getBlockId(trialDatasetId, String.valueOf(instanceNumber));

		FieldmapBlockInfo fieldmapBlockInfo = null;
		if (blockId != null) {
			fieldmapBlockInfo = this.fieldMapService.getBlockInformation(Integer.valueOf(blockId));
		}

		if (collectionOrder == CollectionOrder.SERPENTINE_ALONG_ROWS) {
			this.dataCollectionSorter.orderByRange(fieldmapBlockInfo, observationUnitRows);
		} else if (collectionOrder == CollectionOrder.SERPENTINE_ALONG_COLUMNS) {
			this.dataCollectionSorter.orderByColumn(fieldmapBlockInfo, observationUnitRows);
		}
	}

	public enum CollectionOrder {

		PLOT_ORDER(1),
		SERPENTINE_ALONG_ROWS(2),
		SERPENTINE_ALONG_COLUMNS(3);

		private int id;

		private CollectionOrder(int id) {
			this.id = id;
		}

		public static CollectionOrder findById(int id) {
			for (CollectionOrder order : CollectionOrder.values()) {
				if (order.getId() == id) {
					return order;
				}
			}
			return null;
		}

		public int getId() {
			return this.id;
		}

	}

}
