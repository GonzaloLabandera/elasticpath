/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository;

import java.util.List;

import com.google.common.collect.ImmutableMap;

import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.definition.carts.CartsIdentifier;
import com.elasticpath.rest.definition.carts.LineItemIdentifier;
import com.elasticpath.rest.definition.carts.LineItemsIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionValueIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionsIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentsIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionNestedComponentsIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionValueIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionsIdentifier;
import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.prices.PriceForCartLineItemIdentifier;
import com.elasticpath.rest.definition.prices.PriceForItemIdentifier;
import com.elasticpath.rest.definition.prices.PriceForItemdefinitionIdentifier;
import com.elasticpath.rest.definition.prices.PriceForShipmentLineItemIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemsIdentifier;
import com.elasticpath.rest.definition.purchases.PurchasesIdentifier;
import com.elasticpath.rest.definition.shipments.ShipmentIdentifier;
import com.elasticpath.rest.definition.shipments.ShipmentLineItemIdentifier;
import com.elasticpath.rest.definition.shipments.ShipmentLineItemOptionIdentifier;
import com.elasticpath.rest.definition.shipments.ShipmentLineItemOptionsIdentifier;
import com.elasticpath.rest.definition.shipments.ShipmentLineItemsIdentifier;
import com.elasticpath.rest.definition.shipments.ShipmentsIdentifier;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.id.type.PathIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl.SearchRepositoryImpl;

