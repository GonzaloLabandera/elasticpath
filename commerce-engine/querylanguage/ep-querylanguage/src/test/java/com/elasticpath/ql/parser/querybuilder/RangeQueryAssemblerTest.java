/*
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.ql.parser.querybuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.apache.lucene.search.Query;
import org.junit.Before;
import org.junit.Test;

import com.elasticpath.ql.parser.EpQLField;
import com.elasticpath.ql.parser.EpQLFieldDescriptor;
import com.elasticpath.ql.parser.EpQLFieldType;
import com.elasticpath.ql.parser.EpQLTerm;
import com.elasticpath.ql.parser.NativeResolvedTerm;
import com.elasticpath.ql.parser.gen.ParseException;
import com.elasticpath.ql.parser.querybuilder.impl.LuceneRangeSubQueryBuilder;

/**
 * Tests for RangeQueryBuilder.
 */
public class RangeQueryAssemblerTest {

	private LuceneRangeSubQueryBuilder queryAssembler;

	/**
	 * Setup test.
	 */
	@Before
	public void setUp() throws Exception {
		queryAssembler = new LuceneRangeSubQueryBuilder();
	}

	/**
	 * Test the range query happy path.
	 * @throws ParseException in case of error
	 */
	@Test
	public void testGetRangeQueryHappyPath() throws ParseException {
		EpQLTerm epQLTerm = new EpQLTerm(EpQLField.PRODUCT_CODE.getFieldName(), null, null, "=", null);
		assertThat(queryAssembler.getRangeQuery(null, epQLTerm, null)).isNull();
	}

	/**
	 * Test that range query throws a parse exception when an invalid operator is used.
	 * @throws ParseException in case of error
	 */
	@Test
	public void testGetRangeQueryInvalidOperator() throws ParseException {
		final EpQLTerm epQLTerm = new EpQLTerm(EpQLField.PRODUCT_CODE.getFieldName(), null, null, "&", null);
		assertThatThrownBy(() -> queryAssembler.getRangeQuery(null, epQLTerm, null))
			.isInstanceOf(ParseException.class)
			.hasMessageStartingWith("Invalid operator");
	}

	/**
	 * Test that range query throws a parse exception when an invalid type is used.
	 * @throws ParseException in case of error
	 */
	@Test
	public void testGetRangeQueryBadType() throws ParseException {
		NativeResolvedTerm solrAssembler = new NativeResolvedTerm(new EpQLFieldDescriptor());
		final EpQLTerm epQLTerm = new EpQLTerm(EpQLField.PRODUCT_CODE.getFieldName(), null, null, ">", null);
		assertThatThrownBy(() -> queryAssembler.getRangeQuery(solrAssembler, epQLTerm, null))
			.isInstanceOf(ParseException.class)
			.hasMessage("Range query is not supported for this field: ProductCode of type: STRING");
	}

	/**
	 * Test method for
	 * {@link com.elasticpath.ql.parser.RangeQueryAssemblerImpl#getDateRangeQuery(com.elasticpath.ql.parser.assembler.NativeResolvedTerm, 
	 * java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetDateRangeQuery() throws ParseException {
		NativeResolvedTerm solrAssembler = new NativeResolvedTerm(new EpQLFieldDescriptor());
		solrAssembler.getFieldDescriptor().setType(EpQLFieldType.DATE);
		solrAssembler.setResolvedField("startDate");
		String date = "2008-07-31T23:59:59.999Z";

		EpQLTerm epQLTerm = new EpQLTerm(EpQLField.PRODUCT_START_DATE.getFieldName(), null, null, ">", null);
		Query rangeQuery = queryAssembler.getRangeQuery(solrAssembler, epQLTerm, date);
		assertThat(rangeQuery).isNotNull();
		assertThat(rangeQuery.toString()).isEqualTo("startDate:{2008-07-31T23:59:59.999Z TO *}");

		epQLTerm = new EpQLTerm(EpQLField.PRODUCT_START_DATE.getFieldName(), null, null, ">=", null);
		rangeQuery = queryAssembler.getRangeQuery(solrAssembler, epQLTerm, date);
		assertThat(rangeQuery).isNotNull();
		assertThat(rangeQuery.toString()).isEqualTo("startDate:[2008-07-31T23:59:59.999Z TO *]");

		epQLTerm = new EpQLTerm(EpQLField.PRODUCT_START_DATE.getFieldName(), null, null, "<", null);
		rangeQuery = queryAssembler.getRangeQuery(solrAssembler, epQLTerm, date);
		assertThat(rangeQuery).isNotNull();
		assertThat(rangeQuery.toString()).isEqualTo("startDate:{* TO 2008-07-31T23:59:59.999Z}");

		epQLTerm = new EpQLTerm(EpQLField.PRODUCT_START_DATE.getFieldName(), null, null, "<=", null);
		rangeQuery = queryAssembler.getRangeQuery(solrAssembler, epQLTerm, date);
		assertThat(rangeQuery).isNotNull();
		assertThat(rangeQuery.toString()).isEqualTo("startDate:[* TO 2008-07-31T23:59:59.999Z]");
	}

	/**
	 * Test method for
	 * {@link com.elasticpath.ql.parser.RangeQueryAssemblerImpl#getRangeQuery(com.elasticpath.ql.parser.assembler.NativeResolvedTerm, 
	 * java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetFloatRangeQuery() throws ParseException {
		NativeResolvedTerm solrAssembler = new NativeResolvedTerm(new EpQLFieldDescriptor());
		solrAssembler.getFieldDescriptor().setType(EpQLFieldType.FLOAT);
		solrAssembler.setResolvedField("price");
		String floatString = "65.21";

		EpQLTerm epQLTerm = new EpQLTerm(EpQLField.PRODUCT_PRICE.getFieldName(), null, null, ">", null);
		Query rangeQuery = queryAssembler.getRangeQuery(solrAssembler, epQLTerm, floatString);
		assertThat(rangeQuery).isNotNull();
		assertThat(rangeQuery.toString()).isEqualTo("price:{65.21 TO *}");
	}
}
