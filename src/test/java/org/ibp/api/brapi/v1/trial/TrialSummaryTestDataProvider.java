package org.ibp.api.brapi.v1.trial;

import org.generationcp.middleware.dao.dms.InstanceMetadata;
import org.generationcp.middleware.domain.dms.StudySummary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrialSummaryTestDataProvider {

	private static Map<String, String> getOptionalInfo() {
		final Map<String, String> additionalInfo = new HashMap<>();
		additionalInfo.put("K", "V");
		return additionalInfo;
	}

	private static List<InstanceMetadata> getInstanceMatadatas() {
		final List<InstanceMetadata> instanceMetadatas = new ArrayList<>();
		final InstanceMetadata instanceMetadata = new InstanceMetadata();
		instanceMetadata.setLocationName("LOC");
		instanceMetadata.setTrialName("TR");
		instanceMetadata.setInstanceDbId(2);
		instanceMetadata.setInstanceNumber("1");
		instanceMetadata.setLocationAbbreviation("ABBR");
		instanceMetadatas.add(instanceMetadata);
		return instanceMetadatas;
	}

	public static StudySummary getTrialSummary() {
		final StudySummary studySummary = new StudySummary();
		studySummary.setLocationId("1");
		studySummary.setActive(Boolean.TRUE);
		studySummary.setEndDate("20170404");
		studySummary.setProgramDbId("64646");
		studySummary.setProgramName("PROGRAM1");
		studySummary.setStartDate("20160404");
		studySummary.setStudyDbid(2);
		studySummary.setName("TRIAL1");
		studySummary.setOptionalInfo(getOptionalInfo());
		studySummary.setInstanceMetaData(getInstanceMatadatas());
		return studySummary;
	}

}
