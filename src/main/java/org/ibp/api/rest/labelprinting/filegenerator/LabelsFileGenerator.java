package org.ibp.api.rest.labelprinting.filegenerator;

import org.ibp.api.rest.labelprinting.domain.LabelsData;
import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;

import java.io.File;
import java.io.IOException;

/**
 * Created by clarysabel on 2/7/19.
 */
public interface LabelsFileGenerator {

	File generate(final LabelsGeneratorInput labelsGeneratorInput, final LabelsData labelsData) throws IOException;

}
