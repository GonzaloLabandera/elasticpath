package com.elasticpath.selenium.domainobjects;

import java.util.List;

/**
 * Category class.
 */
public class Store {
	private String code;
	private String name;
	private List<String> storeCodesList;

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public List<String> getStoreCodesList() {
		return storeCodesList;
	}

	public void setStoreCodesList(final List<String> storeCodesList) {
		this.storeCodesList = storeCodesList;
	}
}
