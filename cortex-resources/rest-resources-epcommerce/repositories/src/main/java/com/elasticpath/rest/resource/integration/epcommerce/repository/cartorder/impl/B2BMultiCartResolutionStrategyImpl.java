/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl.ShoppingCartResourceConstants.CREATE_CART_NOT_SUPPORTED;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.impl.CartData;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.identity.ScopePrincipal;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.identity.attribute.SubjectAttribute;
import com.elasticpath.rest.identity.util.PrincipalsUtil;

/**
 * B2C strategy for multi-carts.
 */
@Singleton
@Named("b2bMulticartStrategy")
public class B2BMultiCartResolutionStrategyImpl extends  AbstractEpMultiCartStrategyImpl  {

	private static final String NAME = "name";
	/**
	 * this resolution strategy is associated with the default cartType.
	 *
	 * This should be moved to it's own B2B cart type.
	 */
	private static final String ASSOCIATED_CARTTYPE = "default";

	@Override
	public boolean isApplicable(final Subject subject) {

		if (subject == null) {
			return false;
		}

		Collection<SubjectAttribute> attributes = subject.getAttributes();
		return attributes != null && attributes.stream().anyMatch(attr -> attr.getType().equals(ShoppingCartResourceConstants.METADATA));
	}

	@Override
	public Observable<String> findAllCarts(final String customerGuid, final String storeCode, final Subject subject) {

		Map<String, List<CartData>> cartDataForCarts =
				getShoppingCartService().findCartDataForCarts(getShoppingCartService().findByCustomerAndStore(customerGuid, storeCode));

	return 	Observable.fromIterable(cartDataForCarts.entrySet().stream()
				.filter(entry -> entry.getValue().stream()
						.anyMatch(datum -> subjectAttributeMatchCartDataForShopper(subject, datum)))
				.map(Map.Entry::getKey)::iterator);
	}

	private boolean subjectAttributeMatchCartDataForShopper(final Subject subject, final CartData datum) {
		SubjectAttribute subjectAttributeUserIdAttribute = getAttributeValue(subject, ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_ID);
		return datum.getKey().equals(ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_ID)
				&& subjectAttributeUserIdAttribute.getValue().equals(datum.getValue());
	}

	@Override
	public boolean supportsCreate(final Subject subject, final Shopper shopper, final String storeCode) {

		if (!hasMulticartEnabled(storeCode)) {
			return false;
		}

		return shopper.getCustomer().isRegistered();
	}

	@Override
	public Single<String> getDefaultShoppingCartGuid() {
		Subject subject = getResourceOperationContext().getSubject();
		String userId = getResourceOperationContext().getUserIdentifier();
		ScopePrincipal scopePrincipal = PrincipalsUtil.getFirstPrincipalByType(subject.getPrincipals(), ScopePrincipal.class);
		String storeCode = scopePrincipal.getValue();
		Map<String, List<CartData>> cartDataForCarts =
				getShoppingCartService().findCartDataForCarts(getShoppingCartService().findByCustomerAndStore(userId, storeCode));

		for (Map.Entry<String, List<CartData>> entry : cartDataForCarts.entrySet()) {

			List<CartData> value = entry.getValue();
			if (isDefaultCartDataForShopperFromSubjectAttributes(subject, value)) {
				return Single.just(entry.getKey());
			}
		}
		return createDefaultCartForShopperFromAttributes(subject).map(ShoppingCart::getGuid);
	}

