package org.ibp.api.java.study;

import java.util.List;

import org.ibp.api.domain.study.StudySummary;


public interface StudyService {
	
	List<StudySummary> listAllStudies();

}