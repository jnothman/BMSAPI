
package org.ibp.api.java.program;

import java.util.List;
import java.util.Map;

import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.program.ProgramDetailsDto;
import org.generationcp.middleware.service.api.program.ProgramFilters;
import org.ibp.api.domain.program.ProgramSummary;

public interface ProgramService {

	List<ProgramSummary> listProgramsByCropName(final String cropName);

	List<ProgramSummary> listProgramsByCropNameAndUser(WorkbenchUser user, String cropName);

	List<ProgramDetailsDto> getProgramsByFilter(final int pageNumber, final int pageSize, final Map<ProgramFilters, Object> filters);

	long countProgramsByFilter(final Map<ProgramFilters, Object> filter);

	ProgramSummary getByUUIDAndCrop(String crop, String programUUID);
}
