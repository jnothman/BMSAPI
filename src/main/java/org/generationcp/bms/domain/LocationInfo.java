package org.generationcp.bms.domain;

import com.wordnik.swagger.annotations.ApiModel;

@ApiModel("Location Information")
public class LocationInfo {

	private Integer id;
	private String name;

	private String label1;
	private String label2;
	private String label3;

	public LocationInfo() {}
	
	public LocationInfo(Integer locationId, String locationName) {

		this.id = locationId;
		this.name = locationName;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel1() {
		return label1;
	}

	public void setLabel1(String label1) {
		this.label1 = label1;
	}

	public String getLabel2() {
		return label2;
	}

	public void setLabel2(String label2) {
		this.label2 = label2;
	}

	public String getLabel3() {
		return label3;
	}

	public void setLabel3(String label3) {
		this.label3 = label3;
	}

}