	/**
	 * Checks the subject attributes against the cartData for the shopping cart to see if the cartData is for the default cart.
	 * @param subject the subject.
	 * @param cartDatas the cart Data.
	 * @return true if this is the default cart for the shopper.
	 */
	private boolean isDefaultCartDataForShopperFromSubjectAttributes(final Subject subject, final List<CartData> cartDatas) {
		String subjectAttributeUserId = getAttributeValue(subject, ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_ID).getValue();
		String subjectAttributeUserEmail = getAttributeValue(subject, ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_EMAIL).getValue();
		String subjectAttributeUserName = getAttributeValue(subject, ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_NAME).getValue();

		boolean hasId = false;
		boolean hasEmail = false;
		boolean hasName = false;
		for (CartData cartData : cartDatas) {
			switch (cartData.getKey()) {

				case ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_ID:
					hasId = cartData.getValue().equals(subjectAttributeUserId);
					break;
				case ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_EMAIL:
					hasEmail = cartData.getValue().equals(subjectAttributeUserEmail);
					break;
				case ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_NAME:
					hasName  = cartData.getValue().equals(subjectAttributeUserName);
					break;
				default:
					//no-op. We don't care about other datas such as name.
			}
		}
		return  hasId && hasEmail && hasName;
	}

	@Override
	public Single<ShoppingCart> getDefaultShoppingCart() {
		return getDefaultShoppingCartGuid().flatMap(this::getShoppingCartSingle);
	}

	@Override
	public Single<ShoppingCart> getDefaultCart(final CustomerSession customerSession) {
		return getDefaultShoppingCartGuid().flatMap(guid -> getShoppingCartSingle(guid, customerSession));
	}

	@Override
	public Single<ShoppingCart> createCart(final Map<String, String> descriptors, final String scope) {

		if (!hasMulticartEnabled(scope)) {
			return Single.error(ResourceOperationFailure.stateFailure(CREATE_CART_NOT_SUPPORTED));
		}
		
		Map<String, String> updatedIdentifiers = new HashMap<>(descriptors);
		Subject subject = getResourceOperationContext().getSubject();
		updatedIdentifiers.putAll(getSubjectAttributeIdentifiers(subject));

		return  getCustomerSessionRepository().createCustomerSessionAsSingle()
				.flatMap(customerSession -> createCartInternal(customerSession, updatedIdentifiers));
	}

	private Single<ShoppingCart> createDefaultCartForShopperFromAttributes(final Subject subject) {

		Map<String, String> identifiers = getSubjectAttributeIdentifiers(subject);

		SubjectAttribute subjectAttributeUserName = getAttributeValue(subject, ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_NAME);
		identifiers.put(NAME, subjectAttributeUserName.getValue());

		return  getCustomerSessionRepository().createCustomerSessionAsSingle()
				.flatMap(customerSession -> createCartInternal(customerSession, identifiers));

	}

	private Map<String, String> getSubjectAttributeIdentifiers(final Subject subject) {
		Map<String, String> identifiers = new HashMap<>();

		SubjectAttribute subjectAttributeUserId = getAttributeValue(subject, ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_ID);
		SubjectAttribute subjectAttributeUserEmail = getAttributeValue(subject, ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_EMAIL);
		SubjectAttribute subjectAttributeUserName = getAttributeValue(subject, ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_NAME);

		identifiers.put(subjectAttributeUserId.getKey(), subjectAttributeUserId.getValue());
		identifiers.put(subjectAttributeUserEmail.getKey(), subjectAttributeUserEmail.getValue());
		identifiers.put(subjectAttributeUserName.getKey(), subjectAttributeUserName.getValue());
		return identifiers;
	}

	private SubjectAttribute getAttributeValue(final Subject subject, final String attributeName) {

		return getSubjectAttributeStreamForType(subject, ShoppingCartResourceConstants.METADATA)
				.filter(attribute -> attribute.getKey().equalsIgnoreCase(attributeName))
				.findFirst().orElseThrow(()
						-> new EpSystemException(String.format("attribute `%s` not found ", attributeName)));
	}

	private Stream<SubjectAttribute> getSubjectAttributeStreamForType(final Subject subject, final String type) {
		return subject.getAttributes().stream().filter(subjectAttribute -> subjectAttribute.getType().equals(type));
	}

	@Override
	protected String getValidCartTypeForStrategy() {
		return ASSOCIATED_CARTTYPE;
	}

}
