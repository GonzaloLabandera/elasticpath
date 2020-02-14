/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.shipping.connectivity;

import static com.elasticpath.shipping.connectivity.commons.constants.ShippingContextIdNames.NON_CACHING_SHIPPING_CALCULATION_SERVICE;
import static com.elasticpath.shipping.connectivity.commons.constants.ShippingContextIdNames.PRICED_SHIPPABLE_ITEM;
import static com.elasticpath.shipping.connectivity.commons.constants.ShippingContextIdNames.PRICED_SHIPPABLE_ITEM_BUILDER_SUPPLIER;
import static com.elasticpath.shipping.connectivity.commons.constants.ShippingContextIdNames.PRICED_SHIPPABLE_ITEM_CONTAINER;
import static com.elasticpath.shipping.connectivity.commons.constants.ShippingContextIdNames.PRICED_SHIPPABLE_ITEM_CONTAINER_BUILDER_SUPPLIER;
import static com.elasticpath.shipping.connectivity.commons.constants.ShippingContextIdNames.PRICED_SHIPPING_CALCULATION_PLUGIN_SELECTOR;
import static com.elasticpath.shipping.connectivity.commons.constants.ShippingContextIdNames.SHIPPING_ADDRESS;
import static com.elasticpath.shipping.connectivity.commons.constants.ShippingContextIdNames.SHIPPING_ADDRESS_BUILDER;
import static com.elasticpath.shipping.connectivity.commons.constants.ShippingContextIdNames.SHIPPING_ADDRESS_BUILDER_SUPPLIER;
import static com.elasticpath.shipping.connectivity.commons.constants.ShippingContextIdNames.SHIPPING_CALCULATION_RESULT;
import static com.elasticpath.shipping.connectivity.commons.constants.ShippingContextIdNames.SHIPPING_CALCULATION_RESULT_BUILDER;
import static com.elasticpath.shipping.connectivity.commons.constants.ShippingContextIdNames.SHIPPING_CALCULATION_RESULT_BUILDER_SUPPLIER;
import static com.elasticpath.shipping.connectivity.commons.constants.ShippingContextIdNames.SHIPPING_CALCULATION_SERVICE;
import static com.elasticpath.shipping.connectivity.commons.constants.ShippingContextIdNames.SHIPPING_OPTION;
import static com.elasticpath.shipping.connectivity.commons.constants.ShippingContextIdNames.SHIPPING_OPTION_BUILDER;
import static com.elasticpath.shipping.connectivity.commons.constants.ShippingContextIdNames.SHIPPING_OPTION_BUILDER_SUPPLIER;
import static com.elasticpath.shipping.connectivity.commons.constants.ShippingContextIdNames.UNPRICED_SHIPPABLE_ITEM;
import static com.elasticpath.shipping.connectivity.commons.constants.ShippingContextIdNames.UNPRICED_SHIPPABLE_ITEM_BUILDER_SUPPLIER;
import static com.elasticpath.shipping.connectivity.commons.constants.ShippingContextIdNames.UNPRICED_SHIPPABLE_ITEM_CONTAINER;
import static com.elasticpath.shipping.connectivity.commons.constants.ShippingContextIdNames.UNPRICED_SHIPPABLE_ITEM_CONTAINER_BUILDER_SUPPLIER;
import static com.elasticpath.shipping.connectivity.commons.constants.ShippingContextIdNames.UNPRICED_SHIPPING_CALCULATION_PLUGIN_LIST;
import static com.elasticpath.shipping.connectivity.commons.constants.ShippingContextIdNames.UNPRICED_SHIPPING_CALCULATION_PLUGIN_SELECTOR;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Supplier;

