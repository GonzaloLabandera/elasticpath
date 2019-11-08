/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.selenium.domainobjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cart Item Modifier Group.
 */
public class CartItemModifierGroup {
	private final Map<String, String> names = new HashMap<>();
	private final List<CartItemModiferGroupField> cartGroupFields = new ArrayList<>();
	private String groupCode;
	private String grouopName;

	public String getGroupCode() {
		return groupCode;
	}

	public void setGroupCode(final String groupCode) {
		this.groupCode = groupCode;
	}

	public String getGrouopName() {
		return grouopName;
	}

	public void setGrouopName(final String grouopName) {
		this.grouopName = grouopName;
	}

	public String getName(final String language) {
		return names.get(language);
	}

	public void setName(final String language, final String name) {
		names.put(language, name);
	}

	public List<CartItemModiferGroupField> getCartGroupsFields() {
		return cartGroupFields;
	}

	public void addField(final CartItemModiferGroupField field) {
		if (field == null) {
			return;
		}
		cartGroupFields.add(field);
	}

	/**
	 * Get group field code by partial match. Returns first matched group field code .
	 * If none of the codes match returns empty string.
	 *
	 * @param code group field code for partial match
	 * @return first matched group field code. If none of the codes match return empty string
	 */
	public String getGroupFieldCodeByPartialCode(final String code) {
		String resultCode = "";
		for (int i = 0; i < getCartGroupsFields().size(); i++) {
			if (getCartGroupsFields().get(i).getFieldCode().startsWith(code)) {
				resultCode = getCartGroupsFields().get(i).getFieldCode();
			}
		}
		return resultCode;
	}

	/**
	 * Get group field by group field code. Returns group field .
	 * If none of the codes match returns empty object.
	 *
	 * @param code group field code
	 * @return matched group field. If none of the codes match return empty object
	 */
	public CartItemModiferGroupField getFieldByCode(final String code) {
		CartItemModiferGroupField resultField = new CartItemModiferGroupField();
		String fullCode = getGroupFieldCodeByPartialCode(code);
		for (int i = 0; i < getCartGroupsFields().size(); i++) {
			if (getCartGroupsFields().get(i).getFieldCode().equals(fullCode)) {
				resultField = getCartGroupsFields().get(i);
			}
		}
		return resultField;
	}
}
