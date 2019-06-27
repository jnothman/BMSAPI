package org.ibp.api.domain.role;

import org.generationcp.middleware.pojos.workbench.RoleType;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class RoleTypeDto {

	private Integer id;

	private String name;

	public RoleTypeDto() {
	}

	public RoleTypeDto(final RoleType roleType) {
		this.id = roleType.getId();
		this.name = roleType.getName();
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
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
