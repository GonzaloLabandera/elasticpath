/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.ql.parser.valueresolver.impl;

import java.util.ArrayList;
import java.util.List;

import com.elasticpath.ql.parser.EpQLFieldType;
import com.elasticpath.ql.parser.EpQLTerm;
import com.elasticpath.ql.parser.EpQuery;
import com.elasticpath.ql.parser.gen.ParseException;

/**
 * Checks a value to be in a set of allowed enum values.
 */
public class JPQLEnumValueResolver extends EnumValueResolver {

	/**
	 * Checks that epQLTerm contains configured allowed enum value.
	 *
	 * @param epQLTerm  EPQL term
	 * @param fieldType type of field value passed in EPQL query
	 * @param epQuery   the epQuery
	 * @return list of values corresponding to given value of EpQL field
	 * @throws ParseException if value is not allowed enum value or value is quoted or the field type is not enum
	 */
	@Override
	public List<String> resolve(final EpQLTerm epQLTerm, final EpQLFieldType fieldType, final EpQuery epQuery) throws ParseException {
		verifyFieldTypeIsEnum(epQLTerm, fieldType);
		verifyQuoteAbsenceValue(epQLTerm);
		verifyEnumValue(epQLTerm);

		final List<String> resolvedValues = new ArrayList<>();
		resolvedValues.add("'" + epQLTerm.getQueryText() + "'");
		return resolvedValues;
	}

}