/**
 * Factory methods for building identifiers.
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.CouplingBetweenObjects"})
public final class IdentifierTestFactory {

	private IdentifierTestFactory() {

	}

	/**
	 * Builds a PurchasesIdentifier with test data.
	 *
	 * @param scope the scope
	 * @return PurchasesIdentifier
	 */
	public static PurchasesIdentifier buildPurchasesIdentifier(final String scope) {
		return PurchasesIdentifier.builder().withScope(StringIdentifier.of(scope)).build();
	}

	/**
	 * Builds a PurchaseIdentifier with test data.
	 *
	 * @param scope      the scope
	 * @param purchaseId the purchase id
	 * @return PurchaseIdentifier
	 */
	public static PurchaseIdentifier buildPurchaseIdentifier(final String scope, final String purchaseId) {
		return PurchaseIdentifier.builder()
				.withPurchases(buildPurchasesIdentifier(scope))
				.withPurchaseId(StringIdentifier.of(purchaseId))
				.build();
	}

	/**
	 * Builds a ShipmentsIdentifier with test data.
	 *
	 * @param scope      the scope
	 * @param purchaseId the purchase id
	 * @return ShipmentsIdentifier
	 */
	public static ShipmentsIdentifier buildShipmentsIdentifier(final String scope, final String purchaseId) {
		return ShipmentsIdentifier.builder().withPurchase(buildPurchaseIdentifier(scope, purchaseId)).build();
	}

	/**
	 * Builds a ShipmentIdentifier with test data.
	 *
	 * @param scope      the scope
	 * @param purchaseId the purchase id
	 * @param shipmentId the shipment id
	 * @return ShipmentIdentifier
	 */
	public static ShipmentIdentifier buildShipmentIdentifier(final String scope, final String purchaseId, final String shipmentId) {
		return ShipmentIdentifier.builder()
				.withShipmentId(StringIdentifier.of(shipmentId))
				.withShipments(buildShipmentsIdentifier(scope, purchaseId))
				.build();
	}

	/**
	 * Builds a ShipmentLineItemsIdentifier with test data.
	 *
	 * @param scope      the scope
	 * @param purchaseId the purchase id
	 * @param shipmentId the shipment id
	 * @return ShipmentLineItemsIdentifier
	 */
	public static ShipmentLineItemsIdentifier buildShipmentLineItemsIdentifier(final String scope, final String purchaseId,
																			   final String shipmentId) {
		return ShipmentLineItemsIdentifier.builder().withShipment(buildShipmentIdentifier(scope, purchaseId, shipmentId)).build();
	}

	/**
	 * Builds a ShipmentLineItemIdentifier with test data.
	 *
	 * @param scope              the scope
	 * @param purchaseId         the purchase id
	 * @param shipmentId         the shipment id
	 * @param shipmentLineItemId the shipment line item id
	 * @return ShipmentLineItemIdentifier
	 */
	public static ShipmentLineItemIdentifier buildShipmentLineItemIdentifier(final String scope, final String purchaseId,
																			 final String shipmentId, final String shipmentLineItemId) {
		return ShipmentLineItemIdentifier.builder()
				.withShipmentLineItems(buildShipmentLineItemsIdentifier(scope, purchaseId, shipmentId))
				.withShipmentLineItemId(StringIdentifier.of(shipmentLineItemId))
				.build();
	}

	/**
	 * Builds a ShipmentLineItemOptionsIdentifier with test data.
	 *
	 * @param scope              the scope
	 * @param purchaseId         the purchase id
	 * @param shipmentId         the shipment id
	 * @param shipmentLineItemId the shipment line item id
	 * @return ShipmentLineItemOptionsIdentifier
	 */
	public static ShipmentLineItemOptionsIdentifier buildShipmentLineItemOptionsIdentifier(final String scope, final String purchaseId,
																						   final String shipmentId, final String
																								   shipmentLineItemId) {
		return ShipmentLineItemOptionsIdentifier.builder()
				.withShipmentLineItem(buildShipmentLineItemIdentifier(scope, purchaseId, shipmentId, shipmentLineItemId))
				.build();
	}

	/**
	 * Builds a ShipmentLineItemOptionIdentifier with test data.
	 *
	 * @param scope              the scope
	 * @param purchaseId         the purchase id
	 * @param shipmentId         the shipment id
	 * @param shipmentLineItemId the shipment line item id
	 * @param optionId           the option id
	 * @return ShipmentLineItemOptionIdentifier
	 */
	public static ShipmentLineItemOptionIdentifier buildShipmentLineItemOptionIdentifier(final String scope, final String purchaseId,
																						 final String shipmentId, final String shipmentLineItemId,
																						 final String optionId) {
		return ShipmentLineItemOptionIdentifier.builder()
				.withShipmentLineItemOptions(buildShipmentLineItemOptionsIdentifier(scope, purchaseId, shipmentId, shipmentLineItemId))
				.withShipmentLineItemOptionId(StringIdentifier.of(optionId))
				.build();
	}

	/**
	 * Builds an ItemIdentifier with test data.
	 *
	 * @param scope   the scope
	 * @param skuCode the item sku code
	 * @return ItemIdentifier
	 */
	public static ItemIdentifier buildItemIdentifier(final String scope, final String skuCode) {
		return ItemIdentifier.builder()
				.withItemId(CompositeIdentifier.of(ImmutableMap.of(ItemRepository.SKU_CODE_KEY, skuCode)))
				.withScope(StringIdentifier.of(scope))
				.build();
	}

	/**
	 * Builds an OfferIdentifier with test data.
	 *
	 * @param scope   the scope
	 * @param guid the item sku code
	 * @return ItemIdentifier
	 */
	public static OfferIdentifier buildOfferIdentifier(final String scope, final String guid) {
		return OfferIdentifier.builder()
				.withOfferId(CompositeIdentifier.of(ImmutableMap.of(SearchRepositoryImpl.PRODUCT_GUID_KEY, guid)))
				.withScope(StringIdentifier.of(scope))
				.build();
	}

	/**
	 * Builds a PriceForItemIdentifier with test data.
	 *
	 * @param scope   the scope
	 * @param skuCode the item sku code
	 * @return PriceForItemIdentifier
	 */
	public static PriceForItemIdentifier buildPriceForItemIdentifier(final String scope, final String skuCode) {
		return PriceForItemIdentifier.builder()
				.withItem(buildItemIdentifier(scope, skuCode))
				.build();
	}

	/**
	 * Builds a CartIdentifier with test data.
	 *
	 * @param scope  the scope
	 * @param cartId the cart id
	 * @return CartIdentifier
	 */
	public static CartIdentifier buildCartIdentifier(final String scope, final String cartId) {
		return CartIdentifier.builder()
				.withCartId(StringIdentifier.of(cartId))
				.withCarts(CartsIdentifier.builder()
						.withScope(StringIdentifier.of(scope))
						.build())
				.build();
	}

	/**
	 * Builds a LineItemsIdentifier with test data.
	 *
	 * @param scope  the scope
	 * @param cartId the cart id
	 * @return LineItemsIdentifier
	 */
	public static LineItemsIdentifier buildLineItemsIdentifier(final String scope, final String cartId) {
		return LineItemsIdentifier.builder()
				.withCart(buildCartIdentifier(scope, cartId))
				.build();
	}

	/**
	 * Builds a LineItemIdentifier with test data.
	 *
	 * @param scope      the scope
	 * @param cartId     the cart id
	 * @param lineItemId the line item id
	 * @return LineItemIdentifier
	 */
	public static LineItemIdentifier buildLineItemIdentifier(final String scope, final String cartId, final String lineItemId) {
		return LineItemIdentifier.builder()
				.withLineItems(buildLineItemsIdentifier(scope, cartId))
				.withLineItemId(StringIdentifier.of(lineItemId))
				.build();
	}

	/**
	 * Builds a PriceForCartLineItemIdentifier with test data.
	 *
	 * @param scope      the scope
	 * @param cartId     the cart id
	 * @param lineItemId the line item id
	 * @return PriceForCartLineItemIdentifier
	 */
	public static PriceForCartLineItemIdentifier buildPriceForCartLineItemIdentifier(final String scope,
																					 final String cartId, final String lineItemId) {
		return PriceForCartLineItemIdentifier.builder()
				.withLineItem(buildLineItemIdentifier(scope, cartId, lineItemId))
				.build();
	}

	/**
	 * Builds an ItemDefinitionIdentifier with test data.
	 *
	 * @param scope   the scope
	 * @param skuCode the item sku code
	 * @return ItemDefinitionIdentifier
	 */
	public static ItemDefinitionIdentifier buildItemDefinitionIdentifier(final String scope, final String skuCode) {
		return ItemDefinitionIdentifier.builder()
				.withScope(StringIdentifier.of(scope))
				.withItemId(CompositeIdentifier.of(ImmutableMap.of(ItemRepository.SKU_CODE_KEY, skuCode)))
				.build();
	}

	/**
	 * Builds an ItemDefinitionOptionValueIdentifier with test data.
	 *
	 * @param scope         the scope
	 * @param skuCode       the item sku code
	 * @param optionId      the option id
	 * @param optionValueId the option value id
	 * @return ItemDefinitionOptionValueIdentifier
	 */
	public static ItemDefinitionOptionValueIdentifier buildItemDefinitionOptionValueIdentifier(
			final String scope, final String skuCode, final String optionId, final String optionValueId) {
		return ItemDefinitionOptionValueIdentifier.builder()
				.withItemDefinitionOption(buildItemDefinitionOptionIdentifier(scope, skuCode, optionId))
				.withOptionValueId(StringIdentifier.of(optionValueId))
				.build();
	}

	/**
	 * Builds an ItemDefinitionOptionIdentifier with test data.
	 *
	 * @param scope    the scope
	 * @param skuCode  the item sku code
	 * @param optionId the option id
	 * @return ItemDefinitionOptionIdentifier
	 */
	public static ItemDefinitionOptionIdentifier buildItemDefinitionOptionIdentifier(
			final String scope, final String skuCode, final String optionId) {
		return ItemDefinitionOptionIdentifier.builder()
				.withItemDefinitionOptions(buildItemDefinitionOptionsIdentifier(scope, skuCode))
				.withOptionId(StringIdentifier.of(optionId))
				.build();
	}

	/**
	 * Builds an ItemDefinitionNestedComponentsIdentifier with test data.
	 *
	 * @param scope       the scope
	 * @param skuCode     the item sku code
	 * @param componentId the component id
	 * @return ItemDefinitionNestedComponentsIdentifier
	 */
	public static ItemDefinitionNestedComponentsIdentifier buildItemDefinitionNestedComponentsIdentifier(
			final String scope, final String skuCode, final String componentId) {
		return ItemDefinitionNestedComponentsIdentifier.builder()
				.withItemDefinitionComponent(buildItemDefinitionComponentIdentifier(scope, skuCode, componentId))
				.build();
	}


	/**
	 * Builds an ItemDefinitionOptionsIdentifier with test data.
	 *
	 * @param scope   the scope
	 * @param skuCode the item sku code
	 * @return ItemDefinitionOptionsIdentifier
	 */
	public static ItemDefinitionOptionsIdentifier buildItemDefinitionOptionsIdentifier(final String scope, final String skuCode) {
		return ItemDefinitionOptionsIdentifier.builder()
				.withItemDefinition(buildItemDefinitionIdentifier(scope, skuCode))
				.build();
	}

	/**
	 * Builds an ItemDefinitionComponentOptionValueIdentifier with test data.
	 *
	 * @param scope         the scope
	 * @param skuCode       the item sku code
	 * @param componentId   the component id
	 * @param optionId      the option id
	 * @param optionValueId the option value id
	 * @return ItemDefinitionComponentOptionValueIdentifier
	 */
	public static ItemDefinitionComponentOptionValueIdentifier buildItemDefinitionComponentOptionValueIdentifier(
			final String scope, final String skuCode, final String componentId, final String optionId, final String optionValueId) {
		return ItemDefinitionComponentOptionValueIdentifier.builder()
				.withItemDefinitionComponentOption(buildItemDefinitionComponentOptionIdentifier(scope, skuCode, componentId, optionId))
				.withOptionValueId(StringIdentifier.of(optionValueId))
				.build();
	}

	/**
	 * Builds an ItemDefinitionComponentOptionIdentifier with test data.
	 *
	 * @param scope       the scope
	 * @param skuCode     the item sku code
	 * @param componentId the component id
	 * @param optionId    the option id
	 * @return ItemDefinitionComponentOptionIdentifier
	 */
	public static ItemDefinitionComponentOptionIdentifier buildItemDefinitionComponentOptionIdentifier(
			final String scope, final String skuCode, final String componentId, final String optionId) {
		return ItemDefinitionComponentOptionIdentifier.builder()
				.withItemDefinitionComponentOptions(buildItemDefinitionComponentOptionsIdentifier(scope, skuCode, componentId))
				.withOptionId(StringIdentifier.of(optionId))
				.build();
	}

	/**
	 * Builds an ItemDefinitionComponentOptionsIdentifier with test data.
	 *
	 * @param scope       the scope
	 * @param skuCode     the item sku code
	 * @param componentId the component id
	 * @return ItemDefinitionComponentOptionsIdentifier
	 */
	public static ItemDefinitionComponentOptionsIdentifier buildItemDefinitionComponentOptionsIdentifier(
			final String scope, final String skuCode, final String componentId) {
		return ItemDefinitionComponentOptionsIdentifier.builder()
				.withItemDefinitionComponent(buildItemDefinitionComponentIdentifier(scope, skuCode, componentId))
				.build();
	}

	/**
	 * Builds an ItemDefinitionComponentIdentifier with test data.
	 *
	 * @param scope       the scope
	 * @param skuCode     the item sku code
	 * @param componentId the component id
	 * @return ItemDefinitionComponentIdentifier
	 */
	public static ItemDefinitionComponentIdentifier buildItemDefinitionComponentIdentifier(
			final String scope, final String skuCode, final String componentId) {
		return ItemDefinitionComponentIdentifier.builder()
				.withItemDefinitionComponents(buildItemDefinitionComponentsIdentifier(scope, skuCode))
				.withComponentId(PathIdentifier.of(componentId))
				.build();
	}

	/**
	 * Builds an ItemDefinitionComponentsIdentifier with test data.
	 *
	 * @param scope   the scope
	 * @param skuCode the item sku code
	 * @return ItemDefinitionComponentsIdentifier
	 */
	public static ItemDefinitionComponentsIdentifier buildItemDefinitionComponentsIdentifier(final String scope, final String skuCode) {
		return ItemDefinitionComponentsIdentifier.builder()
				.withItemDefinition(buildItemDefinitionIdentifier(scope, skuCode))
				.build();
	}


	/**
	 * Builds a PriceForItemdefinitionIdentifier with test data.
	 *
	 * @param scope   the scope
	 * @param skuCode the item sku code
	 * @return PriceForItemdefinitionIdentifier
	 */
	public static PriceForItemdefinitionIdentifier buildPriceForItemdefinitionIdentifier(final String scope, final String skuCode) {
		return PriceForItemdefinitionIdentifier.builder()
				.withItemDefinition(buildItemDefinitionIdentifier(scope, skuCode))
				.build();
	}

	/**
	 * Builds a PriceForShipmentLineItemIdentifier with test data.
	 *
	 * @param scope              the scope
	 * @param purchaseId         the purchase id
	 * @param shipmentId         the shipment id
	 * @param shipmentLineItemId the shipment line item id
	 * @return PriceForShipmentLineItemIdentifier
	 */
	public static PriceForShipmentLineItemIdentifier buildPriceForShipmentLineItemIdentifier(final String scope, final String purchaseId,
																							 final String shipmentId,
																							 final String shipmentLineItemId) {
		return PriceForShipmentLineItemIdentifier.builder()
				.withShipmentLineItem(buildShipmentLineItemIdentifier(scope, purchaseId, shipmentId, shipmentLineItemId))
				.build();
	}

	/**
	 * Builds an OrderIdentifier with test data.
	 *
	 * @param scope   the scope
	 * @param orderId the order id
	 * @return OrderIdentifier
	 */
	public static OrderIdentifier buildOrderIdentifier(final String scope, final String orderId) {
		return OrderIdentifier.builder()
				.withOrderId(StringIdentifier.of(orderId))
				.withScope(StringIdentifier.of(scope))
				.build();
	}

	/**
	 * Builds a PurchaseLineItemsIdentifier.
	 *
	 * @param scope      scope
	 * @param purchaseId purchase id
	 * @return PurchaseLineItemsIdentifier
	 */
	public static PurchaseLineItemsIdentifier buildPurchaseLineItemsIdentifier(final String scope, final String purchaseId) {
		return PurchaseLineItemsIdentifier.builder()
				.withPurchase(buildPurchaseIdentifier(scope, purchaseId))
				.build();
	}

	/**
	 * Builds a PurchaseLineItemIdentifier.
	 *
	 * @param scope       scope
	 * @param purchaseId  purchase id
	 * @param lineItemIds line item ids
	 * @return PurchaseLineItemIdentifier
	 */
	public static PurchaseLineItemIdentifier buildPurchaseLineItemIdentifier(final String scope, final String purchaseId,
																			 final List<String> lineItemIds) {
		return PurchaseLineItemIdentifier.builder()
				.withLineItemId(PathIdentifier.of(lineItemIds))
				.withPurchaseLineItems(buildPurchaseLineItemsIdentifier(scope, purchaseId))
				.build();
	}

}
