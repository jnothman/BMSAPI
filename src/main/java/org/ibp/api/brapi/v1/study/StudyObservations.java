package org.ibp.api.brapi.v1.study;

import org.ibp.api.brapi.v1.common.Metadata;
import org.pojomatic.Pojomatic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "metadata", "result" })
public class StudyObservations {

	private Metadata metadata;

	private StudyObservationTable result;

	/**
	 *
	 * @return Metadata
	 */
	public Metadata getMetadata() {
		return metadata;
	}

	/**
	 *
	 * @param metadata
	 *            The metadata
	 * @return this
	 */
	public StudyObservations setMetadata(final Metadata metadata) {
		this.metadata = metadata;
		return this;
	}

	/**
	 *
	 * @return The result
	 */
	public StudyObservationTable getResult() {
		return result;
	}

	/**
	 *
	 * @param result
	 *            The result
	 * @return this
	 */
	public StudyObservations setResult(final StudyObservationTable result) {
		this.result = result;
		return this;
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
	public boolean equals(Object o) {
		return Pojomatic.equals(this, o);
	}
}
