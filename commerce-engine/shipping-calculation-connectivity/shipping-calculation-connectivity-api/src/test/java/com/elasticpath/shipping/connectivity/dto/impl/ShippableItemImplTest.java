/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.shipping.connectivity.dto.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.junit.Before;
import org.junit.Test;

import com.elasticpath.shipping.connectivity.dto.ShippableItem;

/**
 * Test cases for {@link ShippableItemImpl}.
 */
public class ShippableItemImplTest {

	private static final int QUANTITY = 200;
	private static final String SKU_GUID = "testSkuGuid";
	private static final BigDecimal WIDTH = new BigDecimal(100);
	private static final BigDecimal LENGTH = new BigDecimal(101);
	private static final BigDecimal HEIGHT = new BigDecimal(102);
	private static final BigDecimal WEIGHT = new BigDecimal(103);

	private static final Map<String, String> FIELD = ImmutableMap.of("key1", "value1", "key2", "value2");
	private static final Map<String, String> EMPTY_FIELD = Collections.emptyMap();

	private static final String FIELD_KEY = "key3";
	private static final String FIELD_VALUE = "value3";
	private static final Map<String, String> OTHER_FIELD = ImmutableMap.of("key1", "value1", "key2", "value2", FIELD_KEY, FIELD_VALUE);

	private ShippableItemImpl shippableItem;

	@Before
	public void setUp() {

		this.shippableItem = buildEmpty();

	}

	@Test
	public void testWidth() {

		shippableItem.setWidth(WIDTH);
		assertThat(shippableItem.getWidth()).isEqualTo(WIDTH);

	}

	@Test
	public void testLength() {

		shippableItem.setLength(LENGTH);
		assertThat(shippableItem.getLength()).isEqualTo(LENGTH);

	}

	@Test
	public void testHeight() {

		shippableItem.setHeight(HEIGHT);
		assertThat(shippableItem.getHeight()).isEqualTo(HEIGHT);

	}

	@Test
	public void testWeight() {

		shippableItem.setWeight(WEIGHT);
		assertThat(shippableItem.getWeight()).isEqualTo(WEIGHT);

	}

	@Test
	public void testSkuGuid() {

		shippableItem.setSkuGuid(SKU_GUID);
		assertThat(shippableItem.getSkuGuid()).isEqualTo(SKU_GUID);

	}

	@Test
	public void testFieldMap() {

		shippableItem.setFields(OTHER_FIELD);
		shippableItem.setFields(FIELD);

		assertThat(shippableItem.getFields()).isNotEqualTo(EMPTY_FIELD);
		assertThat(shippableItem.getFields()).isNotEqualTo(OTHER_FIELD);
		assertThat(shippableItem.getFields()).isEqualTo(FIELD);

	}

	@Test(expected = UnsupportedOperationException.class)
	public void testInsertingInImmutableFieldMap() {

		shippableItem.setFields(FIELD);
		shippableItem.getFields().put(FIELD_KEY, FIELD_VALUE);

	}

	@Test
	public void testInsertingInMutableFieldMap() {

		shippableItem.setFields(FIELD);
		shippableItem.setField(FIELD_KEY, FIELD_VALUE);

		assertThat(shippableItem.getFields()).isNotEqualTo(FIELD);
		assertThat(shippableItem.getFields()).isEqualTo(OTHER_FIELD);

	}

	@Test(expected =  NullPointerException.class)
	public void testInsertingNullFieldInMutableFieldMap() {

		shippableItem.setFields(FIELD);
		shippableItem.setFields(null);

	}

	@Test
	public void testInsertingNullPairInMutableFieldMap() {

		shippableItem.setField(null, null);

		assertThat(shippableItem.getFields()).isNotEqualTo(EMPTY_FIELD);
		assertThat(shippableItem.getFields().containsKey(null)).isTrue();
		assertThat(shippableItem.getFields().containsValue(null)).isTrue();

	}

	@Test
	public void testInsertingValidPairInMutableFieldMap() {

		shippableItem.setField(FIELD_KEY, FIELD_VALUE);

		assertThat(shippableItem.getFields()).isNotEqualTo(EMPTY_FIELD);
		assertThat(shippableItem.getFields().containsKey(FIELD_KEY)).isTrue();
		assertThat(shippableItem.getFields().containsValue(FIELD_VALUE)).isTrue();

	}

	@Test
	public void testQuantity() {

		shippableItem.setQuantity(QUANTITY);
		assertThat(shippableItem.getQuantity()).isEqualTo(QUANTITY);

	}

	@Test
	public void testToString() {

		final ShippableItem shippableItem = buildDefault();

		final String resultToString = shippableItem.toString();

		assertThat(resultToString).contains(HEIGHT.toString(), WIDTH.toString(), LENGTH.toString(), WEIGHT.toString(), FIELD.toString(),
			String.valueOf(QUANTITY), SKU_GUID);

	}

	@Test
	public void testEquals() {
		new EqualsTester().addEqualityGroup(shippableItem, buildEmpty())
				.addEqualityGroup(buildDefault())
				.addEqualityGroup(build(HEIGHT, null, null, null, 0, EMPTY_FIELD, null))
				.addEqualityGroup(build(null, WIDTH, null, null, 0, EMPTY_FIELD, null))
				.addEqualityGroup(build(null, null, LENGTH, null, 0, EMPTY_FIELD, null))
				.addEqualityGroup(build(null, null, null, WEIGHT, 0, EMPTY_FIELD, null))
				.addEqualityGroup(build(null, null, null, null, QUANTITY, EMPTY_FIELD, null))
				.addEqualityGroup(build(null, null, null, null, 0, FIELD, null))
				.addEqualityGroup(build(null, null, null, null, 0, EMPTY_FIELD, SKU_GUID))
				.testEquals();
	}

	private ShippableItemImpl buildEmpty() {
		return build(null, null, null, null, 0, EMPTY_FIELD, null);
	}

	private ShippableItemImpl buildDefault() {
		return build(HEIGHT, WIDTH, LENGTH, WEIGHT, QUANTITY, FIELD, SKU_GUID);
	}

	@SuppressWarnings("parameternumber")
	private ShippableItemImpl build(final BigDecimal height,
									final BigDecimal width,
									final BigDecimal length,
									final BigDecimal weight,
									final int quantity,
									final Map<String, String> field,
									final String skuGuid) {

		final ShippableItemImpl shippableItem = new ShippableItemImpl();

		shippableItem.setHeight(height);
		shippableItem.setWidth(width);
		shippableItem.setLength(length);
		shippableItem.setWeight(weight);
		shippableItem.setQuantity(quantity);
		shippableItem.setFields(field);
		shippableItem.setSkuGuid(skuGuid);

		return shippableItem;
	}

}
