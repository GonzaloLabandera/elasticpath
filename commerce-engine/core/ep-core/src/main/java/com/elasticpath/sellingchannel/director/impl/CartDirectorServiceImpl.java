/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.sellingchannel.director.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.structured.EpValidationException;
import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.WishList;
import com.elasticpath.domain.store.Store;
import com.elasticpath.sellingchannel.ProductUnavailableException;
import com.elasticpath.sellingchannel.director.CartDirector;
import com.elasticpath.sellingchannel.director.CartDirectorService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.service.shoppingcart.ShoppingItemService;
import com.elasticpath.service.shoppingcart.WishListService;
import com.elasticpath.service.shoppingcart.impl.AddToWishlistResult;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.connectivity.context.XPFShoppingItemValidationContext;
import com.elasticpath.xpf.connectivity.entity.XPFOperationEnum;
import com.elasticpath.xpf.connectivity.extensionpoint.ShoppingItemValidator;
import com.elasticpath.xpf.context.builders.ShoppingItemValidationContextBuilder;
import com.elasticpath.xpf.converters.StructuredErrorMessageConverter;
import com.elasticpath.xpf.impl.XPFExtensionSelectorByStoreCode;

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
	private PricingSnapshotService pricingSnapshotService;
	private ShoppingItemService shoppingItemService;
	private XPFExtensionLookup extensionLookup;

	private ShoppingItemValidationContextBuilder shoppingItemValidationContextBuilder;
	private StructuredErrorMessageConverter structuredErrorMessageConverter;

	@Override
	public ShoppingItem addItemToCart(final ShoppingCart shoppingCart, final ShoppingItemDto dto) {
		final ShoppingItem shoppingItem = getCartDirector().addItemToCart(shoppingCart, dto);

		final ShoppingCart persistedShoppingCart = saveShoppingCart(shoppingCart);

		return persistedShoppingCart.getCartItemByGuid(shoppingItem.getGuid());
	}

	@Override
	public ShoppingCart addItemsToCart(final ShoppingCart shoppingCart, final List<ShoppingItemDto> dtos) {

		Set<StructuredErrorMessage> errorMessagesCollected = new HashSet<>();

		for (ShoppingItemDto dto : dtos) {
			errorMessagesCollected.addAll(addItem(shoppingCart, dto, null));
		}

		if (errorMessagesCollected.isEmpty()) {
			return saveShoppingCart(shoppingCart);
		} else {
			throw new ProductUnavailableException("One or mode products could not be added to cart", new ArrayList<>(errorMessagesCollected));
		}
	}

	private Set<StructuredErrorMessage> addItem(final ShoppingCart shoppingCart,
												final ShoppingItemDto shoppingItemDto,
												final ShoppingItem parentItem) {
		Set<StructuredErrorMessage> results = new HashSet<>();
		ShoppingItem newItem = cartDirector.addItemToCart(shoppingCart, shoppingItemDto, parentItem);

		if (newItem == null) {
			StructuredErrorMessage structuredErrorMessage = constructStructuredErrorMessage(shoppingItemDto);
			results.add(structuredErrorMessage);
		}

		return results;
	}

	private StructuredErrorMessage constructStructuredErrorMessage(final ShoppingItemDto dto) {
		String sku = dto.getSkuCode();
		String debugMessage = "Item " + sku + " is not available";
		String messageId = "item.not.available";
		Map<String, String> errorData = new HashMap<>();
		errorData.put("item-code", sku);

		return new StructuredErrorMessage(messageId, debugMessage, errorData);
	}

	@Override
	public ShoppingCart updateCartItem(final ShoppingCart shoppingCart, final long itemId, final ShoppingItemDto dto) {
		getCartDirector().updateCartItem(shoppingCart, itemId, dto);

		return saveShoppingCart(shoppingCart);
	}

	@Override
	public ShoppingCart updateCompoundCartItem(
			final ShoppingCart shoppingCart, final long rootItemId, final ShoppingItemDto rootItemDto,
			final List<ShoppingItemDto> dependentItemDtos, final List<ShoppingItemDto> associatedItemDtos) {
		ShoppingItem parentItem;
		if (isUpdate(rootItemId)) {
			parentItem = cartDirector.updateCartItem(shoppingCart, rootItemId, rootItemDto);

			// remove existing dependents, we will add back newly selected dependents below
			final List<ShoppingItem> dependents = parentItem.getChildren().stream()
					.filter(shoppingItem -> !shoppingItem.isBundleConstituent())
					.collect(Collectors.toList());

			parentItem.getChildren().removeAll(dependents);

			final Collection<String> dependentGuids = dependents.stream()
					.map(ShoppingItem::getGuid)
					.collect(Collectors.toSet());

			shoppingCart.removeCartItems(dependentGuids);
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
			validateCartItemBeforeDeletion(shoppingCart, doomedItemGuid);
			shoppingCart.removeCartItem(doomedItemGuid);
		}

		shoppingItemService.deleteItemsByGuids(doomedItemGuids);

		return shoppingCart;
	}

	private void validateCartItemBeforeDeletion(final ShoppingCart shoppingCart, final String itemGuid) {
		ShoppingItem shoppingItem = shoppingCart.getCartItemByGuid(itemGuid);

		ShoppingItem parentShoppingItem = null;
		if (shoppingItem.getParentItemUid() != null) {
			parentShoppingItem = shoppingCart.getCartItemById(shoppingItem.getParentItemUid());
		}

		final XPFShoppingItemValidationContext context =
				shoppingItemValidationContextBuilder.build(shoppingCart, shoppingItem, parentShoppingItem, XPFOperationEnum.DELETE);
		final Collection<StructuredErrorMessage> validationMessages = executeValidators(shoppingCart, context);
		if (!validationMessages.isEmpty()) {
			throw new EpValidationException("Remove item from cart validation failure", validationMessages);
		}
	}

	private Collection<StructuredErrorMessage> executeValidators(final ShoppingCart shoppingCart,
																 final XPFShoppingItemValidationContext context) {
		List<ShoppingItemValidator> validators = extensionLookup.getMultipleExtensions(
				ShoppingItemValidator.class,
				XPFExtensionPointEnum.VALIDATE_SHOPPING_ITEM_AT_REMOVE_FROM_CART,
				new XPFExtensionSelectorByStoreCode(shoppingCart.getStore().getCode()));
		return validators.stream()
				.map(strategy -> strategy.validate(context))
				.flatMap(Collection::stream)
				.map(structuredErrorMessageConverter::convert)
				.collect(Collectors.toList());
	}

	@Override
	public ShoppingItem addSkuToWishList(final String skuCode, final Shopper shopper, final Store store) {
		ShoppingItem item = getCartDirector().createShoppingItem(skuCode, store, 1);

		WishList wishList = getWishListService().findOrCreateWishListByShopper(shopper);
		final AddToWishlistResult result = getWishListService().addItem(wishList, item);
		final WishList persistedWishList = getWishListService().save(wishList);

		return persistedWishList.getAllItems().stream()
				.filter(shoppingItem -> shoppingItem.getGuid().equals(result.getShoppingItem().getGuid()))
				.findAny()
				.orElse(null);
	}

	@Override
	public ShoppingItem moveItemFromWishListToCart(final ShoppingCart shoppingCart, final ShoppingItemDto dto, final String wishlistLineItemGuid) {
		WishList wishList = getWishListService().findOrCreateWishListByShopper(shoppingCart.getShopper());
		wishList.removeItem(wishlistLineItemGuid);
		getWishListService().save(wishList);

		ShoppingItem shoppingItem = getCartDirector().addItemToCart(shoppingCart, dto);

		if (dto.getQuantity() > 1) {
			shoppingCart.setItemWithNoTierOneFromWishList(true);
		}

		final ShoppingCart persistedShoppingCart = saveShoppingCart(shoppingCart);

		return persistedShoppingCart.getCartItemByGuid(shoppingItem.getGuid());
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
		pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
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

	protected PricingSnapshotService getPricingSnapshotService() {
		return pricingSnapshotService;
	}

	public void setPricingSnapshotService(final PricingSnapshotService pricingSnapshotService) {
		this.pricingSnapshotService = pricingSnapshotService;
	}

	public ShoppingItemService getShoppingItemService() {
		return shoppingItemService;
	}

	public void setShoppingItemService(final ShoppingItemService shoppingItemService) {
		this.shoppingItemService = shoppingItemService;
	}

	protected XPFExtensionLookup getExtensionLookup() {
		return extensionLookup;
	}

	public void setExtensionLookup(final XPFExtensionLookup extensionLookup) {
		this.extensionLookup = extensionLookup;
	}

	protected ShoppingItemValidationContextBuilder getShoppingItemValidationContextBuilder() {
		return shoppingItemValidationContextBuilder;
	}

	public void setShoppingItemValidationContextBuilder(final ShoppingItemValidationContextBuilder shoppingItemValidationContextBuilder) {
		this.shoppingItemValidationContextBuilder = shoppingItemValidationContextBuilder;
	}

	protected StructuredErrorMessageConverter getStructuredErrorMessageConverter() {
		return structuredErrorMessageConverter;
	}

	public void setStructuredErrorMessageConverter(final StructuredErrorMessageConverter structuredErrorMessageConverter) {
		this.structuredErrorMessageConverter = structuredErrorMessageConverter;
	}
}
