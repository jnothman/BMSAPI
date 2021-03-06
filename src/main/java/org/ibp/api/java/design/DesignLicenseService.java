package org.ibp.api.java.design;

import org.ibp.api.domain.design.DesignLicenseInfo;
import org.ibp.api.domain.design.License;
import org.ibp.api.exception.BVLicenseParseException;

public interface DesignLicenseService {

	boolean isExpired();

	License getLicenseInfo();

}
