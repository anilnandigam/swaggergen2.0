package com.tmo.swagger.model;

import java.util.List;

public class RowData {
	private String xpath;
	private String min;
	private String max;
	private String xsdType;
	private String jsonType;
	private String jsonFormat;
	private List<String>  enumcell;
	public String getXpath() {
		return xpath;
	}
	public void setXpath(String xpath) {
		this.xpath = xpath;
	}
	public String getMin() {
		return min;
	}
	public void setMin(String min) {
		this.min = min;
	}
	public String getMax() {
		return max;
	}
	public void setMax(String max) {
		this.max = max;
	}
	public String getXsdType() {
		return xsdType;
	}
	public void setXsdType(String xsdType) {
		this.xsdType = xsdType;
	}
	public String getJsonType() {
		return jsonType;
	}
	public void setJsonType(String jsonType) {
		this.jsonType = jsonType;
	}
	public String getJsonFormat() {
		return jsonFormat;
	}
	public void setJsonFormat(String jsonFormat) {
		this.jsonFormat = jsonFormat;
	}
	public List<String> getEnumcell() {
		return enumcell;
	}
	public void setEnumcell(List<String> enumcell) {
		this.enumcell = enumcell;
	}


}
