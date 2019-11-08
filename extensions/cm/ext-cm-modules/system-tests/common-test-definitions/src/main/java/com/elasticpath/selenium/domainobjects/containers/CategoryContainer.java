/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.selenium.domainobjects.containers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import com.elasticpath.selenium.domainobjects.Category;

/**
 * Category  container class.
 */
public class CategoryContainer {
	private final Map<String, Category> categoryMap = new LinkedHashMap<>();
	private final Map<String, ImmutableList<String>> categoryChildren = new HashMap<>();

	public Map<String, Category> getCategoryMap() {
		return categoryMap;
	}

	/**
	 * Add category into category map.
	 *
	 * @param category new category.
	 */
	public void addCategory(final Category category) {
		categoryMap.put(category.getCategoryName(), category);
	}

	/**
	 * Removes category into category map.
	 *
	 * @param name category name.
	 */
	public void removeCategory(final String name) {
		categoryMap.remove(name);
	}

	/**
	 * Get category name by partial match. Returns first matched category name.
	 * If none of the codes match returns null.
	 *
	 * @param partialName category name for partial match
	 * @return first matched category name. If none of the names match return null.
	 */
	public String getFullCategoryNameByPartialName(final String partialName) {
		return categoryMap.entrySet().stream()
				.filter(entry -> entry.getKey().startsWith(partialName))
				.findFirst()
				.map(Map.Entry::getKey)
				.orElse(null);
	}

	/**
	 * Getting children names.
	 *
	 * @param fullName name of parent.
	 * @return list of child names
	 */
	public List<String> getChildNames(final String fullName) {
		List<String> child = new ArrayList<>();
		for (Map.Entry<String, Category> entry : categoryMap.entrySet()) {
			if (entry.getValue().getParentCategory() != null && entry.getValue().getParentCategory().equals(fullName)) {
				child.add(entry.getValue().getCategoryName());
			}
		}
		return child;
	}

	/**
	 * Getting children codes.
	 *
	 * @param fullName name of parent.
	 * @return list of child codes.
	 */
	public List<String> getChildCodes(final String fullName) {
		List<String> child = new ArrayList<>();
		for (Map.Entry<String, Category> entry : categoryMap.entrySet()) {
			if (entry.getValue().getParentCategory() != null && entry.getValue().getParentCategory().equals(fullName)) {
				child.add(entry.getValue().getCategoryCode());
			}
		}
		return child;
	}

	/**
	 * Getting path localized names.
	 *
	 * @param fullName name of category.
	 * @param language target language.
	 * @return list of path names.
	 */
	public List<String> getPathLocalizedName(final String fullName, final String language) {
		List<String> path = new ArrayList<>();
		if (categoryMap.get(fullName).getParentCategory() == null) {
			return Collections.emptyList();
		} else {
			path.add(categoryMap.get(categoryMap.get(fullName).getParentCategory()).getName(language));
			Category category = categoryMap.get(categoryMap.get(fullName).getParentCategory());
			do {
				if (category.getParentCategory() == null) {
					return path;
				} else {
					path.add(categoryMap.get(category.getParentCategory()).getName(language));
					String parent = category.getParentCategory();
					category = categoryMap.get(parent);
				}
			} while (category.getParentCategory() != null);
		}
		Collections.reverse(path);
		return path;
	}

	/**
	 * Getting path codes.
	 *
	 * @param fullName name of category.
	 * @return list of path codes.
	 */
	public List<String> getPathCodes(final String fullName) {
		List<String> path = new ArrayList<>();
		if (categoryMap.get(fullName).getParentCategory() == null) {
			return Collections.emptyList();
		} else {

			path.add(categoryMap.get(categoryMap.get(fullName).getParentCategory()).getCategoryCode());
			Category category = categoryMap.get(categoryMap.get(fullName).getParentCategory());
			do {
				if (category.getParentCategory() == null) {
					return path;
				} else {
					path.add(categoryMap.get(category.getParentCategory()).getCategoryCode());
					String parent = category.getParentCategory();
					category = categoryMap.get(parent);
				}
			} while (category.getParentCategory() != null);
		}
		Collections.reverse(path);
		return path;
	}

	/**
	 * Adds new ordered list of children for category.
	 *
	 * @param categoryName category name.
	 * @param children     categories children names (in particular order).
	 */
	public void addCategoryChildren(final String categoryName, final ImmutableList<String> children) {
		this.categoryChildren.put(categoryName, children);
	}

	/**
	 * Adds new ordered list of children for category.
	 *
	 * @param categoryName category name.
	 */
	public ImmutableList<String> getCategoryChildren(final String categoryName) {
		return this.categoryChildren.get(categoryName);
	}

	/**
	 * Adds new ordered list of children for category.
	 *
	 * @param categoryName category name.
	 */
	public ImmutableList<String> getCategoryChildrenCodes(final String categoryName) {
		ImmutableList<String> childrenNames = this.categoryChildren.get(categoryName);
		List<String> childrenCodes = new LinkedList<>();
		for (String name : childrenNames) {
			childrenCodes.add(categoryMap.get(name).getCategoryCode());
		}
		return new ImmutableList.Builder<String>().addAll(childrenCodes).build();
	}
}
