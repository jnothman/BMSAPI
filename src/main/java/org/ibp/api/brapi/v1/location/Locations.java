
package org.ibp.api.brapi.v1.location;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"metadata", "result"})
public class Locations {

	private Metadata metadata;

	private Result<Location> result;

	/**
	 * No args constructor required by serialization libraries.
	 */
	public Locations() {
	}

	/**
	 *
	 * @param result
	 * @param metadata
	 */
	public Locations(final Metadata metadata, final Result<Location> result) {
		this.metadata = metadata;
		this.result = result;
	}

	/**
	 *
	 * @return The metadata
	 */
	public Metadata getMetadata() {
		return this.metadata;
	}

	/**
	 *
	 * @param metadata The metadata
	 */
	public void setMetadata(final Metadata metadata) {
		this.metadata = metadata;
	}

	public Locations withMetadata(final Metadata metadata) {
		this.metadata = metadata;
		return this;
	}

	/**
	 *
	 * @return The result
	 */
	public Result<Location> getResult() {
		return this.result;
	}

	/**
	 *
	 * @param result The result
	 */
	public void setResult(final Result<Location> result) {
		this.result = result;
	}

	public Locations withResult(final Result<Location> result) {
		this.result = result;
		return this;
	}

	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}
}
