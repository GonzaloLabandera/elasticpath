/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.ql.custom.customer;

import java.util.List;
import java.util.stream.Collectors;

import com.elasticpath.search.searchengine.SearchResultConverter;

/**
 * SQL searcher will find a list of arrays of objects, first element of the array is a setting path, second if context value.
 */
public class CustomerSearchResultConverter implements SearchResultConverter<String, Object> {

	@Override
	public List<String> convert(final List<Object> searchResult) {
		return searchResult.stream().map(result -> (String) result).collect(Collectors.toList());
	}
}
