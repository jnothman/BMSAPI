package org.ibp.api.java.impl.middleware.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Component
public class ContextResolverImpl implements ContextResolver {
	
	private static final Logger LOG = LoggerFactory.getLogger(ContextResolverImpl.class);

    @Override
    public String resolveDatabaseFromUrl() throws ContextResolutionException {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        if(request == null) {
        	throw new ContextResolutionException("Request is null");
        }

        String path = request.getRequestURI().substring(request.getContextPath().length());
        LOG.debug("Request path: " + path);
        String[] parts = path.trim().toLowerCase().split("/");
        if(parts.length < 3){
            LOG.error("BAD URL Request :" + path);
            throw new ContextResolutionException("BAD URL:" + path, new Exception("Expecting crop name"));
        }
        LOG.debug("Crop Name: " + parts[2]);
        return String.format(Constants.DB_NAME_FORMAT, parts[2]);
    }
}