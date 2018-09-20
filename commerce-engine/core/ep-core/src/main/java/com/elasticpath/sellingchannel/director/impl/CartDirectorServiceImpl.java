/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.sellingchannel.director.impl;

import static java.util.Arrays.asList;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import com.google.common.collect.ImmutableMap;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.common.dto.StructuredErrorMessage;
import com.elasticpath.commons.exception.EpValidationException;
import com.elasticpath.domain.cartmodifier.CartItemModifierField;
import com.elasticpath.domain.cartmodifier.CartItemModifierGroup;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartMessageIds;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.WishList;
import com.elasticpath.domain.shoppingcart.impl.CartItem;
import com.elasticpath.domain.store.Store;
import com.elasticpath.sellingchannel.ProductUnavailableException;
import com.elasticpath.sellingchannel.director.CartDirector;
import com.elasticpath.sellingchannel.director.CartDirectorService;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.service.shoppingcart.WishListService;
import com.elasticpath.service.shoppingcart.impl.AddToWishlistResult;
import com.elasticpath.validation.ConstraintViolationTransformer;
import com.elasticpath.validation.service.CartItemModifierFieldValidationService;

/**
 * Service which wraps CartDirector's shopping cart update functionality within Container managed
 * transactional boundaries.
 * <p>
 * This allows outside callers like Controllers and Cortex to safely access CartDirector functionality from
 * within an over-arching transaction.
 */
public class CartDirectorServiceImpl implements CartDirectorService {
	private CartDirector cartDirector;
	private WishListService wishListService;
	private ShoppingCartService shoppingCartService;
	private ProductSkuLookup productSkuLookup;
	private PricingSnapshotService pricingSnapshotService;

	private Validator validator;
	private ConstraintViolationTransformer constraintViolationTransformer;
	private CartItemModifierFieldValidationService cartItemModifierFieldValidationService;

	public void setValidator(final Validator validator) {
		this.validator = validator;
	}

	public void setConstraintViolationTransformer(
			final ConstraintViolationTransformer constraintViolationTransformer) {
		this.constraintViolationTransformer = constraintViolationTransformer;
	}

	public void setCartItemModifierFieldValidationService(final CartItemModifierFieldValidationService cartItemModifierFieldValidationService) {
		this.cartItemModifierFieldValidationService = cartItemModifierFieldValidationService;
	}

	@Override
	public ShoppingItem addItemToCart(final ShoppingCart shoppingCart, final ShoppingItemDto dto) {
		validateAddAndUpdateCartItem(dto);

		ShoppingItem shoppingItem = getCartDirector().addItemToCart(shoppingCart, dto);

		saveShoppingCart(shoppingCart);

		return shoppingItem;
	}

	@Override
	public ShoppingCart updateCartItem(final ShoppingCart shoppingCart, final long itemId, final ShoppingItemDto dto) {
		validateAddAndUpdateCartItem(dto);

		getCartDirector().updateCartItem(shoppingCart, itemId, dto);

		return saveShoppingCart(shoppingCart);
	}

	private void validateAddAndUpdateCartItem(final ShoppingItemDto dto) {

		List<StructuredErrorMessage> commerceMessageList = new LinkedList<>();
		commerceMessageList.addAll(validateDomainBean(dto));
		commerceMessageList.addAll(performDynamicValidation(dto));

		if (!commerceMessageList.isEmpty()) {
			throw new EpValidationException("CartItem validation failure.", commerceMessageList);
		}
	}

	private List<StructuredErrorMessage> performDynamicValidation(final ShoppingItemDto dto) {
		ProductSku productSku = productSkuLookup.findBySkuCode(dto.getSkuCode());
		Set<CartItemModifierGroup> cartItemModifierGroups = productSku.getProduct().getProductType().getCartItemModifierGroups();
		return cartItemModifierFieldValidationService.validate(dto.getItemFields(), getCartItemModifierFields(cartItemModifierGroups));
	}

	private List<StructuredErrorMessage> validateDomainBean(final ShoppingItemDto dto) {
		Set<ConstraintViolation<ShoppingItemDto>> shoppingViolations = validator.validate(dto);
		return constraintViolationTransformer.transform(shoppingViolations);
	}

