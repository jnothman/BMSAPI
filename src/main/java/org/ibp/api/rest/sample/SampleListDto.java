
package org.ibp.api.rest.sample;

import java.util.List;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import com.fasterxml.jackson.annotation.JsonInclude;

@AutoProperty
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SampleListDto {

	private String description;

	private String notes;

	private String createdBy;

	private Integer selectionVariableId;

	private List<Integer> instanceIds;

	private String takenBy;

	private String samplingDate;

	private Integer studyId;

	private String cropName;

	public String getDescription() {
		return this.description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getNotes() {
		return this.notes;
	}

	public void setNotes(final String notes) {
		this.notes = notes;
	}

	public String getCreatedBy() {
		return this.createdBy;
	}

	public void setCreatedBy(final String createdBy) {
		this.createdBy = createdBy;
	}

	public Integer getSelectionVariableId() {
		return this.selectionVariableId;
	}

	public void setSelectionVariableId(final Integer selectionVariableId) {
		this.selectionVariableId = selectionVariableId;
	}

	public List<Integer> getInstanceIds() {
		return this.instanceIds;
	}

	public void setInstanceIds(final List<Integer> instanceIds) {
		this.instanceIds = instanceIds;
	}

	public String getTakenBy() {
		return this.takenBy;
	}

	public void setTakenBy(final String takenBy) {
		this.takenBy = takenBy;
	}

	public String getSamplingDate() {
		return this.samplingDate;
	}

	public void setSamplingDate(final String samplingDate) {
		this.samplingDate = samplingDate;
	}

	public Integer getStudyId() {
		return this.studyId;
	}

	public void setStudyId(final Integer studyId) {
		this.studyId = studyId;
	}

	public String getCropName() {
		return this.cropName;
	}

	public void setCropName(final String cropName) {
		this.cropName = cropName;
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}

	@Override
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}
}