package org.ibp.api.brapi.v1.trial;

import org.generationcp.middleware.dao.dms.InstanceMetadata;
import org.generationcp.middleware.domain.dms.StudySummary;
import org.ibp.api.brapi.v1.study.StudySummaryDto;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;

import java.util.ArrayList;
import java.util.List;

public class TrialSummaryMapper {

	private static ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	private TrialSummaryMapper() {

	}

	static {
		TrialSummaryMapper.addTrialSummaryMapper(TrialSummaryMapper.applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return TrialSummaryMapper.applicationWideModelMapper;
	}

	private static class StudySummaryConverter implements Converter<List<InstanceMetadata>, List<StudySummaryDto>> {

		@Override
		public List<StudySummaryDto> convert(final MappingContext<List<InstanceMetadata>, List<StudySummaryDto>> context) {

			final List<StudySummaryDto> studySummaries = new ArrayList<>();

			for (final InstanceMetadata instance : context.getSource()) {
				final StudySummaryDto studyMetadata = new StudySummaryDto();
				studyMetadata.setStudyDbId(instance.getInstanceDbId());
				studyMetadata.setStudyName(instance.getTrialName() + " Environment Number " + instance.getInstanceNumber());
				studyMetadata.setLocationName(
						instance.getLocationName() != null ? instance.getLocationName() : instance.getLocationAbbreviation());
				studyMetadata.setLocationDbId(String.valueOf(instance.getLocationDbId()));
				studySummaries.add(studyMetadata);
			}
			return context.getMappingEngine().map(context.create(studySummaries, context.getDestinationType()));

		}

	}

	private static void addTrialSummaryMapper(final ModelMapper mapper) {

		mapper.addMappings(new PropertyMap<StudySummary, TrialSummary>() {

			@Override
			protected void configure() {
				map(source.getLocationId(), destination.getLocationDbId());
				map(source.getStudyDbid(), destination.getTrialDbId());
				map(source.getName(), destination.getTrialName());
				map(source.getProgramDbId(), destination.getProgramDbId());
				map(source.getProgramName(), destination.getProgramName());
				map(source.getStartDate(), destination.getStartDate());
				map(source.getEndDate(), destination.getEndDate());
				map(source.isActive(), destination.isActive());
				map(source.getOptionalInfo(), destination.getAdditionalInfo());
				this.using(new StudySummaryConverter()).map(this.source.getInstanceMetaData()).setStudies(null);

			}

		});
	}
}