import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.elasticpath.shipping.connectivity.dto.builder.ShippingAddressBuilder;
import com.elasticpath.shipping.connectivity.dto.builder.ShippingCalculationResultBuilder;
import com.elasticpath.shipping.connectivity.dto.builder.ShippingOptionBuilder;
import com.elasticpath.shipping.connectivity.dto.impl.PricedShippableItemContainerImpl;
import com.elasticpath.shipping.connectivity.dto.impl.PricedShippableItemImpl;
import com.elasticpath.shipping.connectivity.dto.impl.ShippableItemContainerImpl;
import com.elasticpath.shipping.connectivity.dto.impl.ShippableItemImpl;
import com.elasticpath.shipping.connectivity.dto.impl.ShippingAddressImpl;
import com.elasticpath.shipping.connectivity.dto.impl.ShippingCalculationResultImpl;
import com.elasticpath.shipping.connectivity.dto.impl.ShippingOptionImpl;
import com.elasticpath.shipping.connectivity.service.ShippingCalculationService;
import com.elasticpath.shipping.connectivity.service.selector.impl.StaticPricedShippingCalculationPluginSelectorImpl;
import com.elasticpath.shipping.connectivity.service.selector.impl.StaticUnpricedShippingCalculationPluginSelectorImpl;

/**
 * Test the validity of the Spring XML files.
 */
public class ShippingCalculationConnectivityAPISpringXmlVerificationTest {
	private ConfigurableApplicationContext context;

	@Before
	public void setUp() {
		context = new ClassPathXmlApplicationContext("shipping-calculation-connectivity-api.xml", "mocked-services.xml");
	}

	/**
	 * Test spring xml.
	 */
	@Test
	public void testGetBeansDefinitionCount() {

		assertThat(context.getBeanDefinitionCount()).as("The bean count should be greater than 0").isGreaterThan(0);
	}

	@Test
	public void testLoadServiceBeans() {

		final SoftAssertions soft = new SoftAssertions();

		soft.assertThat(context.getBean(SHIPPING_CALCULATION_SERVICE)).isNotNull().isInstanceOf(ShippingCalculationService.class);
		soft.assertThat(context.getBean(UNPRICED_SHIPPING_CALCULATION_PLUGIN_SELECTOR)).isNotNull()
				.isInstanceOf(StaticUnpricedShippingCalculationPluginSelectorImpl.class);
		soft.assertThat(context.getBean(PRICED_SHIPPING_CALCULATION_PLUGIN_SELECTOR)).isNotNull()
				.isInstanceOf(StaticPricedShippingCalculationPluginSelectorImpl.class);
		soft.assertThat(context.getBean(UNPRICED_SHIPPING_CALCULATION_PLUGIN_LIST)).isNotNull().isInstanceOf(List.class);
		soft.assertThat(context.getBean(UNPRICED_SHIPPING_CALCULATION_PLUGIN_LIST)).isNotNull().isInstanceOf(List.class);
		soft.assertThat(context.getBean(NON_CACHING_SHIPPING_CALCULATION_SERVICE)).isNotNull().isInstanceOf(ShippingCalculationService.class);

		soft.assertAll();
	}

	@Test
	public void testLoadPrototypeBeans() {

		final SoftAssertions soft = new SoftAssertions();

		soft.assertThat(context.getBean(SHIPPING_OPTION)).isNotSameAs(context.getBean(SHIPPING_OPTION))
				.isInstanceOf(ShippingOptionImpl.class);
		soft.assertThat(context.getBean(SHIPPING_CALCULATION_RESULT)).isNotSameAs(context.getBean(SHIPPING_CALCULATION_RESULT))
				.isInstanceOf(ShippingCalculationResultImpl.class);
		soft.assertThat(context.getBean(SHIPPING_ADDRESS)).isNotSameAs(context.getBean(SHIPPING_ADDRESS))
				.isInstanceOf(ShippingAddressImpl.class);
		soft.assertThat(context.getBean(UNPRICED_SHIPPABLE_ITEM)).isNotSameAs(context.getBean(UNPRICED_SHIPPABLE_ITEM))
				.isInstanceOf(ShippableItemImpl.class);
		soft.assertThat(context.getBean(PRICED_SHIPPABLE_ITEM)).isNotSameAs(context.getBean(PRICED_SHIPPABLE_ITEM))
				.isInstanceOf(PricedShippableItemImpl.class);
		soft.assertThat(context.getBean(UNPRICED_SHIPPABLE_ITEM_CONTAINER)).isNotSameAs(context.getBean(UNPRICED_SHIPPABLE_ITEM_CONTAINER))
				.isInstanceOf(ShippableItemContainerImpl.class);
		soft.assertThat(context.getBean(PRICED_SHIPPABLE_ITEM_CONTAINER)).isNotSameAs(context.getBean(PRICED_SHIPPABLE_ITEM_CONTAINER))
				.isInstanceOf(PricedShippableItemContainerImpl.class);

		soft.assertAll();
	}