	private Set<CartItemModifierField> getCartItemModifierFields(final Set<CartItemModifierGroup> cartItemModifierGroups) {
		return cartItemModifierGroups.stream().map(CartItemModifierGroup::getCartItemModifierFields)
				.flatMap(Set::stream)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	@Override
	public ShoppingCart updateCompoundCartItem(
			final ShoppingCart shoppingCart, final long rootItemId, final ShoppingItemDto rootItemDto,
			final List<ShoppingItemDto> dependentItemDtos, final List<ShoppingItemDto> associatedItemDtos) {
		ShoppingItem parentItem;
		if (isUpdate(rootItemId)) {
			parentItem = cartDirector.updateCartItem(shoppingCart, rootItemId, rootItemDto);

			// remove existing dependents, we will add back newly selected dependents below
			final List<ShoppingItem> dependents = ((CartItem) parentItem).getDependentItems();
			parentItem.getChildren().removeAll(dependents);
			shoppingCart.getCartItems().removeAll(dependents);
		} else {
			parentItem = cartDirector.addItemToCart(shoppingCart, rootItemDto, null);
		}
		for (ShoppingItemDto dependentItemDto : dependentItemDtos) {
			cartDirector.addItemToCart(shoppingCart, dependentItemDto, parentItem);
		}
		for (ShoppingItemDto associatedItemDto : associatedItemDtos) {
			cartDirector.addItemToCart(shoppingCart, associatedItemDto, null);
		}

		return saveShoppingCart(shoppingCart);
	}

	private boolean isUpdate(final long rootItemId) {
		return rootItemId != 0;
	}

	@Override
	public ShoppingCart removeItemsFromCart(final ShoppingCart shoppingCart, final String... doomedItemGuids) {
		for (final String doomedItemGuid : doomedItemGuids) {
			shoppingCart.removeCartItem(doomedItemGuid);
		}

		return saveShoppingCart(shoppingCart);
	}

	@Override
	public ShoppingCart refresh(final ShoppingCart shoppingCart) {
		getCartDirector().refresh(shoppingCart);

		return saveShoppingCart(shoppingCart);
	}

	@Override
	public void reApplyCatalogPromotions(final ShoppingCart shoppingCart) {
		getCartDirector().refresh(shoppingCart);
	}

	@Override
	public ShoppingItem addSkuToWishList(final String skuCode, final Shopper shopper, final Store store) {
		ShoppingItem item = getCartDirector().createShoppingItem(skuCode, store, 1);

		WishList wishList = getWishListService().findOrCreateWishListByShopper(shopper);
		getWishListService().addItem(wishList, item);
		getWishListService().save(wishList);

		return item;
	}

	@Override
	public ShoppingItem moveItemFromWishListToCart(final ShoppingCart shoppingCart, final ShoppingItemDto dto, final String wishlistLineItemGuid) {

		if (!getCartDirector().isSkuAllowedAddToCart(dto.getSkuCode(), shoppingCart)) {
			String errorMessage = "Item is not purchasable and cannot be added to cart";
			throw new ProductUnavailableException(
					errorMessage,
					asList(
							new StructuredErrorMessage(
									ShoppingCartMessageIds.ITEM_NOT_AVAILABLE,
									errorMessage,
									ImmutableMap.of("item-code", dto.getSkuCode())
							)
					)
			);
		}

		validateAddAndUpdateCartItem(dto);

		WishList wishList = getWishListService().findOrCreateWishListByShopper(shoppingCart.getShopper());
		wishList.removeItem(wishlistLineItemGuid);
		getWishListService().save(wishList);

		ShoppingItem shoppingItem = getCartDirector().addItemToCart(shoppingCart, dto);

		if (dto.getQuantity() > 1) {
			shoppingCart.setItemWithNoTierOneFromWishList(true);
		}

		saveShoppingCart(shoppingCart);

		return shoppingItem;
	}

	@Override
	public AddToWishlistResult moveItemFromCartToWishList(final ShoppingCart shoppingCart, final String cartLineItemGuid) {

		ShoppingItem cartItem = shoppingCart.getShoppingItemByGuid(cartLineItemGuid);

		WishList wishList = getWishListService().findOrCreateWishListByShopper(shoppingCart.getShopper());
		AddToWishlistResult addToWishlistResult = wishListService.addItem(wishList, cartItem);
		getWishListService().save(wishList);

		removeItemsFromCart(shoppingCart, cartItem.getGuid());

		return addToWishlistResult;
	}

	@Override
	public ShoppingCart clearItems(final ShoppingCart shoppingCart) {
		getCartDirector().clearItems(shoppingCart);

		return saveShoppingCart(shoppingCart);
	}

	/**
	 * Persists {@code shoppingCart} in the database.
	 *
	 * @param shoppingCart The shopping cart to save.
	 * @return The now updated shopping cart.
	 */
	protected ShoppingCart saveShoppingCart(final ShoppingCart shoppingCart) {
		getCartDirector().reorderItems(shoppingCart);
		getPricingSnapshotService().getPricingSnapshotForCart(shoppingCart);
		return getShoppingCartService().saveOrUpdate(shoppingCart);
	}

	protected CartDirector getCartDirector() {
		return cartDirector;
	}

	public void setCartDirector(final CartDirector cartDirector) {
		this.cartDirector = cartDirector;
	}

	protected WishListService getWishListService() {
		return wishListService;
	}

	public void setWishListService(final WishListService wishListService) {
		this.wishListService = wishListService;
	}

	protected ShoppingCartService getShoppingCartService() {
		return shoppingCartService;
	}

	public void setShoppingCartService(final ShoppingCartService shoppingCartService) {
		this.shoppingCartService = shoppingCartService;
	}

	protected ProductSkuLookup getProductSkuLookup() {
		return productSkuLookup;
	}

	public void setProductSkuLookup(final ProductSkuLookup productSkuLookup) {
		this.productSkuLookup = productSkuLookup;
	}

	protected PricingSnapshotService getPricingSnapshotService() {
		return pricingSnapshotService;
	}

	public void setPricingSnapshotService(final PricingSnapshotService pricingSnapshotService) {
		this.pricingSnapshotService = pricingSnapshotService;
	}
}