	@Test
	public void testLoadBuilderBeans() {

		final SoftAssertions soft = new SoftAssertions();

		soft.assertThat(context.getBean(SHIPPING_OPTION_BUILDER)).isNotSameAs(context.getBean(SHIPPING_OPTION_BUILDER))
				.isInstanceOf(ShippingOptionBuilder.class);
		soft.assertThat(context.getBean(SHIPPING_CALCULATION_RESULT_BUILDER)).isNotSameAs(context.getBean(SHIPPING_CALCULATION_RESULT_BUILDER))
				.isInstanceOf(ShippingCalculationResultBuilder.class);
		soft.assertThat(context.getBean(SHIPPING_ADDRESS_BUILDER)).isNotSameAs(context.getBean(SHIPPING_ADDRESS_BUILDER))
				.isInstanceOf(ShippingAddressBuilder.class);

		soft.assertAll();
	}

	@Test
	public void testLoadSupplierBeans() {

		final SoftAssertions soft = new SoftAssertions();

		soft.assertThat(context.getBean(SHIPPING_OPTION_BUILDER_SUPPLIER)).isSameAs(context.getBean(SHIPPING_OPTION_BUILDER_SUPPLIER))
				.isInstanceOf(Supplier.class);
		soft.assertThat(context.getBean(SHIPPING_CALCULATION_RESULT_BUILDER_SUPPLIER)).isSameAs(
				context.getBean(SHIPPING_CALCULATION_RESULT_BUILDER_SUPPLIER))
				.isInstanceOf(Supplier.class);
		soft.assertThat(context.getBean(SHIPPING_ADDRESS_BUILDER_SUPPLIER)).isSameAs(context.getBean(SHIPPING_ADDRESS_BUILDER_SUPPLIER))
				.isInstanceOf(Supplier.class);
		soft.assertThat(context.getBean(UNPRICED_SHIPPABLE_ITEM_BUILDER_SUPPLIER)).isSameAs(
				context.getBean(UNPRICED_SHIPPABLE_ITEM_BUILDER_SUPPLIER))
				.isInstanceOf(Supplier.class);
		soft.assertThat(context.getBean(PRICED_SHIPPABLE_ITEM_BUILDER_SUPPLIER)).isSameAs(context.getBean(PRICED_SHIPPABLE_ITEM_BUILDER_SUPPLIER))
				.isInstanceOf(Supplier.class);
		soft.assertThat(context.getBean(UNPRICED_SHIPPABLE_ITEM_CONTAINER_BUILDER_SUPPLIER))
				.isSameAs(context.getBean(UNPRICED_SHIPPABLE_ITEM_CONTAINER_BUILDER_SUPPLIER))
				.isInstanceOf(Supplier.class);
		soft.assertThat(context.getBean(PRICED_SHIPPABLE_ITEM_CONTAINER_BUILDER_SUPPLIER))
				.isSameAs(context.getBean(PRICED_SHIPPABLE_ITEM_CONTAINER_BUILDER_SUPPLIER))
				.isInstanceOf(Supplier.class);

		soft.assertAll();
	}

	/**
	 * Closes a context after tests.
	 */
	@After
	public void closeContext() {
		if (context != null) {
			context.close();
		}
	}


}
